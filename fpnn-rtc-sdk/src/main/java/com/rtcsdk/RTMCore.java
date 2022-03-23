package com.rtcsdk;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fpnn.sdk.ClientEngine;
import com.fpnn.sdk.ConnectionWillCloseCallback;
import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.MessagePayloadUnpacker;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.rtcsdk.RTMStruct.CaptureLevle;
import com.rtcsdk.RTMStruct.FileStruct;
import com.rtcsdk.RTMStruct.MessageType;
import com.rtcsdk.RTMStruct.P2PRTCEvent;
import com.rtcsdk.RTMStruct.RTMAnswer;
import com.rtcsdk.RTMStruct.RTMMessage;
import com.rtcsdk.RTMStruct.RoomInfo;
import com.rtcsdk.RTMStruct.TranslatedInfo;
import com.rtcsdk.UserInterface.IRTMCallback;
import com.rtcsdk.UserInterface.IRTMEmptyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class RTMCore  extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks{
    public enum ClientStatus {
        Closed,
        Connecting,
        Connected
    }

    public enum CloseType {
        ByUser,
        ByServer,
        Timeout,
        None
    }

    boolean isMicAvaliable(){
        Boolean available = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if( mAudioManager.getActiveRecordingConfigurations().size()>0)
                available = false;
        }
        else {
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_DEFAULT, 44100);
            try {
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                    available = false;

                }
                recorder.startRecording();
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                    available = false;

                }
                recorder.stop();
            } finally {
                recorder.release();
                recorder = null;
            }
        }
        if (!available)
            printLog("microphone is be used in another app");
        return available;
    }

    //for network change
    private int LAST_TYPE = NetUtils.NETWORK_NOTINIT;


    @Override
    public void onReceive(Context context, Intent intent) {
        String b= ConnectivityManager.CONNECTIVITY_ACTION;
        String a= intent.getAction();
        if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(a)) {
            final int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.STATE", Integer.MIN_VALUE);
            if (intExtra == 2 || intExtra == 0) {//2-连接 0-断开
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        RTCEngine.headsetStat(intExtra);
                    }
                }, 1500L);
            }
        }
        else if (a == b || (a != null && a.equals(b))) {
            int netWorkState = NetUtils.getNetWorkState(context);
            if (LAST_TYPE != netWorkState) {
                LAST_TYPE = netWorkState;
                onNetChange(netWorkState);
            }
        }
        else if (a.equals(Intent.ACTION_HEADSET_PLUG)) {
            if (intent.hasExtra("state")){
                final int ret = intent.getIntExtra("state", 0);
                if (ret ==0 || ret == 1){//0-拔出 1-插入
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                            RTCEngine.headsetStat(ret);
                        }
                    }, 1000L);
                }
            }
        }
    }
    //for network change

    //-------------[ Fields ]--------------------------//
    private final Object interLocker =  new Object();
    private long pid;
    private long uid;
    private String lang;
    private String token;
    private String curve;
    private String rtmEndpoint;
    private String rtcEndpoint;
    private Context context;
    private Application application;
    private WeakReference<Activity> currentActivity;
    private boolean background = false;
    private byte[] encrptyData;
    private boolean autoConnect;
    boolean cameraStatus = false;
    OrientationEventListener mOrEventListener;
    int currVideoLevel = CaptureLevle.Normal.value();

    long lastCallId = 0; //p2pRTC 用
    int lastP2Ptype = 0; //1-音频 2-视频
    long peerUid = 0; //p2p对方uid
//    int earpieceid = 0;
//    int speakerid = 0;
    //时间校准
//    private AtomicLong minRTT = new AtomicLong(0);
    AtomicLong differenceTime = new AtomicLong(0);
    private Thread serverTimeCheck;
    //
//    int apiSelect = 1;
    private Map<String, String>  loginAttrs;
    private ClientStatus rtmGateStatus = ClientStatus.Closed;
    private CloseType closedCase = CloseType.None;
    private int lastNetType = NetUtils.NETWORK_NOTINIT;
    private AtomicBoolean isRelogin = new AtomicBoolean(false);
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicBoolean initCheckThread = new AtomicBoolean(false);
    private Thread checkThread;
    private RTMQuestProcessor processor;
    ErrorRecorder errorRecorder = new ErrorRecorder();
//    private TCPClient dispatch;
    private TCPClient rtmGate;
    private Map<String, Map<TCPClient, Long>> fileGates;
    private AtomicLong connectionId = new AtomicLong(0);
    private AtomicBoolean noNetWorkNotify = new AtomicBoolean(false);
    private RTMAnswer lastReloginAnswer = new RTMAnswer();
    private RTMPushProcessor serverPushProcessor;
    RTMConfig rtmConfig;
    RTMUtils rtmUtils = new RTMUtils();

    final int okRet = RTMErrorCode.RTM_EC_OK.value();
    final int videoError = RTMErrorCode.RTM_EC_VIDEO_ERROR.value();
    final int voiceError = RTMErrorCode.RTM_EC_VOICE_ERROR.value();

    //voice
    //video
    public enum RTMModel{
        Normal,
        VOICE,
        VIDEO
    }


//    static HashMap<Long, Long> mMTS = new HashMap<>();
//    byte[] mPreBuffer = null;

//    //    String cameraId;
//    public int Video_Framerate = 15;
//    public int Video_Bitrate = 1024*300;
//    public int Video_Width = 320;
//    public int Video_Height = 240;
////    public int Video_Width = 320;
////    public int Video_Height = 240;
//    final String H264_MIME = MediaFormat.MIMETYPE_VIDEO_AVC;
//    final int VIdeo_FrameInterval = 30;


    private ArrayList<Integer> finishCodeList = new ArrayList<Integer>(){{
        add(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value());
        add(RTMErrorCode.RTM_EC_PROJECT_BLACKUSER.value()); }};

    //voice
