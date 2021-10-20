package com.voicedemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fpnn.sdk.ErrorRecorder;
import com.livedata.rtc.RTCEngine;
import com.rtcvoice.RTMClient;
import com.rtcvoice.RTMPushProcessor;
import com.rtcvoice.RTMStruct;
import com.rtcvoice.UserInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

public class TestvoiceActivity extends Activity {
    Utils testUtils;
    final String[] buttonNames = {"enter", "leave", "login", "clear"};
//    private SurfaceView decodeSurfaceView = null;
    RTMPushProcessor voicepush = new RTMVoiceProcessor();
    public TestErrorRecorderVoice voicerecoder = new TestErrorRecorderVoice();
    ConstraintLayout alllayout;
  //    private int viewHeght;
//    private int viewWidth;
    public  static  boolean running = false;
    TextView logview;
    boolean micStatus = false;
    boolean voiceStatus = false;
    boolean usespeaker = true;
    ToggleButton checkbutton;
    ImageView laba;
    long activityRoom;
    ImageView speakers;
    int agclevel = 0;
    TextView showbeishu;
    ImageView mic;
    SeekBar agc;
    Context mycontext = this;
    Activity myactivity = this;
    int REQUEST_CODE_CONTACT = 101;
    TextView uidtext;
    CheckBox channellNum;
    TextView textuid2;
    RTMClient client;
    Spinner roomMembers;
    ArrayList<String> roomMembervalue = new ArrayList<String>();
    ArrayAdapter adapterMembers;


    String userInfo() {
        return "用户 " + testUtils.uid + " ";
    }

    String transRet(RTMStruct.RTMAnswer answer) {
        return (answer.errorCode == 0 ? "成功" : "失败-" + answer.getErrInfo());
    }

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }
    
    class RTMVoiceProcessor extends RTMPushProcessor {
        String msg = "";

        public boolean reloginWillStart(long uid,  int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            msg = userInfo() + " 开始重连第 " + reloginCount + "次";
            addLog(msg);
            return true;
        }


        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            msg = userInfo() + " 重连结束 共" + reloginCount + "次，结果 " + transRet(answer);
            addLog(msg);
            if (successful) {
                if (activityRoom <= 0)
                    return;
                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode != 0) {
                            msg = userInfo() + "重新进入房间 " + activityRoom + answer.getErrInfo();
                        } else {
                            msg = userInfo() + "重新进入房间 " + activityRoom + " 成功";
                            TestvoiceActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textuid2.setText("房间id " + activityRoom);
                                    mic.setBackgroundResource(R.drawable.micclose);
                                    roomMembervalue.clear();
                                    for (Long id: roomInfo.uids){
                                        roomMembervalue.add(String.valueOf(id));
                                    }
//
                                    adapterMembers.notifyDataSetChanged();
                                }
                            });
                        }
                        addLog(msg);
                    }
                },activityRoom);
            } else {
                mic.setBackgroundResource(R.drawable.micclose);
                micStatus = false;
            }
        }

        public void rtmConnectClose(long uid) {
            TestvoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (logview) {
                        logview.append("RTM链接断开\n");
                    }
                    if (activityRoom > 0) {
                        textuid2.setText("");
                        mic.setBackgroundResource(R.drawable.micclose);
                    }
                }
            });
        }

        public void kickout() {
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (logview) {
                        logview.append("Received kickout.\n");
                    }
                    activityRoom = 0;
                    mic.setBackgroundResource(R.drawable.micclose);
                }
            });
        }


        @Override
        public void pushPullRoom(long roomId, RTMStruct.RoomInfo info) {
            addLog("user " + testUtils.uid + "被拉入房间 " + roomId + info.getErrInfo());
        }

        @Override
        public void pushEnterRTCRoom(final long roomId, final long userId, long time) {
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    adapterMembers.add(userId);
                    if (roomMembervalue.indexOf(userId) == -1) {
                        roomMembervalue.add(String.valueOf(userId));
                        adapterMembers.notifyDataSetChanged();
                    }
                }
            });
            addLog("user " + userId + "进入房间 " + roomId);
        }

        @Override
        public void pushAdminCommand(int command, HashSet<Long> uids) {
            addLog("recieve AdminCommand " + command + " uids " + uids.toString());
        }

        @Override
        public void pushExitRTCRoom(final long roomId, final long userId, long time) {
//            client.userLeave(userId);
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapterMembers.remove(userId);
                    roomMembervalue.remove(String.valueOf(userId));
                    adapterMembers.notifyDataSetChanged();
                }
            });
            addLog("user " + userId + "退出房间 " + roomId);
        }

        @Override
        public void pushRTCRoomClosed(long roomId) {
           myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        roomMembervalue.clear();
                        adapterMembers.clear();
                        adapterMembers.notifyDataSetChanged();
                }
            });
            addLog("房间 " + roomId + "已关闭 ");
        }

        @Override
        public void pushKickoutRTCRoom(final long roomId) {
            realLeaveRoom();
            addLog("被踢出语音房间 " + roomId);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        testUtils.rtmClient.leaveRTCRoom(activityRoom);
        realLeaveRoom();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    void floatBenchmark()
    {
        float a = 0.0f;
        long start = SystemClock.elapsedRealtimeNanos();
        for (int i = 0;i!=10000;i++)
        {
            a+=1.0f;
        }
        long end = SystemClock.elapsedRealtimeNanos();
        Log.d("benchMark","spend "+ (end-start) + "ns");
    }

    @Override
    public Resources getResources() {
        // 字体大小不跟随系统
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)
            getResources();
        super.onConfigurationChanged(newConfig);
    }

