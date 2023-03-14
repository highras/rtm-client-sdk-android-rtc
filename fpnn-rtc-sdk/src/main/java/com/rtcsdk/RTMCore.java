package com.rtcsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HttpsURLConnection;

import static com.fpnn.sdk.ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION;

class RTMCore extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks{


    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                        printLog("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
                        RTCEngine.audioFocusFlag(false);

                        // Pause playback because your Audio Focus was
                        // temporarily stolen, but will be back soon.
                        // i.e. for a phone call
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        errorRecorder.recordError("AudioManager.AUDIOFOCUS_LOSS");
                        RTCEngine.audioFocusFlag(false);

                        // Stop playback, because you lost the Audio Focus.
                        // i.e. the user started some other playback app
                        // Remember to unregister your controls/buttons here.
                        // And release the kra — Audio Focus!
                        // You’re done.
                        mAudioManager.abandonAudioFocus(afChangeListener);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//                        printLog("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                        // Lower the volume, because something else is also
                        // playing audio over you.
                        // i.e. for notifications or navigation directions
                        // Depending on your audio playback, you may prefer to
                        // pause playback here instead. You do you.
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                        printLog("AudioManager.AUDIOFOCUS_GAIN");
                        RTCEngine.audioFocusFlag(true);
//                        printLog("AudioManager.AUDIOFOCUS_GAIN resumeAudioFocus ret " + ret);

                        // Resume playback, because you hold the Audio Focus
                        // again!
                        // i.e. the phone call ended or the nav directions
                        // are finished
                        // If you implement ducking and lower the volume, be
                        // sure to return it to normal here, as well.
                    }
                }
            };

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
    SharedPreferences addressSp;


    @Override
    public void onReceive(Context context, Intent intent) {
        String b= ConnectivityManager.CONNECTIVITY_ACTION;
        String a= intent.getAction();
        if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(a)) {
            if (!isInitRTC)
                return;
            final int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.STATE", Integer.MIN_VALUE);
            if (intExtra == 2 || intExtra == 0) {//2-连接 0-断开
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        RTCEngine.headsetStat(intExtra);
                    }
                }, 1000L);
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
            if (!isInitRTC)
                return;
            if (intent.hasExtra("state")){
                final int ret = intent.getIntExtra("state", 0);
                Log.e("sdktest", "ACTION_HEADSET_PLUG " + ret);
                if (ret ==0 || ret == 1){//0-拔出 1-插入
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                            RTCEngine.headsetStat(ret);
                        }
                    }, 500L);
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
    private long logints;
    private String curve;
    private String rtmEndpoint;
    private String rtcEndpoint;
    private Context context;
    private Application application;
    private WeakReference<Activity> currentActivity;
    private boolean background = false;
    private byte[] encrptyData;
    private String endpoint;
    boolean cameraStatus = false;
    private boolean isInitRTC = false;
    OrientationEventListener mOrEventListener;
    int currVideoLevel = CaptureLevle.MIddle.value();

    long lastCallId = 0; //p2pRTC 用
    int lastP2Ptype = 0; //1-音频 2-视频
    long peerUid = 0; //p2p对方uid

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


    private ArrayList<Integer> finishCodeList = new ArrayList<Integer>(){{
        add(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value());
        add(RTMErrorCode.RTM_EC_PROJECT_BLACKUSER.value()); }};

    //voice