//    private int mFinalCount = 0;
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
//        printLog("onActivityCreated");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        this.currentActivity = new WeakReference<Activity>(activity);
        if (this.background && !activity.isChangingConfigurations()) {
            this.background = false;
            RTCEngine.setBackground(false);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!this.background && (this.currentActivity == null || activity == this.currentActivity.get()) && !activity.isChangingConfigurations()) {
            this.background = true;
            RTCEngine.setBackground(true);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public  static AudioManager mAudioManager;
//    AtomicBoolean pause = new AtomicBoolean(false);
    final Object videoLocker =  new Object();
    private int voiceConnectionId = 0;

//    public int setBluetoothScoOn(final boolean flag) {
//        if (mAudioManager == null) {
//            return 0;
//        }
//        try {
//            if (flag) {
//                mAudioManager.startBluetoothSco();
//                mAudioManager.setBluetoothScoOn(true);
//            }
//            else {
//                mAudioManager.setBluetoothScoOn(false);
//                mAudioManager.stopBluetoothSco();
//            }
//        }
//        catch (Exception ex) {
//            Log.e("device", "setBluetoothScoOn failed, " + ex.getMessage());
//            return -1;
//        }
//        return 0;
//    }

    void rtcClear(){
        RTCEngine.RTCClear();
    }

    void enterRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId) {
        final RoomInfo ret = new RoomInfo();
//        ret.roomId = roomId;
//        if (rtmModel == RTMModel.VIDEO &&  videoRoom > 0) {
//            callback.onResult(ret, genRTMAnswer(videoError,"please exit video room before enter another video room"));
//            return;
//        }


        Quest quest = new Quest("enterRTCRoom");
        quest.param("rid", roomId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                ret.errorCode = errorCode;
                if (errorCode == okRet) {
                    String roomtoken = rtmUtils.wantString(answer,"token");
                    ret.roomTyppe = rtmUtils.wantInt(answer,"type");
                    enterRTCRoomReal(callback, roomId, roomtoken, ret, ret.roomTyppe);
                } else {
                    callback.onResult(ret, genRTMAnswer(answer, errorCode));
                }
            }
        });
    }

    void enterRTCRoomReal(@NonNull final IRTMCallback callback, final long roomId, final String token, final RoomInfo ret, int roomTyppe) {
        byte[] enterRet = RTCEngine.enterRTCRoom(token, roomId, roomTyppe);
        MessagePayloadUnpacker kk = new MessagePayloadUnpacker(enterRet);
        HashMap retmap;
        RTMAnswer rst = new RTMAnswer(0,"");
        try {
            retmap= new HashMap(kk.unpack());
            if (retmap.containsKey("ex")){
                ret.errorCode = rtmUtils.wantInt(retmap.get("code"));
                ret.errorMsg = String.valueOf(retmap.get("ex"));
                rst.errorCode = ret.errorCode;
                rst.errorMsg = ret.errorMsg;

            }
            else
            {
                ret.errorCode = 0;
                ret.errorMsg = "";
                ret.uids = rtmUtils.longHashSet(retmap.get("uids"));
                ret.owner = rtmUtils.wantLong(retmap.get("owner"));
                ret.managers = rtmUtils.longHashSet(retmap.get("administrators"));
            }
        }
        catch (Exception e) {
            errorRecorder.recordError("Decoding enterRTCRoomReal package exception");
        }
        callback.onResult(ret, rst);
    }

    class RTMQuestProcessor{
        private DuplicatedMessageFilter duplicatedFilter;
        private AtomicLong lastPingTime;

        public RTMQuestProcessor() {
            duplicatedFilter = new DuplicatedMessageFilter();
            lastPingTime = new AtomicLong();
        }

        synchronized void setLastPingTime(long time){
            lastPingTime.set(time);
        }

        synchronized long getLastPingTime(){
            return lastPingTime.get();
        }

        boolean ConnectionIsAlive() {
            long lastPingSec = lastPingTime.get();
            boolean ret = true;

            if (Genid.getCurrentSeconds() - lastPingSec > RTMConfig.lostConnectionAfterLastPingInSeconds) {
                ret = false;
            }
            return ret;
        }


        void rtmConnectClose() {
            serverPushProcessor.rtmConnectClose(uid);
        }
//
//        void rtmVoiceConnectClose() {
//            serverPushProcessor.rtmVoiceConnectClose();
//        }

        //----------------------[ RTM Operations ]-------------------//
        Answer ping(Quest quest, InetSocketAddress peer) {
//            Log.i("sdktest"," receive rtm ping");

            long now = Genid.getCurrentSeconds();
            lastPingTime.set(now);
            return new Answer(quest);
        }

        Answer kickout(Quest quest, InetSocketAddress peer) {
            setCloseType(CloseType.ByServer);
            rtmGate.close();
            rtcClear();
            serverPushProcessor.kickout();
            return null;
        }

        Answer kickoutRoom(Quest quest, InetSocketAddress peer) {
            long roomId = (long) quest.get("rid");
            serverPushProcessor.kickoutRoom(roomId);
            return null;
        }

        class MessageInfo {
            public boolean isBinary;
            public byte[] binaryData;
            public String message;

            MessageInfo() {
                isBinary = false;
                message = "";
                binaryData = null;
            }
        }

        //----------------------[ RTM Messagess Utilities ]-------------------//
        private TranslatedInfo processChatMessage(Quest quest, StringBuilder message) {
            Object ret = quest.want("msg");
            Map<String, String> msg = new HashMap<>((Map<String, String>) ret);
            TranslatedInfo tm = new TranslatedInfo();
            tm.source = msg.get("source");
            tm.target = msg.get("target");
            tm.sourceText = msg.get("sourceText");
            tm.targetText = msg.get("targetText");
            return tm;
        }

        private MessageInfo BuildMessageInfo(Quest quest) {
            MessageInfo info = new MessageInfo();

            Object obj = quest.want("msg");
            if (obj instanceof byte[]) {
                info.isBinary = true;
                info.binaryData = (byte[]) obj;
            } else
                info.message = (String) obj;

            return info;
        }

        Answer pushP2PRTCRequest(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));
            int pid = rtmUtils.wantInt(quest,"pid");
            lastCallId = rtmUtils.wantLong(quest,"callId");
            peerUid = rtmUtils.wantLong(quest,"peerUid");
            lastP2Ptype = rtmUtils.wantInt(quest,"type");
            serverPushProcessor.pushRequestP2PRTC(peerUid, lastP2Ptype);
            return null;
        }

        Answer pushP2PRTCEvent(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));
//            int pid = rtmUtils.wantInt(quest,"pid");
            long callId = rtmUtils.wantLong(quest,"callId");
            final long calluid = rtmUtils.wantLong(quest,"peerUid");
            final int type = rtmUtils.wantInt(quest,"type");
            int ievent = rtmUtils.wantInt(quest,"event");
