package com.highras.videoudp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.highras.videoudp.adapter.VoiceMemberAdapter;
import com.highras.videoudp.model.VoiceMember;
import com.livedata.rtc.RTCEngine;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMErrorCode;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TestVoiceActivity extends AppCompatActivity {
    private static final String TAG = "TestVoiceActivity";
    Timer timer;
    RTMPushProcessor voicepush = new RTMVoiceProcessor();
    public TestErrorRecorderVoice voicerecoder = new TestErrorRecorderVoice();
    boolean micStatus = true;
    boolean usespeaker = true;
    TCPClient rttcclient = null;
    LinearLayout leave;
    MediaPlayer mediaPlayer = new MediaPlayer();
    LinearLayout mic;
    LinearLayout speaker;
    ImageView speakerImageView;
    ImageView muteImageView;
    ImageView back;
    TextView muteTextView;
    TextView logView;
    long activityRoom = 0;
    long userid = 0;
    Activity myactivity = this;
    TextView roomshow;
    TextView udpRTTshow;
    TextView tcpRTTshow;
    RTMClient client;
    Button clear;
    Utils utils = Utils.INSTANCE;
    String nickName;

    Chronometer chronometer;

    VoiceMemberAdapter voiceMemberAdapter;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    class RTMVoiceProcessor extends RTMPushProcessor {
        String msg = "";

        @Override
        public boolean reloginWillStart(long uid, int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            return true;
        }

        @Override
        public void pushEnterRTCRoom(long roomId, long userId, long time) {
            if (containsMember(userId) == null) {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HashSet<Long> hashSet = new HashSet<>();
                        hashSet.add(userId);
                        RTMStruct.PublicInfo userPublicInfo = client.getUserPublicInfo(hashSet);
                        String name = userPublicInfo.publicInfos.get(String.valueOf(userId));
                        synchronized (logView) {
                            logView.append( name + "(" + userId + ")" + " 进入房间" + "\n");
                        }

                        memberlist.add(new VoiceMember(userId, name));
                        voiceMemberAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void pushExitRTCRoom(long roomId, long userId, long time) {
            VoiceMember member = containsMember(userId);
            if (member!= null) {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (logView) {
                            logView.append(member.getNickName() + "(" + userId + ") 离开房间" + "\n");
                        }
                        removeMember(userId);
                        voiceMemberAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0 || client == null)
                    return;
                client.enterRTCRoom(activityRoom, "",new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode == 0) {
                            startVoice(activityRoom, true, roomInfo);
                        } else {
                            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    soundRelayout.setVisibility(View.VISIBLE);
                                    soundText.setText("重新进入房间" + reloginCount + "次失败");
                                }
                            });
                        }
                        TestVoiceActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                soundRelayout.setVisibility(View.GONE);
                                soundText.setText("");
                            }
                        });
                    }
                });
            } else {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        soundRelayout.setVisibility(View.VISIBLE);
                        soundText.setText("重新进入房间失败");
                        addLog("重新进入房间失败 " + answer.getErrInfo());
                    }
                });
                micStatus = false;
            }
        }

        @Override
        public void rtmConnectClose(long uid) {
            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    soundRelayout.setVisibility(View.VISIBLE);
                    soundText.setText("客户端断开连接");
                }
            });
        }

        @Override
        public void kickout() {
            mylog.log("receive kickout");
        }

        @Override
        public void voiceSpeak(long uid) {
            myactivity.runOnUiThread(() -> {
                setVoice(uid);
                voiceMemberAdapter.notifyDataSetChanged();
            });
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (client != null && client.isOnline()) {
                voiceMemberAdapter.notifyDataSetChanged();
            }
            handler.postDelayed(this, 1000);
        }
    };


    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        leaveRoom();
    }

    private void leaveRoom() {
        if (client == null)
            return;
        chronometer.stop();
        utils.client.leaveRTCRoom(activityRoom, new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
            }
        });
        client.closeRTM();
        client = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)

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

    Toolbar toolbar;

    RelativeLayout soundRelayout;
    TextView soundText;
    List<VoiceMember> memberlist = new ArrayList<>();

    void addLog(final String msg) {
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (logView) {
                    String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
                    logView.append(realmsg);
                }
            }
        });
    }

    private void initMediaPlayer() {
        try {
            AssetFileDescriptor fd = getAssets().openFd("zh.wav");
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.prepare();//初始化播放器MediaPlayer
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.testvoice);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        customToolbarAndStatusBarBackgroundColor(true);
        mic = $(R.id.mic);
        back = $(R.id.back);
        muteImageView = $(R.id.muteImageView);
        speaker = $(R.id.speaker);
        speakerImageView = $(R.id.speakerImageView);
        leave = $(R.id.leave);
        muteTextView = $(R.id.muteTextView);
        roomshow = $(R.id.roomnum);
        udpRTTshow = $(R.id.UDPRTTshow);
        tcpRTTshow = $(R.id.TCPRTTshow);
        logView = $(R.id.logview);
        logView.setTextSize(14);
        logView.setTextColor(this.getResources().getColor(R.color.white));
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        clear = $(R.id.clearlog);

        chronometer = $(R.id.caltimer);
        activityRoom = utils.currentRoomid;
        userid = utils.currentUserid;
        speakerImageView.setSelected(true);
        nickName = utils.nickName;

//        addLog("buffsize "+ AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT));
        soundText = $(R.id.nameTextView);
        soundRelayout = $(R.id.sound_relayout);
        Timer timer = new Timer();
        utils.login(myactivity, voicepush,new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                mylog.log("login ret" + answer.getErrInfo());
                if (answer.errorCode == 0) {
                    client = utils.client;
                    client.setErrorRecoder(voicerecoder);
                    RTMStruct.RTMAnswer rtmAnswer = client.setUserInfo(nickName, "");
//                    Log.d(TAG, "login: nick = " + nickName + " info = " + rtmAnswer.getErrInfo());
                    realEnterRoom(activityRoom);
                } else {
                    TestVoiceActivity.this.runOnUiThread(() -> {
                        createDialog(answer.errorMsg);
                    });
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (logView) {
                    logView.setText("");
                }
            }
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (rttcclient == null)
                    rttcclient = TCPClient.create(utils.rtmEndpoint);
                Quest quest = new Quest("*ping");
                long sendTime = System.currentTimeMillis();
                rttcclient.sendQuest(quest, new FunctionalAnswerCallback() {
                    @Override
                    public void onAnswer(Answer answer, int errorCode) {
                        long recieveTime = System.currentTimeMillis();
                        long RTTTime = recieveTime - sendTime;
//                        mylog.log("*ping");
                        showRTMRTT(RTTTime);
//                        tcpRTTshow.setText("RTM:" + RTTTime);
                    }
                });
                long udpTime = RTCEngine.getRTTTime();
                showRTCRTT(udpTime);
            }
        }, 1, 2000);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                leaveRoom();
                finish();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                if (client == null || !client.isOnline())
                    return;
                setSpeakerStatus();
            }
        });


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client == null || !client.isOnline() || activityRoom == 0)
                    return;
                setMicStatus(!micStatus);
            }
        });
        handler.postDelayed(runnable, 1000);

        RecyclerView recyclerView = findViewById(R.id.room_member_recycle);
        recyclerView.setLayoutManager(new MyGridLayoutManager(this, 2));
        voiceMemberAdapter = new VoiceMemberAdapter(this, memberlist);
        recyclerView.setAdapter(voiceMemberAdapter);