//    private int mFinalCount = 0;
    @Override
    public void onActivityCreated( Activity activity,  Bundle bundle) {
//        printLog("onActivityCreated");
    }

    public void setServerPush(RTMPushProcessor jj){
        serverPushProcessor = jj;
    }

    @Override
    public void onActivityStarted( Activity activity) {
        this.currentActivity = new WeakReference<Activity>(activity);
        if (this.background && !activity.isChangingConfigurations()) {
            this.background = false;
            if (isInitRTC)
                RTCEngine.setBackground(false);
        }
    }

    @Override
    public void onActivityResumed( Activity activity) {
    }

    @Override
    public void onActivityPaused( Activity activity) {
    }

    @Override
    public void onActivityStopped( Activity activity) {
        if (!this.background && (this.currentActivity == null || activity == this.currentActivity.get()) && !activity.isChangingConfigurations()) {
            this.background = true;
            if (isInitRTC)
                RTCEngine.setBackground(true);
        }
    }



    @Override
    public void onActivitySaveInstanceState( Activity activity,  Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed( Activity activity) {
    }

    public  static AudioManager mAudioManager;
//    AtomicBoolean pause = new AtomicBoolean(false);
    final Object videoLocker =  new Object();
    private int voiceConnectionId = 0;

    void enterRTCRoom( final IRTMEmptyCallback callback, final long roomId, final String lang) {

        if (initRTC().errorCode != okRet){
            callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"init RTC error"));
            return;
        }

        if (lastCallId > 0){
            callback.onResult(genRTMAnswer(voiceError, "in p2pRTC type"));
            return;
        }

        Quest quest = new Quest("enterRTCRoom");
        quest.param("rid", roomId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    String roomtoken = rtmUtils.wantString(answer,"token");
                    int type = rtmUtils.wantInt(answer,"type");
                    if (type == 2 || type == 3){ //视频房间或者带翻译的房间
                        if (RTCEngine.isInRTCRoom() > 0){
                            callback.onResult(genRTMAnswer(voiceError, "enter video Room error you are in rtcroom-" + RTCEngine.isInRTCRoom() ));
                            return;
                        }
                    }
                    enterRTCRoomReal(callback, roomId, roomtoken, type, lang);
                } else {
                    callback.onResult(genRTMAnswer(answer, errorCode));
                }
            }
        });
    }

    void createRTCRoom(final long roomId, final int roomType, int enableRecord, final String language, final IRTMEmptyCallback callback) {
        if (initRTC().errorCode != okRet){
            callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"init RTC error"));
            return;
        }

        if (lastCallId > 0){
            callback.onResult(genRTMAnswer(voiceError, "in p2pRTC type"));
            return;
        }

        if ((roomType == 2 || roomType == 3) && RTCEngine.isInRTCRoom() > 0){
            callback.onResult(genRTMAnswer(voiceError, "createRTCRoom error you are in rtcroom-" + RTCEngine.isInRTCRoom()));
            return;
        }

        Quest quest = new Quest("createRTCRoom");
        quest.param("rid", roomId);
        quest.param("type", roomType);
        quest.param("enableRecord", enableRecord);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    String bringlang = language;
                    if (bringlang == null)
                        bringlang = "";
                    String roomToken = rtmUtils.wantString(answer,"token");
                    enterRTCRoomReal(callback,roomId, roomToken, roomType,bringlang);
                }
                else {
                    callback.onResult(genRTMAnswer(answer, errorCode));
                }
            }
        });
    }


    void enterRTCRoomReal( final IRTMEmptyCallback callback, final long roomId, final String token, int roomTyppe, String lang) {
        byte[] enterRet = RTCEngine.enterRTCRoom(token, roomId, roomTyppe, "", 0, lang);
        MessagePayloadUnpacker kk = new MessagePayloadUnpacker(enterRet);
        HashMap retmap;
        RTMAnswer rst = new RTMAnswer(0,"");
        try {
            retmap= new HashMap(kk.unpack());
            if (retmap.containsKey("ex")){
                rst.errorCode = rtmUtils.wantInt(retmap.get("code"));
                rst.errorMsg = String.valueOf(retmap.get("ex"));
            }
            else
            {
                if (roomTyppe == 2 && mOrEventListener!=null){ //视频房间
                    mOrEventListener.enable();
                }
//                ret.uids = rtmUtils.longHashSet(retmap.get("uids"));
//                ret.managers = rtmUtils.longHashSet(retmap.get("administrators"));
            }
        }
        catch (Exception e) {
            errorRecorder.recordError("Decoding enterRTCRoomReal package exception");
        }
        callback.onResult( rst);
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

        //----------------------[ RTM Operations ]-------------------//
        Answer ping(Quest quest, InetSocketAddress peer) {
//            Log.i("sdktest"," receive rtm ping");

            long now = Genid.getCurrentSeconds();
            lastPingTime.set(now);
            return new Answer(quest);
        }

        Answer kickout(Quest quest, InetSocketAddress peer) {
            setCloseType(CloseType.ByServer);
            close();
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
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
//                    Log.i("sdktest", "user " + calluid + "accept P2P " + type +" startP2P ok");
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
//                    Log.i("sdktest", "Accept connect P2P rtc failed " + ret);
                    errorRecorder.recordError("pushP2PRTCEvent startP2P error " + ret);
                }
                return null;
            }
            else {
                lastCallId = 0;
                peerUid = 0;
                lastP2Ptype = 0;
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
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
            serverPushProcessor.pushExitRTCRoom(roomId,  userId, time);
            return null;
        }

        Answer pushRTCRoomClosed(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));

            long roomId = rtmUtils.wantLong(quest,"rid");
            RTCEngine.leaveRTCRoom(roomId);
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
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
            enterRTCRoom(new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {

                }
            }, roomId, lang);
            return null;
        }

        Answer pushKickOutRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            long roomId = rtmUtils.wantLong(quest,"rid");
            RTCEngine.leaveRTCRoom(roomId);
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
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
        if (!successful) {
            if (isInitRTC){
                RTCEngine.RTCClear();
                RTCEngine.closeP2P();
            }
            lastCallId = 0;
            lastP2Ptype = 0;
            peerUid = 0;
        }
        serverPushProcessor.reloginCompleted(uid, successful, lastReloginAnswer, reloginCount);
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
            lastReloginAnswer = login(token, lang, kk, logints);
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
                    break;
                case NetUtils.NETWORK_MOBILE:
                case NetUtils.NETWORK_WIFI:
