package com.highras.voiceDemo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.fpnn.sdk.ErrorRecorder;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringJoiner;

public class TestVoiceActivity extends AppCompatActivity {
    RTMPushProcessor voicepush = new RTMVoiceProcessor();
    public TestErrorRecorderVoice voicerecoder = new TestErrorRecorderVoice();
    ConstraintLayout alllayout;
    //    private int viewHeght;
//    private int viewWidth;
    public static boolean running = false;
    boolean micStatus = false;
    boolean usespeaker = true;
    LinearLayout leave;
    LinearLayout mic;
    LinearLayout speaker;
    ImageView speakerImageView;
    ImageView muteImageView;
    TextView muteTextView;
    long activityRoom = 0;
    Activity myactivity = this;
    TextView roomdshow;
    RTMClient client;
    Utils utils = Utils.INSTANCE;

    long previousTime = System.currentTimeMillis();

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    class RTMVoiceProcessor extends RTMPushProcessor {
        String msg = "";

        public boolean reloginWillStart(long uid, int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            return true;
        }


        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0)
                    return;
                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode == 0) {
                            startVoice(activityRoom, true);
                        } else {
                            mylog.log("重新进入失败");
                        }
                    }
                }, activityRoom);
            } else {
                mylog.log("重新进入失败");
                mic.setBackgroundResource(R.drawable.micclose);
                micStatus = false;
            }
        }

        public void rtmConnectClose(long uid) {
            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        public void kickout() {
            mylog.log("receive kickout");
        }

        public void voiceSpeak(long[] uid) {
            myactivity.runOnUiThread(() -> {
                soundRelayout.setVisibility(View.VISIBLE);
                previousTime = System.currentTimeMillis();
                StringJoiner sj = new StringJoiner(",", "正在讲话:", "");
                for (long l : uid) {
                    sj.add(String.valueOf(l));
                }
                soundText.setText(sj.toString());
            });
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - previousTime > 2000) {
                soundRelayout.setVisibility(View.INVISIBLE);
                soundText.setText("");
            }
            handler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }

    private void leaveRoom() {
        if (client == null)
            return;
        utils.client.leaveRTCRoom(activityRoom, new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                client.closeRTM();
                client = null;
            }
        });
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

    int getLatencyTime() {
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
        return 0;
    }

    AudioManager am;
    Toolbar toolbar;

    RelativeLayout soundRelayout;
    TextView soundText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testvoice);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        customToolbarAndStatusBarBackgroundColor(true);
        mic = $(R.id.mic);
        muteImageView = $(R.id.muteImageView);
        speaker = $(R.id.speaker);
        speakerImageView = $(R.id.speakerImageView);
        leave = $(R.id.leave);
        muteTextView = $(R.id.muteTextView);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        roomdshow = $(R.id.roomnum);

        activityRoom = getIntent().getIntExtra("roomid", 0);
        speakerImageView.setSelected(true);
//        startAnimation();

        soundText = $(R.id.nameTextView);
        soundRelayout = $(R.id.sound_relayout);
        utils.login(new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                mylog.log("void login ret" + answer.getErrInfo());
                if (answer.errorCode == 0) {
                    client = utils.client;
                    realEnterRoom(activityRoom);
                }
//                client = utils.client;
            }
        }, myactivity, voicepush);

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveRoom();
                finish();
            }
        });


        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client == null || !client.isOnline())
                    return;
                setSpeakerStatus();
            }
        });


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!client.isOnline() || activityRoom == 0)
                    return;
                setMicStatus(!micStatus);
            }
        });
        handler.postDelayed(runnable, 500);
    }


    public class TestErrorRecorderVoice extends ErrorRecorder {
        public TestErrorRecorderVoice() {
            super.setErrorRecorder(this);
        }

        public void recordError(Exception e) {
            String msg = "Exception:" + e;
            mylog.log(msg);
        }

        public void recordError(String message) {
            mylog.log(message);
        }

        public void recordError(String message, Exception e) {
            String msg = String.format("Error: %s, exception: %s", message, e);
            mylog.log(msg);
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


    void startVoice(long roomId) {
        startVoice(roomId, false);
    }

    void startVoice(long roomId, boolean relogin) {
        activityRoom = roomId;
        client.setActivityRoom(activityRoom);
        RTMStruct.RTMAnswer ret = client.setVoiceStat(true);
        if (ret.errorCode != 0) {
            return;
        }
        if (relogin) {
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usespeaker = true;
                    speakerImageView.setSelected(true);
                }
            });
            return;
        }
        client.openMic();
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mic.setBackgroundResource(R.drawable.micopen);
//                speaker.setBackgroundResource(R.drawable.speakon);
                micStatus = true;
                usespeaker = true;
                roomdshow.setText("房间id-" + roomId + "    用户id-" + client.getUid());
//                speakerImageView.setSelected(true);
            }
        });
    }

    void realEnterRoom(final long roomId) {
        client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    startVoice(roomId);
                } else {
                    client.createRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            if (answer.errorCode == 0) {
                                startVoice(roomId);
                            } else {
                                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                    @Override
                                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                        if (answer.errorCode == 0) {
                                            startVoice(roomId);
                                        }
                                    }
                                }, roomId);
                            }
                        }
                    }, roomId, 1, 0);
                }
            }
        }, roomId);
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

//    private void startAnimation() {
//        ImageView laba = findViewById(R.id.laba_image);
//        AnimationDrawable animationDrawable1 = new AnimationDrawable();
//        animationDrawable1.addFrame(AppCompatResources.getDrawable(this, R.mipmap.voice_1), 200);
//        animationDrawable1.addFrame(AppCompatResources.getDrawable(this, R.mipmap.voice_2), 200);
//        animationDrawable1.addFrame(AppCompatResources.getDrawable(this, R.mipmap.voice_3), 200);
//        animationDrawable1.setOneShot(false);
//        laba.setImageDrawable(animationDrawable1);
//        animationDrawable1.start();
//    }
}