//        initMediaPlayer();

    }

    private void createDialog(String msg) {
        try {
            if (this.isFinishing()) {
                Log.d(TAG, "activity is finishing");
                return;
            }
            AlertDialog dialog = new AlertDialog.Builder(this).
                    setTitle("进入房间").
                    setCancelable(false).setMessage(msg).
                    setPositiveButton("确 认", (dlg, whichButton) -> {
                        finish();
                    }).create();
            dialog.show();
        } catch (Exception e) {
            Log.d(TAG, "createDialog: " + e.getMessage());
        }
    }

    public class TestErrorRecorderVoice extends ErrorRecorder {
        public TestErrorRecorderVoice() {
            super.setErrorRecorder(this);
        }

        @Override
        public void recordError(Exception e) {
            String msg = "Exception:" + e;
            addLog(msg);
        }

        @Override
        public void recordError(String message) {
            addLog(message);
        }

        @Override
        public void recordError(String message, Exception e) {
            String msg = String.format("Error: %s, exception: %s", message, e);
            addLog(msg);
        }
    }

    void closeInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
    }

    void setMicStatus(boolean status) {
        if (!status) {
            client.closeMic();
            muteTextView.setText("麦克风关闭");
            muteImageView.setSelected(true);
        } else {
            client.openMic();
            muteTextView.setText("麦克风开启");
            muteImageView.setSelected(false);
        }
        micStatus = status;
    }

    void setSpeakerStatus() {
        usespeaker = !usespeaker;
        if (!usespeaker) {
            speakerImageView.setSelected(false);
        } else {
            speakerImageView.setSelected(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (usespeaker)
                    client.switchOutput(true);
                else
                    client.switchOutput(false);
            }
        }).start();
    }


    void startVoice(RTMStruct.RoomInfo info, long roomId) {
        startVoice(roomId, false, info);
    }

    void startVoice(long roomId, boolean relogin, RTMStruct.RoomInfo info) {
        activityRoom = roomId;
        RTMStruct.RTMAnswer ret = client.setActivityRoom(activityRoom);
        if (ret.errorCode != 0) {
            addLog("set activity error " + ret.getErrInfo());
            return;
        }
        if (!relogin) {
            client.openMic();
        } else {
            if (!usespeaker) {
                client.switchOutput(false);
            }
            if (micStatus) {
                client.openMic();
            } else {
                client.closeMic();
            }
        }

        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chronometer.start();
                memberlist.clear();
                roomshow.setText("房间id-" + roomId + "    用户-" + nickName + "(" +client.getUid()+")");
                RTMStruct.PublicInfo userPublicInfo = client.getUserPublicInfo(info.uids);
                if (info != null && info.uids != null) {
                    info.uids.forEach(aLong -> {
                        VoiceMember m = new VoiceMember(aLong, userPublicInfo.publicInfos.get(String.valueOf(aLong)));
                        if (aLong != userid) memberlist.add(m);
                    });
                    voiceMemberAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void showRTCRTT(long rtt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                udpRTTshow.setText("RTC:" + rtt);
            }
        });
    }

    public void showRTMRTT(long rtt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tcpRTTshow.setText("RTM:" + rtt);
            }
        });
    }

    void realEnterRoom(final long roomId) {
        client.enterRTCRoom(roomId, "",new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    if (info.roomTyppe != 1){//不是音频房间
                        utils.alertDialog(TestVoiceActivity.this,"房间 " + roomId  + "为视频房间 请重新选择视频通话进入");
                        return;
                    }
                    startVoice(info, roomId);
                } else if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value()) {
                    client.createRTCRoom(roomId,1,new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            if (answer.errorCode == 0) {
                                startVoice(info, roomId);
                            } else if ( answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_EXIST.value()){
                                client.enterRTCRoom(roomId, "",new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                    @Override
                                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                        if (answer.errorCode == 0) {
                                            startVoice(info, roomId);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    utils.alertDialog(TestVoiceActivity.this,"进入音频房间" + roomId + "失败：" + answer.getErrInfo());
                }
            }
        });
    }

    protected void customToolbarAndStatusBarBackgroundColor(boolean darkTheme) {
        int toolbarBackgroundColorResId = darkTheme ? R.color.purple_500 : R.color.white;
        setTitleBackgroundResource(toolbarBackgroundColorResId, darkTheme);
    }

    /**
     * 设置状态栏和标题栏的颜色
     *
     * @param resId 颜色资源id
     */
    protected void setTitleBackgroundResource(int resId, boolean dark) {
        toolbar.setBackgroundResource(resId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, resId));
        }
        setStatusBarTheme(this, dark);
    }

    /**
     * Changes the System Bar Theme.
     */
    public static void setStatusBarTheme(final Activity pActivity, final boolean pIsDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Fetch the current flags.
            final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
            // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
            pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
        }
    }

    private VoiceMember containsMember(long userid) {
        for (VoiceMember member: memberlist){
            if (member.getUid() == userid){
                return member;
            }
        }
        return  null;
    }

    private void removeMember(long userid) {
        memberlist.removeIf(item -> item.getUid() == userid);
    }

    private void setVoice(long userid) {
        memberlist.forEach(voiceMember -> {
            if (userid == voiceMember.getUid()) {
                voiceMember.setPreviousVoiceTime(System.currentTimeMillis());
            }
        });
    }
}