//            Log.i("sdktest","receive pushP2PRTCEvent callId " + callId + " peerUid "+ peerUid +  " type " + type + " ievent " + ievent);
            final P2PRTCEvent event = P2PRTCEvent.intToEnum(ievent);

            if (event == P2PRTCEvent.Accept){
                String ret = RTCEngine.startP2P(type, calluid, callId);
                if (ret.isEmpty()){
                    Log.i("sdktest", "user " + calluid + "accept P2P " + type +" startP2P ok");
                    if (type == 2) {
                        Handler mainhandle = new Handler(Looper.getMainLooper());
                        mainhandle.post(new Runnable() {
                            @Override
                            public void run() {
                                SurfaceView view = serverPushProcessor.pushP2PRTCEvent(calluid, type, event);
                                if (view !=null)
                                {
                                    RTCEngine.bindDecodeSurface(calluid, view.getHolder().getSurface());
                                }
                            }
                        });
                    }
                    else {
                        serverPushProcessor.pushP2PRTCEvent(calluid, type, event);
                    }
                }
                else{
                    Log.i("sdktest", "Accept connect P2P rtc failed " + ret);
                    errorRecorder.recordError("pushP2PRTCEvent startP2P error " + ret);
                }
                return null;
            }
            else {
                lastCallId = 0;
                peerUid = 0;
                RTCEngine.closeP2P();
            }

            serverPushProcessor.pushP2PRTCEvent(calluid, type, event);
            return null;
        }


        //----------------------[ RTM Messagess ]-------------------//
        Answer pushmsg(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));

            long from = rtmUtils.wantLong(quest,"from");
            long to = rtmUtils.wantLong(quest,"to");
            long mid = rtmUtils.wantLong(quest,"mid");

            if (!duplicatedFilter.CheckMessage(DuplicatedMessageFilter.MessageCategories.P2PMessage, from, mid))
                return null;

            byte mtype = (byte) rtmUtils.wantInt(quest,"mtype");

            String attrs = rtmUtils.wantString(quest,"attrs");
            long mtime = rtmUtils.wantLong(quest,"mtime");

            RTMMessage userMsg = new RTMMessage();
            userMsg.attrs = attrs;
            userMsg.fromUid = from;
            userMsg.modifiedTime = mtime;
            userMsg.messageType = mtype;
            userMsg.messageId = mid;
            userMsg.toId = to;

            if (mtype == MessageType.CHAT) {
                StringBuilder orginialMessage = new StringBuilder();
                userMsg.translatedInfo = processChatMessage(quest, orginialMessage);
                serverPushProcessor.pushChat(userMsg);
                return null;
            }

            MessageInfo messageInfo = BuildMessageInfo(quest);
            if (mtype == MessageType.CMD) {
                userMsg.stringMessage = messageInfo.message;
                serverPushProcessor.pushCmd(userMsg);
            } else if (mtype >= MessageType.IMAGEFILE && mtype <= MessageType.NORMALFILE) {
                FileStruct fileInfo = new FileStruct();
                userMsg.fileInfo = fileInfo;
                String fileRecieve = quest.getString("msg");
                try {
                    JSONObject kk = new JSONObject(fileRecieve);
                    fileInfo.url = kk.optString("url");
                    fileInfo.fileSize = kk.getLong("size");
                    if (kk.has("surl"))
                        fileInfo.surl = kk.optString("surl");
                    JSONObject tt = new JSONObject(attrs);
                    if (mtype == MessageType.AUDIOFILE) {
                        if (tt.has("rtm")){
                            JSONObject rtmjson = tt.getJSONObject("rtm");
                            if (rtmjson.has("type") && rtmjson.getString("type").equals("audiomsg")) {//rtm语音消息
                                JSONObject fileAttrs = tt.getJSONObject("rtm");
                                userMsg.fileInfo.isRTMaudio = true;
                                userMsg.fileInfo.lang = fileAttrs.optString("lang");
                                userMsg.fileInfo.duration = fileAttrs.optInt("duration");
                                userMsg.fileInfo.codec = fileAttrs.optString("codec");
                                userMsg.fileInfo.srate = fileAttrs.optInt("srate");
                            }
                        }
                    }
                    String realAttrs;
                    if (tt.has("custom")) {
                        try {
                            JSONObject custtomObject = tt.getJSONObject("custom");
                            realAttrs = custtomObject.toString();
                        } catch (Exception ex) {
                            realAttrs = "";
                        }
                        userMsg.attrs = realAttrs;
                    }
                } catch (JSONException e) {
                    printLog("pushmsg parse json error " + e.getMessage());
                }
                serverPushProcessor.pushFile(userMsg);
            }
            else {
                if (messageInfo.isBinary) {
                    userMsg.binaryMessage = messageInfo.binaryData;
                    serverPushProcessor.pushMessage(userMsg);
                }
                else {
                    userMsg.stringMessage = messageInfo.message;
                    serverPushProcessor.pushMessage(userMsg);
                }
            }
            return null;
        }

        Answer pushgroupmsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long from = rtmUtils.wantLong(quest,"from");
            long groupId = rtmUtils.wantLong(quest,"gid");
            long mid = rtmUtils.wantLong(quest,"mid");

            if (!duplicatedFilter.CheckMessage(DuplicatedMessageFilter.MessageCategories.GroupMessage, from, mid, groupId))
                return null;

            byte mtype = (byte) rtmUtils.wantInt(quest,"mtype");
            String attrs = rtmUtils.wantString(quest,"attrs");
            long mtime = rtmUtils.wantLong(quest,"mtime");

            RTMMessage userMsg = new RTMMessage();
            userMsg.attrs = attrs;
            userMsg.fromUid = from;
            userMsg.modifiedTime = mtime;
            userMsg.messageType = mtype;
            userMsg.messageId = mid;
            userMsg.toId = groupId;

            if (mtype == MessageType.CHAT) {
                StringBuilder orginialMessage = new StringBuilder();
                userMsg.translatedInfo = processChatMessage(quest, orginialMessage);
                serverPushProcessor.pushGroupChat(userMsg);
                return null;
            }

            MessageInfo messageInfo = BuildMessageInfo(quest);
            if (mtype == MessageType.CMD) {
                userMsg.stringMessage = messageInfo.message;
                serverPushProcessor.pushGroupCmd(userMsg);
            }else if (mtype >= MessageType.IMAGEFILE && mtype <= MessageType.NORMALFILE) {
                FileStruct fileInfo = new FileStruct();
                userMsg.fileInfo = fileInfo;
                String fileRecieve = rtmUtils.wantString(quest,"msg");
                String fileattrs = rtmUtils.wantString(quest,"attrs");
                try {
                    JSONObject tt = new JSONObject(fileattrs);
                    JSONObject kk = new JSONObject(fileRecieve);
                    fileInfo.url = kk.optString("url");
                    fileInfo.fileSize = kk.getLong("size");
                    if (kk.has("surl"))
                        fileInfo.surl = kk.optString("surl");

                    if (mtype == MessageType.AUDIOFILE) {
                        if (tt.has("rtm")){
                            JSONObject rtmjson = tt.getJSONObject("rtm");
                            if (rtmjson.has("type") && rtmjson.getString("type").equals("audiomsg")) {//rtm语音消息
                                JSONObject fileAttrs = tt.getJSONObject("rtm");
                                userMsg.fileInfo.isRTMaudio = true;
                                userMsg.fileInfo.lang = fileAttrs.optString("lang");
                                userMsg.fileInfo.duration = fileAttrs.optInt("duration");
                                userMsg.fileInfo.codec = fileAttrs.optString("codec");
                                userMsg.fileInfo.srate = fileAttrs.optInt("srate");
                            }
                        }
                    }
                    String realAttrs;
                    if (tt.has("custom")) {
                        try {
                            JSONObject custtomObject = tt.getJSONObject("custom");
                            realAttrs = custtomObject.toString();
                        } catch (JSONException ex) {
                            realAttrs = "";
                        }
                        userMsg.attrs = realAttrs;
                    }
                } catch (JSONException e) {
                    printLog("pushgroupmsg parse json error " + e.getMessage());
                }
                serverPushProcessor.pushGroupFile(userMsg);
            }else {
                if (messageInfo.isBinary) {
                    userMsg.binaryMessage = messageInfo.binaryData;
                    serverPushProcessor.pushGroupMessage(userMsg);
                }
                else {
                    userMsg.stringMessage = messageInfo.message;
                    serverPushProcessor.pushGroupMessage(userMsg);
                }
            }
            return null;
        }

        Answer pushroommsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long from = rtmUtils.wantLong(quest,"from");
            long roomId = rtmUtils.wantLong(quest,"rid");
            long mid = rtmUtils.wantLong(quest,"mid");

            if (!duplicatedFilter.CheckMessage(DuplicatedMessageFilter.MessageCategories.RoomMessage, from, mid, roomId))
                return null;

            byte mtype = (byte) rtmUtils.wantInt(quest,"mtype");
            String attrs = rtmUtils.wantString(quest,"attrs");
            long mtime = rtmUtils.wantLong(quest,"mtime");

            RTMMessage userMsg = new RTMMessage();
            userMsg.attrs = attrs;
            userMsg.fromUid = from;
            userMsg.modifiedTime = mtime;
            userMsg.messageType = mtype;
            userMsg.messageId = mid;
            userMsg.toId = roomId;

            if (mtype == MessageType.CHAT) {
                StringBuilder orginialMessage = new StringBuilder();
                userMsg.translatedInfo = processChatMessage(quest, orginialMessage);
                serverPushProcessor.pushRoomChat(userMsg);
                return null;
            }

            MessageInfo messageInfo = BuildMessageInfo(quest);
            if (mtype == MessageType.CMD) {
                userMsg.stringMessage = messageInfo.message;
                serverPushProcessor.pushRoomCmd(userMsg);
            }else if (mtype >= MessageType.IMAGEFILE && mtype <= MessageType.NORMALFILE) {
                FileStruct fileInfo = new FileStruct();
                userMsg.fileInfo = fileInfo;

                String fileRecieve = rtmUtils.wantString(quest,"msg");
                String fileattrs = rtmUtils.wantString(quest,"attrs");
                try {
                    JSONObject tt = new JSONObject(fileattrs);
                    JSONObject kk = new JSONObject(fileRecieve);
                    fileInfo.url = kk.getString("url");
                    fileInfo.fileSize = kk.getLong("size");
                    if (kk.has("surl"))
                        fileInfo.surl = kk.getString("surl");

                    if (mtype == MessageType.AUDIOFILE) {
                        if (tt.has("rtm")){
                            JSONObject rtmjson = tt.getJSONObject("rtm");
                            if (rtmjson.has("type") && rtmjson.optString("type").equals("audiomsg")) {//rtm语音消息
                                JSONObject fileAttrs = tt.getJSONObject("rtm");
                                userMsg.fileInfo.isRTMaudio = true;
                                userMsg.fileInfo.lang = fileAttrs.optString("lang");
                                userMsg.fileInfo.duration = fileAttrs.optInt("duration");
                                userMsg.fileInfo.codec = fileAttrs.optString("codec");
                                userMsg.fileInfo.srate = fileAttrs.optInt("srate");
                            }
                        }
                    }
                    String realAttrs;
                    if (tt.has("custom")) {
                        try {
                            JSONObject custtomObject = tt.getJSONObject("custom");
                            realAttrs = custtomObject.toString();
                        } catch (JSONException ex) {
                            realAttrs = "";
                        }
                        userMsg.attrs = realAttrs;
                    }
                } catch (JSONException e) {
                    errorRecorder.recordError("pushroommsg parse json error " + e.getMessage());
                }
                serverPushProcessor.pushRoomFile(userMsg);
            }else {
                if (messageInfo.isBinary) {
                    userMsg.binaryMessage = messageInfo.binaryData;
                    serverPushProcessor.pushRoomMessage(userMsg);
                }
                else {
                    userMsg.stringMessage = messageInfo.message;
                    serverPushProcessor.pushRoomMessage(userMsg);
                }
            }
            return null;
        }

        Answer pushbroadcastmsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long from = rtmUtils.wantLong(quest,"from");
            long mid = rtmUtils.wantLong(quest,"mid");

            if (!duplicatedFilter.CheckMessage(DuplicatedMessageFilter.MessageCategories.BroadcastMessage, from, mid))
                return null;

            byte mtype = (byte) rtmUtils.wantInt(quest,"mtype");
            String attrs = rtmUtils.wantString(quest,"attrs");
            long mtime = rtmUtils.wantLong(quest,"mtime");

            RTMMessage userMsg = new RTMMessage();
            userMsg.attrs = attrs;
            userMsg.fromUid = from;
            userMsg.modifiedTime = mtime;
            userMsg.messageType = mtype;
            userMsg.messageId = mid;

            if (mtype == MessageType.CHAT) {
                StringBuilder orginialMessage = new StringBuilder();
                userMsg.translatedInfo = processChatMessage(quest, orginialMessage);
                serverPushProcessor.pushBroadcastChat(userMsg);
                return null;
            }

            MessageInfo messageInfo = BuildMessageInfo(quest);
            if (mtype == MessageType.CMD) {
                userMsg.stringMessage = messageInfo.message;
                serverPushProcessor.pushBroadcastCmd(userMsg);
            } else if (mtype >= MessageType.IMAGEFILE && mtype <= MessageType.NORMALFILE) {
                FileStruct fileInfo = new FileStruct();
                userMsg.fileInfo = fileInfo;

                String fileRecieve = rtmUtils.wantString(quest,"msg");
                String fileattrs = rtmUtils.wantString(quest,"attrs");
                try {
                    JSONObject tt = new JSONObject(fileattrs);
                    JSONObject kk = new JSONObject(fileRecieve);
                    fileInfo.url = kk.optString("url");
                    fileInfo.fileSize = kk.getLong("size");
                    if (kk.has("surl"))
                        fileInfo.surl = kk.optString("surl");

                    if (mtype == MessageType.AUDIOFILE) {
                        if (tt.has("rtm")){
                            JSONObject rtmjson = tt.getJSONObject("rtm");
                            if (rtmjson.has("type") && rtmjson.optString("type").equals("audiomsg")) {//rtm语音消息
                                JSONObject fileAttrs = tt.getJSONObject("rtm");
                                userMsg.fileInfo.isRTMaudio = true;
                                userMsg.fileInfo.lang = fileAttrs.optString("lang");
                                userMsg.fileInfo.duration = fileAttrs.optInt("duration");
                                userMsg.fileInfo.codec = fileAttrs.optString("codec");
                                userMsg.fileInfo.srate = fileAttrs.optInt("srate");
                            }
                        }
                    }
                    String realAttrs;
                    if (tt.has("custom")) {
                        try {
                            JSONObject custtomObject = tt.getJSONObject("custom");
                            realAttrs = custtomObject.toString();
                        } catch (JSONException ex) {
                            realAttrs = "";
                        }
                        userMsg.attrs = realAttrs;
                    }
                } catch (JSONException e) {
                    errorRecorder.recordError("pushbroadcastmsg parse json error " + e.getMessage());
                }
                serverPushProcessor.pushBroadcastFile(userMsg);
            }else {
                if (messageInfo.isBinary) {
                    userMsg.binaryMessage =  messageInfo.binaryData;
                    serverPushProcessor.pushBroadcastMessage(userMsg);
                }
                else {
                    userMsg.stringMessage = messageInfo.message;
                    serverPushProcessor.pushBroadcastMessage(userMsg);
                }
            }
            return null;
        }


        //-------------RTC message--------------//
        Answer pushEnterRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long roomId = rtmUtils.wantLong(quest,"rid");
            long userId = rtmUtils.wantLong(quest,"uid");
            long time = rtmUtils.wantLong(quest,"mtime");
            serverPushProcessor.pushEnterRTCRoom(roomId,  userId, time);
            return null;
        }


        Answer pushExitRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long roomId = rtmUtils.wantLong(quest,"rid");
            long userId = rtmUtils.wantLong(quest,"uid");
            long time = rtmUtils.wantLong(quest,"mtime");
            RTCEngine.userLeave(userId);
            serverPushProcessor.pushExitRTCRoom(roomId,  userId, time);
            return null;
        }

        Answer pushRTCRoomClosed(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long roomId = rtmUtils.wantLong(quest,"rid");
            rtcClear();
            serverPushProcessor.pushRTCRoomClosed(roomId);
            return null;
        }

        Answer pushInviteIntoRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long roomId = rtmUtils.wantLong(quest,"rid");
            long userId = rtmUtils.wantLong(quest,"fromUid");
            serverPushProcessor.pushInviteIntoRTCRoom(roomId, userId);

            return null;
        }

        Answer pushPullIntoRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            final long roomId = rtmUtils.wantLong(quest,"rid");
            enterRTCRoom(new IRTMCallback<RoomInfo>() {
                @Override
                public void onResult(RoomInfo info ,RTMAnswer answer) {
                    serverPushProcessor.pushPullRoom(roomId,info);
                }
            },roomId);
            return null;
        }

        Answer pushKickOutRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            long roomId = rtmUtils.wantLong(quest,"rid");
            rtcClear();
            serverPushProcessor.pushKickoutRTCRoom(roomId);
            return null;
        }
        Answer pushAdminCommand(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            int command = rtmUtils.wantInt(quest,"command");
            HashSet<Long> uids = rtmUtils.wantLongHashSet(quest,"uids");
            serverPushProcessor.pushAdminCommand(command, uids);
            return null;
        }
    }

    void  internalReloginCompleted(final long uid, final boolean successful, final int reloginCount){
        serverPushProcessor.reloginCompleted(uid, successful, lastReloginAnswer, reloginCount);
//        if (videoInitFlag.get() && videoRoom.get()>0){
//            if (!successful){
//            }
//        }
    }

    void reloginEvent(int count){
        if (noNetWorkNotify.get()) {
            isRelogin.set(false);
            internalReloginCompleted(uid, false, count);
            return;
        }
//        isRelogin.set(true);
        int num = count;
        Map<String, String> kk = loginAttrs;
        if (serverPushProcessor.reloginWillStart(uid, num)) {
            lastReloginAnswer = login(token, lang, kk);
            if(lastReloginAnswer.errorCode == okRet || lastReloginAnswer.errorCode == RTMErrorCode.RTM_EC_DUPLCATED_AUTH.value()) {
                isRelogin.set(false);
                internalReloginCompleted(uid, true, num);
                return;
            }
            else {
                if (finishCodeList.contains(lastReloginAnswer.errorCode)){
                    isRelogin.set(false);
                    internalReloginCompleted(uid, false, num);
                    return;
                }
                else {
                    if (num >= serverPushProcessor.internalReloginMaxTimes){
                        isRelogin.set(false);
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    if (!isRelogin.get()) {
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        isRelogin.set(false);
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    reloginEvent(++num);
                }
            }
        }
        else {
            isRelogin.set(false);
            internalReloginCompleted(uid, false, --num);
        }
    }

    public void onNetChange(int netWorkState){
        if (lastNetType != NetUtils.NETWORK_NOTINIT) {
            switch (netWorkState) {
                case NetUtils.NETWORK_NONE:
                    noNetWorkNotify.set(true);
                    Log.e("sdktest","no network");
//                    if (isRelogin.get()){
//                        isRelogin.set(false);
//                    }
                    break;
                case NetUtils.NETWORK_MOBILE:
                case NetUtils.NETWORK_WIFI:
//                    Log.e("sdktest","have network");

                    if (rtmGate == null)
                        return;
                    noNetWorkNotify.set(false);
                    if (lastNetType != netWorkState && autoConnect) {
                        if (isRelogin.get())
                            return;

                        isRelogin.set(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (getClientStatus() == ClientStatus.Connected){
                                    Quest quest = new Quest("bye");
                                    sendQuest(quest, new FunctionalAnswerCallback() {
                                        @Override
                                        public void onAnswer(Answer answer, int errorCode) {
                                            close();
                                            try {
                                                Thread.sleep(200);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            reloginEvent(1);
                                        }
                                    }, 5);
                                }
                                else {
//                                    voiceClose();
                                    reloginEvent(1);
                                }
                            }
                        }).start();
                    }
                    break;
            }
        }
        lastNetType = netWorkState;
    }

    public  void setServerPushProcessor(RTMPushProcessor processor){
        this.serverPushProcessor = processor;
    }

    void RTMInit(String rtmendpoint, String rtcendpoint, long pid, long uid, RTMPushProcessor serverPushProcessor, final Activity currentActivity, RTMConfig config) {
        if (config == null)
            rtmConfig = new RTMConfig();
        else
            rtmConfig = config;

        errorRecorder = rtmConfig.defaultErrorRecorder;
        rtmUtils.errorRecorder = errorRecorder;
        this.rtmEndpoint = rtmendpoint;
        this.rtcEndpoint = rtcendpoint;

        String errDesc = "";
        if (rtmEndpoint == null || rtmEndpoint.equals(""))
            errDesc = "invalid rtmEndpoint:" + rtmEndpoint;
        if (pid <= 0)
            errDesc += " pid is invalid:" + pid;
        if (uid <= 0)
            errDesc += " uid is invalid:" + uid;
        if (serverPushProcessor == null)
            errDesc += " RTMPushProcessor is null";

        if (!errDesc.equals("")) {
            printLog("rtmclient init error " + errDesc);
            return;
        }

//        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
//            apiSelect = 2;



        this.pid = pid;
        this.uid = uid;
        isRelogin.set(false);
        fileGates = new HashMap<>();
        processor = new RTMQuestProcessor();
        this.serverPushProcessor = serverPushProcessor;
        autoConnect = rtmConfig.autoConnect;
        this.currentActivity = new WeakReference<Activity>(currentActivity);


        application = currentActivity.getApplication();
        ClientEngine.setMaxThreadInTaskPool(RTMConfig.globalMaxThread);
        application.registerActivityLifecycleCallbacks(this);

        if (autoConnect) {
            if (currentActivity == null){
                printLog("currentActivity is null ");
                return;
            }
            context = currentActivity.getApplicationContext();

            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            //网络监听
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            context.registerReceiver(this, intentFilter);
        }
    }

    public void setErrorRecoder(ErrorRecorder value){
        if (value == null)
            return;
        errorRecorder = value;
        rtmUtils.errorRecorder = errorRecorder;
    }

    public void enableEncryptorByDerData(String curve, byte[] peerPublicKey) {
        this.curve = curve;
        encrptyData = peerPublicKey;
    }

    public void enableEncryptorByDerFile(String curve, String file) {
        this.curve = curve;
        try {
            FileInputStream fis = new FileInputStream(new File(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            encrptyData = baos.toByteArray();
            fis.close();
            baos.close();
        } catch (Exception e) {
            printLog("RTMInit error " + e.getMessage());
        }
    }

    long getPid() {
        return pid;
    }

    long getUid() {
        return uid;
    }

    RTMAnswer  initRTC(boolean stereo) {
        //        manager = (CameraManager) application.getSystemService(Context.CAMERA_SERVICE);
//        backgroundThread = new HandlerThread("imageAvailableListener");
//        backgroundThread.start();
//        backgroundHandler = new Handler(backgroundThread.getLooper());
//
//        HandlerThread imageHandlerThread = new HandlerThread("image");
//        imageHandlerThread.start();
//        imageHandle = new Handler(imageHandlerThread.getLooper());
//        if (mPreBuffer == null) {
//            mPreBuffer = new byte[Video_Height * Video_Height *3 /2];
//        }
        mOrEventListener = new OrientationEventListener(currentActivity.get()) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
                    rotation = 0;
                } else if ((rotation > 70) && (rotation <= 110)) {
                    rotation = 90;
                } else if ((rotation > 160) && (rotation <= 200)) {
                    rotation = 180;
                } else if ((rotation > 250) && (rotation <= 290)) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                RTCEngine.setRotation(rotation);
            }
        };
        int errCode = RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value();
        if (rtmGateStatus != ClientStatus.Connected) {
            return genRTMAnswer(errCode, "you must RTMlogin sucessfully at first");
        }
        rtcClear();
        String ret = RTCEngine.create(this, rtcEndpoint, stereo, currVideoLevel, pid, uid, application);
        if (!ret.isEmpty()) {
            return genRTMAnswer(errCode,"initRTC create error " + ret);
        }

        return genRTMAnswer(okRet);
    }


    synchronized protected ClientStatus getClientStatus() {
        synchronized (interLocker) {
            return rtmGateStatus;
        }
    }

    private boolean connectionIsAlive() {
        return processor.ConnectionIsAlive();
    }

    RTMAnswer genRTMAnswer(int errCode){
        return genRTMAnswer(errCode,"");
    }

    RTMAnswer genRTMAnswer(int errCode,String msg)
    {
        RTMAnswer tt = new RTMAnswer();
        tt.errorCode = errCode;
        if (msg == null || msg.isEmpty())
            tt.errorMsg = RTMErrorCode.getMsg(errCode);
        else
            tt.errorMsg = msg;
        return tt;
    }

    private TCPClient getCoreClient() {
        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected)
                return rtmGate;
            else
                return null;
        }
    }


    RTMAnswer genRTMAnswer(Answer answer) {
        if (answer == null)
            return new RTMAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        return new RTMAnswer(answer.getErrorCode(),answer.getErrorMessage());
    }



    RTMAnswer genRTMAnswer(Answer answer, String msg) {
        if (answer == null)
            return new RTMAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        return new RTMAnswer(answer.getErrorCode(),answer.getErrorMessage() + " " + msg);
    }


    RTMAnswer genRTMAnswer(Answer answer,int errcode) {
        if (answer == null && errcode !=0) {
            if (errcode == ErrorCode.FPNN_EC_CORE_TIMEOUT.value())
                return new RTMAnswer(errcode, "FPNN_EC_CORE_TIMEOUT");
            else
                return new RTMAnswer(errcode,"fpnn  error");
        }
        else
            return new RTMAnswer(answer.getErrorCode(),answer.getErrorMessage());
    }

    void setCloseType(CloseType type)
    {
        closedCase = type;
    }

    void sayBye(final IRTMEmptyCallback callback) {
        closedCase = CloseType.ByUser;
        final TCPClient client = getCoreClient();
        if (client == null) {
            close();
            return;
        }
        Quest quest = new Quest("bye");
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                close();
                callback.onResult(genRTMAnswer(answer,errorCode));
            }
        }, 5);
    }

    void realClose(){
        closedCase = CloseType.ByUser;
        try {
            if (autoConnect && context != null)
                context.unregisterReceiver(this);
            if (application != null)
                application.unregisterActivityLifecycleCallbacks(this);

        } catch (IllegalArgumentException e){
        }
        close();
        RTCEngine.delete();
    }

    void sayBye(boolean async) {
        closedCase = CloseType.ByUser;
        final TCPClient client = getCoreClient();
        if (client == null) {
            close();
            return;
        }
        Quest quest = new Quest("bye");
        if (async) {
            sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    close();
                }
            }, 5);
        } else {
            try {
                client.sendQuest(quest,5);
                close();
            } catch (InterruptedException e) {
                close();
            }
        }
    }

    void sendFileQuest(Quest quest, final FunctionalAnswerCallback callback) {
        sendQuest(quest, callback, rtmConfig.globalFileQuestTimeoutSeconds);
    }

    void sendQuest(Quest quest, final FunctionalAnswerCallback callback) {
        sendQuest(quest, callback, rtmConfig.globalQuestTimeoutSeconds);
    }

    Answer sendFileQuest(Quest quest) {
        return sendQuest(quest,rtmConfig.globalFileQuestTimeoutSeconds);
    }

    Answer sendQuest(Quest quest) {
        return sendQuest(quest,rtmConfig.globalQuestTimeoutSeconds);
    }

    Answer sendQuest(Quest quest, int timeout) {
        Answer answer = new Answer(new Quest(""));
        TCPClient client = getCoreClient();
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        }else {
            try {
                answer = client.sendQuest(quest, timeout);
            } catch (Exception e) {
                if (errorRecorder != null)
                    errorRecorder.recordError(e);
                answer = new Answer(quest);
                answer.fillErrorInfo(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(), e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return answer;
    }

    void sendQuest(Quest quest, final FunctionalAnswerCallback callback, int timeout) {
        TCPClient client = getCoreClient();
        final Answer answer = new Answer(quest);
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(),"invalid connection");
            callback.onAnswer(answer,answer.getErrorCode());//当前线程
            return;
        }
        if (timeout <= 0)
            timeout = rtmConfig.globalQuestTimeoutSeconds;
        try {
            client.sendQuest(quest, callback, timeout);
        }
        catch (Exception e){
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),e.getMessage());
            callback.onAnswer(answer, answer.getErrorCode());
        }
    }

    void sendQuestEmptyCallback(final IRTMEmptyCallback callback, Quest quest) {
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                callback.onResult(genRTMAnswer(answer,errorCode));
            }
        }, rtmConfig.globalQuestTimeoutSeconds);
    }

    RTMAnswer sendQuestEmptyResult(Quest quest){
        Answer ret =  sendQuest(quest);
        if (ret == null)
            return genRTMAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(),"invalid connection");
        return genRTMAnswer(ret);
    }

    void activeFileGateClient(String endpoint, final TCPClient client) {
        synchronized (interLocker) {
            if (fileGates.containsKey(endpoint)) {
                if (fileGates.get(endpoint) != null)
                    fileGates.get(endpoint).put(client, Genid.getCurrentSeconds());
            }
            else
                fileGates.put(endpoint, new HashMap<TCPClient, Long>() {{
                    put(client, Genid.getCurrentSeconds());
                }});
        }
    }

    TCPClient fecthFileGateClient(String endpoint) {
        synchronized (interLocker) {
            if (fileGates.containsKey(endpoint)) {
                if(fileGates.get(endpoint) != null)
                    for (TCPClient client : fileGates.get(endpoint).keySet())
                        return client;
            }
        }
        return null;
    }

    private void checkRoutineInit() {
        if (initCheckThread.get() || !running.get())
            return;

        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running.get()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        synchronized (interLocker) {
                            rtmGateStatus = ClientStatus.Closed;
                        }
                        return;
                    }

                    synchronized (interLocker) {
                        if (rtmGateStatus != ClientStatus.Closed && !connectionIsAlive()) {
                            closedCase = CloseType.Timeout;
                            close();
                        }
                    }
                }
            }
        });
        checkThread.setName("RTM.ThreadCheck");
        checkThread.setDaemon(true);
        checkThread.start();

        initCheckThread.set(true);
        running.set(true);
    }

    public static byte[] getBytes(int data)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    public static byte[] getBytes(long data)
    {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        bytes[4] = 0;
        bytes[5] = 0;
        bytes[6] = 0;
        bytes[7] = 0;
        return bytes;
    }


    public void whoSpeak(long[] uids){
        serverPushProcessor.voiceSpeak(uids);
    }

    public void printLog(String msg){
        Log.e("sdktest",msg);
        errorRecorder.recordError(msg);
    }

    boolean isAirplaneModeOn() {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,0) != 0;
    }

    boolean isNetWorkConnected() {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeInfo = cm.getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isAvailable() && activeInfo.isConnected())
                isConnected = true;
        }
        return isConnected;
    }


     boolean isHeadsetOn() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return mAudioManager.isWiredHeadsetOn() || mAudioManager.isBluetoothScoOn() || mAudioManager.isBluetoothA2dpOn();
        } else {
            AudioDeviceInfo[] devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            for (int i = 0; i < devices.length; i++) {
                AudioDeviceInfo device = devices[i];

                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean setmode (boolean usespeaker){
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (isHeadsetOn()){
            return false;
        }
        if (usespeaker){
            mAudioManager.setSpeakerphoneOn(true);
        }
        else
        {
            mAudioManager.setSpeakerphoneOn(false);
        }

        return true;
    }


    public boolean setmode (boolean usespeaker, boolean special){
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (isHeadsetOn()){
            return false;
        }
        if (usespeaker){
            mAudioManager.setSpeakerphoneOn(true);
        }
        else
        {
           mAudioManager.setSpeakerphoneOn(false);
        }

        return true;
    }


    public void switchmode(){

    }
//    public boolean setmode(boolean usejavamode, boolean start){
////        printLog("current mode " + mAudioManager.getMode());
//        if (isHeadsetOn()){
//            return false;
//        }
//
//        if (usejavamode){
//            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        }
//        if (start){
//            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
////            mAudioManager.setSpeakerphoneOn(true);
//        }
//        else
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//        return true;
//    }

    //-------------[ Auth(Login) utilies functions ]--------------------------//
    private void ConfigRtmGateClient(final TCPClient client) {
        client.setQuestTimeout(rtmConfig.globalQuestTimeoutSeconds);

        if (encrptyData != null && curve!=null && !curve.equals(""))
            client.enableEncryptorByDerData(curve, encrptyData);

        if (errorRecorder != null)
            client.setErrorRecorder(errorRecorder);

        client.setQuestProcessor(processor, "com.rtcsdk.RTMCore$RTMQuestProcessor");
//        client.setConnectedCallback(new ConnectionConnectedCallback() {
//            @Override
//            public void connectResult(InetSocketAddress peerAddress, int _connectionId,boolean connected) {
//                connectionId = _connectionId;
//            }
//        });
        client.setWillCloseCallback(new ConnectionWillCloseCallback() {
            @Override
            public void connectionWillClose(InetSocketAddress peerAddress, int _connectionId,boolean causedByError) {
//                printLog("closedCase " + closedCase + " getClientStatus() " + getClientStatus());
                if (connectionId.get() != 0 && connectionId.get() == _connectionId && closedCase != CloseType.ByUser && getClientStatus() != ClientStatus.Connecting) {

                    close();
                    processor.rtmConnectClose();

                    if (!autoConnect) {
                        return;
                    }
                    else {
                        if (closedCase == CloseType.ByServer || isRelogin.get()) {
                            return;
                        }

                        if (isAirplaneModeOn()) {
                            return;
                        }

                        if(getClientStatus() == ClientStatus.Closed){
                            try {
                                Thread.sleep(2* 1000);//处理一些特殊情况
                                if (noNetWorkNotify.get()) {
                                    return;
                                }
                                if (isRelogin.get() || getClientStatus() == ClientStatus.Connected) {
                                    return;
                                }
                                isRelogin.set(true);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        reloginEvent(1);
                                    }
                                }).start();
                            }
                            catch (Exception e){
                                printLog(" relogin error " + e.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    //------------voice add---------------//
    private RTMAnswer auth(String token, Map<String, String> attr) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        qt.param("version", "Android-Voice-" + RTMConfig.SDKVersion);

        if (attr != null)
            qt.param("attrs", attr);
        try {
            Answer answer = rtmGate.sendQuest(qt);

            if (answer  == null || answer.getErrorCode() != okRet) {
                closeStatus();
                return genRTMAnswer(answer,"when send auth endpoint:" + rtmGate.getAddres().toString());
            }
            else if (!rtmUtils.wantBoolean(answer,"ok")) {
                closeStatus();
                return genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "auth failed token maybe expired");
            }
            else {
                synchronized (interLocker) {
                    rtmGateStatus = ClientStatus.Connected;
                }
                processor.setLastPingTime(Genid.getCurrentSeconds());
                checkRoutineInit();
                connectionId.set(rtmGate.getConnectionId());
                return genRTMAnswer(answer);
            }
        }
        catch (Exception  ex){
            closeStatus();
            return genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),ex.getMessage());
        }
    }

    private void auth(final IRTMEmptyCallback callback, final String token, final Map<String, String> attr) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        qt.param("version", "Android-" + rtmConfig.SDKVersion);
        if (attr != null)
            qt.param("attrs", attr);

        rtmGate.sendQuest(qt, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                try {
                    if (errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        closeStatus();
                        if (answer == null)
                            callback.onResult(genRTMAnswer( errorCode, "when send auth to rtmgate orgnall endpoint:" + rtmEndpoint));
                        else
                            callback.onResult(genRTMAnswer( errorCode, "when send auth to rtmgate " + answer.getErrorMessage() +  " orgnall endpoint:" + rtmEndpoint));
                    } else if (!rtmUtils.wantBoolean(answer,"ok")) {
                        closeStatus();
                        callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "auth failed token maybe expired"));
                    } else {
                        synchronized (interLocker) {
                            rtmGateStatus = ClientStatus.Connected;
                        }
                        processor.setLastPingTime(Genid.getCurrentSeconds());
                        checkRoutineInit();
                        connectionId.set(rtmGate.getConnectionId());
                        callback.onResult(genRTMAnswer(errorCode));
                    }
                }
                catch (Exception e){
                    callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"when auth " + e.getMessage()));
                }
            }
        }, 0);
    }

    void login(final IRTMEmptyCallback callback, final String token, final String lang, final Map<String, String> attr) {
        if (token ==null || token.isEmpty()){
            callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value()," token  is null or empty"));
            return;
        }

        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_OK.value()));
                    }
                }).start();
                return;
            }
            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            auth(callback, token, attr);
        } else {
            try {
                rtmGate = TCPClient.create(rtmEndpoint);
                rtmGate.setErrorRecorder(errorRecorder);
            }
            catch (IllegalArgumentException ex){
                callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"create rtmgate error endpoint Illegal:" +ex.getMessage() + " :" +  rtmEndpoint ));
                return;
            }
            catch (Exception e){
                String msg = "create rtmgate error orginal error:" + e.getMessage() + " endpoint: " + rtmEndpoint;
                if (rtmGate != null)
                    msg = msg + " parse endpoint " + rtmGate.endpoint();
                callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),msg ));
                return;
            }
            this.token = token;
            if (lang == null)
                this.lang = "";
            else
                this.lang = lang;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            auth(callback, token, attr);
        }
    }

    private  void closeStatus()
    {
        synchronized (interLocker) {
            rtmGateStatus = ClientStatus.Closed;
        }
    }

    RTMAnswer login(String token, String lang, Map<String, String> attr) {
        if (token == null || token.isEmpty())
            return genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "login failed token  is null or empty");

        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting)
                return genRTMAnswer(ErrorCode.FPNN_EC_OK.value());

            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            return auth(token, attr);
        } else {
            try {
                rtmGate = TCPClient.create(rtmEndpoint);
                rtmGate.setErrorRecorder(errorRecorder);
            }
            catch (IllegalArgumentException ex){
                return genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"create rtmgate error endpoint Illegal:" +ex.getMessage() + " :" +  rtmEndpoint );
            }
            catch (Exception e){
                String msg = "create rtmgate error orginal error:" + e.getMessage() + " endpoint: " + rtmEndpoint;
                if (rtmGate != null)
                    msg = msg + " parse endpoint " + rtmGate.endpoint();
                return genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),msg );
            }
            if (lang == null)
                this.lang = "";
            else
                this.lang = lang;
            this.token =  token;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            return auth(token, attr);
        }
    }

    public void close() {
        synchronized (interLocker) {
            initCheckThread.set(false);
            running.set(false);
            fileGates.clear();
            if (rtmGateStatus == ClientStatus.Closed)
                return;
            rtmGateStatus = ClientStatus.Closed;
        }
        if (rtmGate !=null)
            rtmGate.close();
        RTCEngine.RTCClear();
    }

//    void voiceDispose(){
//        RTCEngine.setVoiceFlag(false);
//        pause.set(false);
//        RTCEngine.canSpeak(false);
//        activityRoom.set(-1);
//
//        synchronized (rtcLocker) {
//            for (Long rid: roomInfos.keySet()){
//                if (rtmGateStatus ==  ClientStatus.Connected){
//                    leaveRTCRooms();
//                }
//            }
//            roomInfos.clear();
//        }
//    }

//    void videoDispose(){
//        pause.set(false);
//        videoRoom = 0;
//        RTCEngine.stopVideo(true);
//    }
//
}