//    @Override
//    public Resources getResources() {
//        Resources res = super.getResources();
//        if (res.getConfiguration().fontScale != 1) {
//            Configuration newConfig = new Configuration();
//            newConfig.setToDefaults();
//            res.updateConfiguration(newConfig, res.getDisplayMetrics());
//        }
//        return res;
//    }

    int getLatencyTime(){
//        AudioTrack myTrack = new AudioTrack(
//                new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build(),
//                myFormat, myBuffSize, AudioTrack.MODE_STREAM, mySession);
//        Method getLatencyMethod =android.media.AudioTrack.class.getMethod("getLatency", (Class < ? > []) null);
//
//        int audioLatencyMs = (int) ((Integer) getLatencyMethod.invoke(audioTrack, (Object[]) null));
//
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Method m = null;
        try {
            m = am.getClass().getMethod("getOutputLatency", int.class);
            return (Integer) m.invoke(am, AudioManager.STREAM_MUSIC);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return  0;
    }
    AudioManager am;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testvideo);
        testUtils = new Utils(voicerecoder,voicepush,this,"nx");
        testUtils.errorRecorder = voicerecoder;
        client = testUtils.rtmClient;
        logview = $(R.id.logview);
        logview.setTextSize(14);
        logview.setTextColor(this.getResources().getColor(R.color.white));
        logview.setMovementMethod(ScrollingMovementMethod.getInstance());
        agc = $(R.id.huatiao);
        showbeishu = $(R.id.showbeishu);
        laba = $(R.id.laba);
        speakers = $(R.id.speaker);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        checkbutton = $(R.id.checkbutton);
        checkbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mylog.log("checkbutton is " + b);
                RTCEngine.setdiscardable(b);
            }
        });
        laba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                setVoiceStatus(!voiceStatus);
            }
        });

        speakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                setSpeakerStatus();
            }
        });


        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showbeishu.setText(String.valueOf(seekBar.getProgress()));
                agclevel = seekBar.getProgress();
//            mylog.log("当前进度：" + seekBar.getProgress());
//            Toast.makeText(mycontext, "当前进度：" + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(mycontext, "开始：" + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                client.setMicphoneLevel(agclevel);
                addLog("设置麦克风增益等级:" + agclevel);
            }
        };

        agc.setOnSeekBarChangeListener(onSeekBarChangeListener);

//        addLog("音频延迟: " + getLatencyTime());

//        mylog.log("version " + Build.VERSION.SDK_INT);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().hide();