//                    Log.e("sdktest","have network");

                    if (rtmGate == null)
                        return;
                    noNetWorkNotify.set(false);
                    if (lastNetType != netWorkState) {
                        if (isRelogin.get())
                            return;
                        close();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isRelogin.set(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                reloginEvent(1);
//                                if (getClientStatus() == ClientStatus.Connected){
//                                    Quest quest = new Quest("bye");
//                                    sendQuest(quest, new FunctionalAnswerCallback() {
//                                        @Override
//                                        public void onAnswer(Answer answer, int errorCode) {
//                                            close();
//                                            try {
//                                                Thread.sleep(200);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                            reloginEvent(1);
//                                        }
//                                    }, 5);
//                                }
//                                else {
////                                    voiceClose();
//                                    reloginEvent(1);
//                                }
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

    public static boolean isH265DecoderSupport(){
        int count = MediaCodecList.getCodecCount();
        for(int i=0;i<count;i++){
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            String name = info.getName();
            if(name.contains("decoder") && name.contains("hevc")){
                return true;
            }
        }
        return false;
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

        this.pid = pid;
        this.uid = uid;
        isRelogin.set(false);
        fileGates = new HashMap<>();
        processor = new RTMQuestProcessor();
        this.serverPushProcessor = serverPushProcessor;
        this.currentActivity = new WeakReference<Activity>(currentActivity);

        application = currentActivity.getApplication();
        ClientEngine.setMaxThreadInTaskPool(RTMConfig.globalMaxThread);
        application.registerActivityLifecycleCallbacks(this);

        if (currentActivity == null){
            printLog("currentActivity is null ");
            return;
        }
        context = currentActivity.getApplicationContext();

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        try {
            //网络监听
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            context.registerReceiver(this, intentFilter);

            addressSp = context.getSharedPreferences("Logindb",context.MODE_PRIVATE);
        }
        catch (Exception ex){
            ex.printStackTrace();
            errorRecorder.recordError("registerReceiver exception:" + ex.getMessage());
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

    RTMAnswer  initRTC() {
        int errCode = RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value();

        if (isInitRTC)
            return genRTMAnswer(okRet);

        if (rtcEndpoint == null || rtcEndpoint.isEmpty())
            return genRTMAnswer(errCode, "rtcEndpoint is null or empty");

        if ( rtcEndpoint.lastIndexOf(':') == -1)
            return genRTMAnswer(errCode, "invalid rtcEndpoint " + rtcEndpoint);

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
//        mOrEventListener.enable();
        if (rtmGateStatus != ClientStatus.Connected) {
            return genRTMAnswer(errCode, "you must RTMlogin sucessfully at first");
        }

        String ret = RTCEngine.create(this, rtcEndpoint, currVideoLevel, pid, uid, application, afChangeListener);
        if (!ret.isEmpty()) {
            return genRTMAnswer(errCode,"initRTC create error " + ret);
        }

        isInitRTC = true;
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
            if (context != null)
                context.unregisterReceiver(this);
            if (application != null)
                application.unregisterActivityLifecycleCallbacks(this);

        } catch (IllegalArgumentException e){
        }

        if (mOrEventListener != null)
            mOrEventListener.disable();
        close(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

                    if (rtmGateStatus != ClientStatus.Closed && !connectionIsAlive()) {
                        closedCase = CloseType.Timeout;
                        close();
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



    public void whoSpeak(long uid){
        serverPushProcessor.voiceSpeak(uid);
    }

    public void pushVoiceTranslate(String text, String slang, long uid){
//        Log.e("sdktest",msg);
//        Log.i("sdktest","pushText " + text);

        serverPushProcessor.pushVoiceTranslate(text, slang, uid);
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

    //-------------[ Auth(Login) utilies functions ]--------------------------//
    private void ConfigRtmGateClient(final TCPClient client) {
        client.setQuestTimeout(rtmConfig.globalQuestTimeoutSeconds);

        if (encrptyData != null && curve!=null && !curve.equals(""))
            client.enableEncryptorByDerData(curve, encrptyData);

        if (errorRecorder != null)
            client.setErrorRecorder(errorRecorder);

        client.setQuestProcessor(processor, "com.rtcsdk.RTMCore$RTMQuestProcessor");

        client.setWillCloseCallback(new ConnectionWillCloseCallback() {
            @Override
            public void connectionWillClose(InetSocketAddress peerAddress, int _connectionId,boolean causedByError) {
//                printLog("closedCase " + closedCase + " getClientStatus() " + getClientStatus());
                if (connectionId.get() != 0 && connectionId.get() == _connectionId && closedCase != CloseType.ByUser && closedCase !=CloseType.ByServer && getClientStatus() != ClientStatus.Connecting) {
                    close();

                    processor.rtmConnectClose();

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
        });
    }

    public void httpRequest(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int resultCode = -1;
                try {
                    URL sendurl = new URL(url);
                    HttpsURLConnection conn = (HttpsURLConnection) sendurl.openConnection();
                    conn.setConnectTimeout(15 * 1000);//超时时间
                    conn.setReadTimeout(15 * 1000);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.connect();
                    resultCode = conn.getResponseCode();
                }catch (Exception ex){
                    Log.i("rtmsdk","httprequest error " + resultCode);
                }
            }
        }).start();
    }


    private void test80(String ipaddres, final IRTMEmptyCallback callback){
        String realhost = ipaddres;
        if (ipaddres.isEmpty()) {
            realhost = endpoint.split(":")[0];
            if (realhost == null || realhost.isEmpty()) {
                callback.onResult(genRTMAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value()));
                return;
            }
        }

        rtmGate = new TCPClient(realhost, 80);
        ConfigRtmGateClient(rtmGate);
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("version", "Android-" + rtmConfig.SDKVersion);
        qt.param("device", deviceid);

        if (loginAttrs != null)
            qt.param("attrs", loginAttrs);

        Answer answer = null;
        try {
            answer = rtmGate.sendQuest(qt, rtmConfig.globalQuestTimeoutSeconds);
//            answer = new Answer(qt);
//            answer.fillErrorCode(FPNN_EC_CORE_INVALID_CONNECTION.value());
            if (answer.getErrorCode() != ErrorCode.FPNN_EC_OK.value()){
                String url = "https://" + endpoint.split(":")[0] + "/service/tcp-13321-fail-tcp-80-fail" + pid + "-" + uid;
                httpRequest(url);
                callback.onResult(genRTMAnswer(answer));
            }
            else{
                Quest quest = new Quest("adddebuglog");
                String msg = "pid:" + pid + " uid:"+uid +  " link 80 port ok";
                quest.param("msg",msg);
                quest.param("attrs","");
                rtmGate.sendQuest(quest, new FunctionalAnswerCallback() {
                    @Override
                    public void onAnswer(Answer answer, int errorCode) {
//                        Log.i("sdktest","hehehehe " + errorCode);
                    }
                });
                synchronized (interLocker) {
                    rtmGateStatus = ClientStatus.Connected;
                }

                synchronized (addressSp){
                    SharedPreferences.Editor editor = addressSp.edit();
                    editor.putString("addressip",rtmGate.peerAddress.getAddress().getHostAddress());
                    editor.commit();
                }

                processor.setLastPingTime(Genid.getCurrentSeconds());
                checkRoutineInit();
                connectionId.set(rtmGate.getConnectionId());
                callback.onResult(genRTMAnswer(ErrorCode.FPNN_EC_OK.value()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            callback.onResult(genRTMAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_METHOD.value()));
        }
    }


    private RTMAnswer test80(String ipaddres){
        String realhost = ipaddres;
        if (ipaddres.isEmpty()) {
            String linkEndpoint = rtmGate.endpoint();
            realhost = linkEndpoint.split(":")[0];
            if (realhost == null || realhost.isEmpty())
                return genRTMAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value());
        }

        rtmGate = new TCPClient(realhost, 80);
        ConfigRtmGateClient(rtmGate);
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("version", "Android-" + rtmConfig.SDKVersion);
        qt.param("device", deviceid);

        if (loginAttrs != null)
            qt.param("attrs", loginAttrs);

        Answer answer = null;
        try {
            answer = rtmGate.sendQuest(qt, rtmConfig.globalQuestTimeoutSeconds);
//            answer = new Answer(qt);
//            answer.fillErrorCode(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value());
            if (answer.getErrorCode() != ErrorCode.FPNN_EC_OK.value()){
                String url = "https://" + endpoint.split(":")[0] + "/service/tcp-13321-fail-tcp-80-fail" + pid + "-" + uid;
                httpRequest(url);
                return genRTMAnswer(answer);
            }
            else {
                Quest quest = new Quest("adddebuglog");
                String msg = "pid:" + pid + " uid:"+uid +  "link 80 port ok";
                quest.param("msg",msg);
                quest.param("attrs","");
                rtmGate.sendQuest(quest, new FunctionalAnswerCallback() {
                    @Override
                    public void onAnswer(Answer answer, int errorCode) {

                    }
                });

                synchronized (interLocker) {
                    rtmGateStatus = ClientStatus.Connected;
                }
                processor.setLastPingTime(Genid.getCurrentSeconds());
                checkRoutineInit();
                connectionId.set(rtmGate.getConnectionId());
                synchronized (addressSp){
                    SharedPreferences.Editor editor = addressSp.edit();
                    editor.putString("addressip",rtmGate.peerAddress.getAddress().getHostAddress());
                    editor.commit();
                }
                return genRTMAnswer(ErrorCode.FPNN_EC_OK.value());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return genRTMAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_METHOD.value());
        }
    }

    //------------voice add---------------//
    private RTMAnswer auth(String token, Map<String, String> attr, boolean retry, long ts) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        String sharedip = "";

        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        qt.param("version", "Android-Voice-" + RTMConfig.SDKVersion);
        if (ts == 0){
            qt.param("authv", 1);
        }
        else{
            qt.param("authv", 2);
        }
        qt.param("ts", ts);

        if (attr != null)
            qt.param("attrs", attr);
        try {
            Answer answer = rtmGate.sendQuest(qt, rtmConfig.globalQuestTimeoutSeconds);
//            Answer answer = new Answer(qt);
//            answer.fillErrorCode(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value());
            if (answer  == null || answer.getErrorCode() != ErrorCode.FPNN_EC_OK.value()) {
                closeStatus();
                if (retry)
                    return test80(sharedip);
                if (answer != null && answer.getErrorMessage().indexOf("Connection open channel failed") != -1){
                    InetSocketAddress peeraddres = rtmGate.peerAddress;
                    if (peeraddres != null){
                        boolean isnetwork = isNetWorkConnected();
                        String hostname = endpoint.split(":")[0];
                        if (peeraddres.getHostString().equals(hostname) && isnetwork && addressSp != null){
                            synchronized (addressSp){
                                sharedip = addressSp.getString("addressip", "");
                            }
                            if (!sharedip.isEmpty()) {
                                rtmGate = new TCPClient(sharedip, peeraddres.getPort());
                                ConfigRtmGateClient(rtmGate);
                                return auth(token, attr,true,ts);
                            }
                        }
                        if (!isnetwork)
                            return genRTMAnswer(answer,"when send sync auth  failed:no network ");
                        else {
                            return genRTMAnswer(answer, "when send sync auth  rtmGate parse endpoint " + peeraddres.getHostString());
                        }
                    }
                    else
                        return genRTMAnswer(answer,"when send sync auth  parse address is null");
                }
                else if (answer != null && answer.getErrorCode() == FPNN_EC_CORE_INVALID_CONNECTION.value())
                {
                    return test80(sharedip);
//                    return genRTMAnswer(answer,"when send sync auth ");
                }
                else
                    return genRTMAnswer(answer,"when send sync auth ");

            }
            else if (!rtmUtils.wantBoolean(answer,"ok")) {
                closeStatus();
                return genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(),"sync auth failed token maybe expired");
            }
            synchronized (interLocker) {
                rtmGateStatus = ClientStatus.Connected;
            }
            processor.setLastPingTime(Genid.getCurrentSeconds());
            checkRoutineInit();
            connectionId.set(rtmGate.getConnectionId());
            synchronized (addressSp){
                SharedPreferences.Editor editor = addressSp.edit();
                editor.putString("addressip",rtmGate.peerAddress.getAddress().getHostAddress());
                editor.commit();
            }

            return genRTMAnswer(answer);
        }
        catch (Exception  ex){
            closeStatus();
            return genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),ex.getMessage());
        }
    }

    private void auth(final IRTMEmptyCallback callback, final String token, final Map<String, String> attr, final boolean retry, final long ts) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        final Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        if (ts == 0){
            qt.param("authv", 1);
        }
        else{
            qt.param("authv", 2);
        }
        qt.param("ts", ts);
        qt.param("version", "Android-" + rtmConfig.SDKVersion);
        if (attr != null)
            qt.param("attrs", attr);

        rtmGate.sendQuest(qt, new FunctionalAnswerCallback() {
            @SuppressLint("NewApi")
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                try {
                    String sharedip = "";

                    if (answer == null || errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        closeStatus();
                        if (retry) {
                            test80(sharedip, callback);
//                            callback.onResult(genRTMAnswer(answer, "retry failed when send async auth "));
                            return;
                        }
                        if (answer!= null && answer.getErrorMessage().indexOf("Connection open channel failed") != -1){
                            InetSocketAddress peeraddres = rtmGate.peerAddress;
                            if (peeraddres != null){
                                boolean isnetwork = isNetWorkConnected();
                                String hostname = endpoint.split(":")[0];
                                if (peeraddres.getHostString().equals(hostname) && isnetwork && addressSp != null){
                                    synchronized (addressSp){
                                        sharedip = addressSp.getString("addressip", "");
                                    }
                                    rtmGate.peerAddress = new InetSocketAddress(sharedip, peeraddres.getPort());
                                    auth(callback, token, attr, true,ts);
                                    return;
                                }
                                if (!isnetwork)
                                    callback.onResult(genRTMAnswer( errorCode, "when send async auth   failed:no network "  + answer.getErrorMessage()));
                                else
                                    callback.onResult(genRTMAnswer( errorCode, "when send async auth " + answer.getErrorMessage() + " parse address:" + peeraddres.getHostString()));
                            }
                            else
                                callback.onResult(genRTMAnswer( errorCode, "when send async auth " + answer.getErrorMessage() + "peeraddres is null"));
                            return;
                        }
                        else
                        {
                            test80(sharedip, callback);
//                            callback.onResult(genRTMAnswer( answer, "when send async auth " + answer.getErrorMessage()));
                            return;
                        }
                    } else if (!rtmUtils.wantBoolean(answer,"ok")) {
                        closeStatus();
                        callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "async auth failed token maybe expired"));
                    } else {
                        synchronized (interLocker) {
                            rtmGateStatus = ClientStatus.Connected;
                        }

                        synchronized (addressSp){
                            SharedPreferences.Editor editor = addressSp.edit();
                            editor.putString("addressip",rtmGate.peerAddress.getAddress().getHostAddress());
                            editor.commit();
                        }

                        processor.setLastPingTime(Genid.getCurrentSeconds());
                        checkRoutineInit();
                        connectionId.set(rtmGate.getConnectionId());
                        callback.onResult(genRTMAnswer(errorCode));
                    }
                }
                catch (Exception e){
                    callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"when async auth " + e.getMessage()));
                }
            }
        }, rtmConfig.globalQuestTimeoutSeconds);
    }


    void login(final IRTMEmptyCallback callback, final String token, final String lang, final Map<String, String> attr, long ts) {
        if (token ==null || token.isEmpty()){
            callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value()," token  is null or empty"));
            return;
        }

        String errDesc = "";
        if (rtmEndpoint == null || rtmEndpoint.isEmpty() || rtmEndpoint.lastIndexOf(':') == -1)
            errDesc = "invalid rtmEndpoint:" + rtmEndpoint;
        if (pid <= 0)
            errDesc += " pid is invalid:" + pid;
        if (uid <= 0)
            errDesc += " uid is invalid:" + uid;
        if (serverPushProcessor == null)
            errDesc += " RTMMPushProcessor is null";

        if (!errDesc.equals("")) {
            errorRecorder.recordError("rtmclient init error." + errDesc);
            callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(), errDesc));
            return;
        }

            if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(genRTMAnswer(RTMErrorCode.RTM_EC_OK.value()));
                    }
                }).start();
                return;
            }
        synchronized (interLocker) {
            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            auth(callback, token, attr,false, ts);
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
            this.logints = ts;
            if (lang == null)
                this.lang = "";
            else
                this.lang = lang;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            auth(callback, token, attr,false, ts);
        }
    }

    private  void closeStatus()
    {
        synchronized (interLocker) {
            rtmGateStatus = ClientStatus.Closed;
        }
    }

    RTMAnswer login(String token, String lang, Map<String, String> attr, long ts) {
        if (token == null || token.isEmpty())
            return genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "login failed token  is null or empty");

        String errDesc = "";
        if (rtmEndpoint == null || rtmEndpoint.isEmpty() || rtmEndpoint.lastIndexOf(':') == -1)
            errDesc = "invalid rtmEndpoint:" + rtmEndpoint;
        if (pid <= 0)
            errDesc += " pid is invalid:" + pid;
        if (uid <= 0)
            errDesc += " uid is invalid:" + uid;
        if (serverPushProcessor == null)
            errDesc += " RTMMPushProcessor is null";

        if (!errDesc.equals("")) {
            errorRecorder.recordError("login init error." + errDesc);
            return genRTMAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(), errDesc);
        }

        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting)
                return genRTMAnswer(ErrorCode.FPNN_EC_OK.value());

            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            return auth(token, attr,false, ts);
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
            this.logints = ts;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            return auth(token, attr, false,ts);
        }
    }

    void rtcclear(final boolean delete){
        if (isInitRTC){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RTCEngine.RTCClear();
                    if (delete)
                        RTCEngine.delete();
                }
            }).start();
        }
        isInitRTC = false;
    }

    public void close(){
        close(false);
    }

    public void close(boolean deleteRTC) {
        if (isRelogin.get()) {
            return;
        }
        synchronized (interLocker) {
            initCheckThread.set(false);
            running.set(false);
            fileGates.clear();
            if (rtmGateStatus == ClientStatus.Closed) {
                return;
            }
            rtmGateStatus = ClientStatus.Closed;
        }
        if (rtmGate !=null)
            rtmGate.close();
        rtcclear(deleteRTC);
    }
}