//        ViewTreeObserver vto = decodeSurface.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                decodeSurface.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                int layoutHeight = decodeSurface.getHeight();
//                int layoutWidth = decodeSurface.getWidth();
//                viewHeght = layoutHeight/2;
//                viewWidth = layoutWidth/3;
//
//            }
//        });
        alllayout = $(R.id.alllayout);

        roomMembers= $(R.id.roomsMembers);
        adapterMembers = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,roomMembervalue);
        roomMembers.setAdapter(adapterMembers);
        adapterMembers.notifyDataSetChanged();
        roomMembers.setSelection(0, true);
//        roomMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });


        uidtext = $(R.id.textuid);
        textuid2 = $(R.id.textuid2);
        mic = $(R.id.mic);
        channellNum = $(R.id.checkbox);

        for (String name : buttonNames) {
            int buttonId = 0;
            try {
                buttonId = getResources().getIdentifier(name, "id", getBaseContext().getPackageName());
            }
            catch (Exception ex){
                mylog.log("error " + ex.getMessage());
                return;
            }
            Button button = $(buttonId);
            button.setTextSize(14);
            button.setTextColor(this.getResources().getColor(R.color.white));
            button.setOnClickListener(new TestButtonListener());
        }

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                if (activityRoom <= 0) {
                    alertDialog("请先进入房间");
                    return;
                }
                setMicStatus(!micStatus);
            }
        });
    }
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < 8; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    void  addLog(final String msg) {
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (logview) {
                    logview.append(msg + "\n");
                }
            }
        });
    }

    public class TestErrorRecorderVoice extends ErrorRecorder {
        public TestErrorRecorderVoice() {
            super.setErrorRecorder(this);
        }

        public void recordError(Exception e) {
            String msg = "Exception:" + e;
            addLog(msg);
        }

        public void recordError(String message) {
            addLog(message);
        }

        public void recordError(String message, Exception e) {
            String msg = String.format("Error: %s, exception: %s", message, e);
            addLog(msg);
        }
    }

    public void alertDialog(final String str) {
//        Looper.prepare();
//        new AlertDialog.Builder(activity).setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        }).show();
//        Looper.loop();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mycontext).setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
    }

    boolean checkClient() {
        if (client == null || !client.isOnline()) {
            alertDialog("请先登录");
            return false;
        }
        return true;
    }

    void closeInput() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
    }

    void realLeaveRoom(){
        addLog( "离开房间 " + activityRoom);
        client.leaveRTCRoom(activityRoom);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVoiceStatus(false);
                textuid2.setText("");
                adapterMembers.clear();
                adapterMembers.notifyDataSetChanged();
            }
        });
    }

    void login() {
        RTMStruct.RTMAnswer  answer =  testUtils.login();
        if (answer.errorCode == 0) {
            boolean ret = channellNum.isChecked();
            addLog("RTM登陆成功");
//            am.setSpeakerphoneOn(false);
//            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            am.setMode(AudioManager.MODE_IN_CALL);

            RTMStruct.RTMAnswer jj = client.initRTMVoice(ret);
            if (jj.errorCode != 0 ){
                addLog("初始化 音频失败 " + jj.getErrInfo());
                return;
            }

//            am.setSpeakerphoneOn(false);
//            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            am.setMode(AudioManager.MODE_IN_COMMUNICATION);

            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uidtext.setText("用户id-" + testUtils.uid);
                }
            });
            realEnterRoom(111);
        } else {
            addLog("RTM登录失败 " + answer.getErrInfo());
        }
    }

    class TestButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.clear:
                    synchronized (logview) {
                        logview.setText("");
                    }
                    break;
                case R.id.login:
                    closeInput();

                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CAMERA};
                        //验证是否许可权限
                        for (String str : permissions) {
                            if (myactivity.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                //申请权限
                                myactivity.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                            }
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            login();
                        }
                    }).start();
                    break;
                case R.id.leave:
                    if (!checkClient())
                        return;
//                    client.adminCommand(new UserInterface.IRTMEmptyCallback() {
//                        @Override
//                        public void onResult(RTMStruct.RTMAnswer answer) {
//                            addLog("adminCommand " + transRet(answer));
//                        }
//                    },111, new HashSet<Long>() {{
//                        add(1L);
//                    }}, 0);
                    realLeaveRoom();
                    break;
                case R.id.enter:
                    final EditText inputServer1 = new EditText(myactivity);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(myactivity);
                    builder1.setTitle("请输入房间号").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer1)
                            .setNegativeButton("取消", null);
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (inputServer1.getText().toString().isEmpty()) {
                                alertDialog("请输入正确的房间号");
                                return;
                            }
                            if (!checkClient())
                                return;

                            long enterid = 0;
                            try {
                                enterid = Long.parseLong(inputServer1.getText().toString());
                            } catch (NumberFormatException e) {
                                alertDialog("请输入正确的房间号");
                                return;
                            }
                            final long inputRid1 = enterid;

//                            if (videoRoom.get() == inputRid1) {
//                                addLog("进入房间  " + inputRid1 + "成功");
//                                return;
//                            }
//
//                            if (videoRoom.get() > 0) {
//                                client.leaveRTCRoom(videoRoom.get());
//                                addLog(userInfo() + "离开房间 " + videoRoom.get());
//                            }
                            realEnterRoom(inputRid1);
                        }
                    });
                    builder1.show();
                    break;
            }
        }
    }


    void setMicStatus(boolean status){
        if (!status) {
            client.closeMic();
            mic.setBackgroundResource(R.drawable.micclose);
            addLog("关闭麦克风");
        } else {
            client.openMic();
            mic.setBackgroundResource(R.drawable.micopen);
            addLog("打开麦克风");
        }
        micStatus = status;
    }


    void setVoiceStatus(boolean status){
        if (status == voiceStatus)
            return;

        RTMStruct.RTMAnswer ret = client.setVoiceStat(status);
        if (ret.errorCode != 0){
            addLog("setVoiceStat:" +  status + " error " + ret.getErrInfo() );
            return;
        }
        if(!status) {
            micStatus = false;
            addLog("关闭语音");
            laba.setBackgroundResource(R.drawable.voiceclose);
            mic.setBackgroundResource(R.drawable.micclose);
            client.closeMic();
        }
        else {
            addLog("打开语音");
            laba.setBackgroundResource(R.drawable.voiceopen);
        }
        voiceStatus = status;
    }

    void setSpeakerStatus(){
        usespeaker = !usespeaker;
        if (usespeaker)
            client.switchOutput(true);
        else
            client.switchOutput(false);

        if(!usespeaker) {
            micStatus = false;
            addLog("打开听筒");
            speakers.setBackgroundResource(R.drawable.speakeroff);
        }
        else {
            addLog("打开扬声器");
            speakers.setBackgroundResource(R.drawable.speakeron);
        }
    }


    void realEnterRoom(final long roomId){
        client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    addLog("进入房间  " + roomId + " " + transRet(answer));
                    activityRoom = roomId;
                    client.setActivityRoom(activityRoom);
                    myactivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setVoiceStatus(true);
                            setMicStatus(true);
                            textuid2.setText("房间id-" + roomId);
                            adapterMembers.clear();
                            roomMembervalue.clear();
//                            adapterMembers
//                            roomMembervalue.addAll(Arrays.asList((String [])(info.uids.toArray())));
                            adapterMembers.addAll(info.uids);
                            adapterMembers.notifyDataSetChanged();
                        }
                    });
                }
                else{
                    client.createRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            addLog("创建房间  " + roomId + " " + transRet(answer));
                            if (answer.errorCode == 0) {
                                activityRoom = roomId;
                                client.setActivityRoom(activityRoom);
                                myactivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setVoiceStatus(true);
                                        setMicStatus(true);
                                        textuid2.setText("房间id-" + roomId);
                                        adapterMembers.clear();
                                        adapterMembers.addAll(roomInfo.uids);
                                        adapterMembers.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    },roomId);
                }
            }
        }, roomId);
    }
}
