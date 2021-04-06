package com.example.rtvdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.rtmsdk.RTMClient;
import com.rtmsdk.RTMPushProcessor;
import com.rtmsdk.RTMStruct;
import com.rtmsdk.UserInterface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class RTV extends Activity {
    final String endpoint = "161.189.171.91:13325";
    final long pid = 11000002;

    TextView uidtext;
    TextView textuid2;
    Random rand = new Random();
    int REQUEST_CODE_CONTACT = 101;
    public AtomicLong currRoom = new AtomicLong(0);

    TestErrorRecorder mylogRecoder = new TestErrorRecorder();
    long uid;
    EditText utext;
    Context mycontext = this;
    RTMClient client;
    public boolean micStatus = false;
    TextView logview;
    ImageView laba;
    ImageView mic;
    boolean micphoneStatus = false;
    boolean voiceStatus = false;
    Object interLock = new Object();

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }


    void astTake(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (interLock) {
                    logview.append(msg + "\n");
                }
            }
        });
    }

    final String[] buttonNames = {"create", "enter", "leave", "login", "clear", "roommembers", "miclevel"};

    public class TestErrorRecorder extends ErrorRecorder {
        public TestErrorRecorder() {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    class RTMExampleQuestProcessor extends RTMPushProcessor {
        String msg = "";

        public boolean reloginWillStart(long uid, RTMStruct.RTMAnswer answer, int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            msg = userInfo() + " 开始重连第 " + reloginCount + "次,结果 " + answer.getErrInfo();
            astTake(msg);
            return true;
        }

        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            msg = userInfo() + " 重连结束 结果 " + transRet(answer);
            astTake(msg);
            if (successful) {
                final long id = currRoom.get();
                if (id <= 0)
                    return;
                RTMStruct.RTMAnswer answerEnter = client.enterVoiceRoom(id);
                if (answerEnter.errorCode != 0) {
                    msg = userInfo() + "重新进入房间 " + id + answer.getErrInfo();
                } else {
                    client.setActivityRoom(id);
                    voiceStatus = true;
                    msg = userInfo() + "重新进入房间 " + id + " 成功";
                    RTV.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textuid2.setText("房间id " + id);
                            laba.setBackgroundResource(R.drawable.voiceopen);
                            mic.setBackgroundResource(R.drawable.micclose);
                        }
                    });
                }
                astTake(msg);
            } else {
                laba.setBackgroundResource(R.drawable.voiceclose);
                mic.setBackgroundResource(R.drawable.micclose);
                currRoom.set(-1);
                micphoneStatus = false;
                micStatus = false;
            }
        }

        public void rtmConnectClose(long uid) {
            RTV.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (interLock) {
                        logview.append("RTM链接断开\n");
                    }
                    if (currRoom.get() > 0) {
                        textuid2.setText("");
                        laba.setBackgroundResource(R.drawable.voiceclose);
                        mic.setBackgroundResource(R.drawable.micclose);
                    }
                }
            });
        }

        public void kickout() {
            RTV.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (interLock) {
                        logview.append("Received kickout.\n");
                        currRoom.set(-1);
                    }
                }
            });
        }


        @Override
        public void pushPullRoom(final long roomId, RTMStruct.RTMAnswer answer) {
            RTV.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currRoom.set(roomId);
                    client.setActivityRoom(roomId);
                    client.getVoiceRoomMembers(new UserInterface.IRTMDoubleValueCallback<HashSet<Long>, HashSet<Long>>() {
                        @Override
                        public void onResult(final HashSet<Long> longs, HashSet<Long> longs2, final RTMStruct.RTMAnswer answer) {
                            RTV.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (answer.errorCode == 0) {
                                        laba.setBackgroundResource(R.drawable.voiceopen);
                                        mic.setBackgroundResource(R.drawable.micclose);
                                        currRoom.set(roomId);
                                    }
                                }
                            });
                        }
                    }, roomId);
                }
            });
            astTake("user " + uid + "被拉入房间 " + roomId);
        }

        @Override
        public void pushEnterVoiceRoom(final long roomId, final long userId, long time) {
            astTake("user " + userId + "进入房间 " + roomId);
        }

        @Override
        public void pushExitVoiceRoom(final long roomId, final long userId, long time) {
            astTake("user " + userId + "退出房间 " + roomId);
        }

        @Override
        public void pushVoiceRoomClosed(long roomId) {
            astTake("房间 " + roomId + "已关闭 ");
        }

        @Override
        public void pushKickoutVoiceRoom(final long roomId) {
            currRoom.set(-1);
            astTake("被踢出语音房间 " + roomId);
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

    public void alertInputDialog() {
        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                long rid = Long.parseLong(inputServer.getText().toString());
            }
        });
        builder.show();
    }


    String transRet(RTMStruct.RTMAnswer answer) {
        return (answer.errorCode == 0 ? "成功" : "失败-" + answer.getErrInfo());
    }


    int getuid() {
        return rand.nextInt(20000 - 1 + 1) + 1;
    }

    void addLog(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (interLock) {
                    logview.append(msg + "\n");
                }
            }
        });
    }

    void closeInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(RTV.this.getWindow().getDecorView().getWindowToken(), 0);
    }

    boolean checkClient() {
        if (client == null || !client.isOnline()) {
            alertDialog("请先登录");
            return false;
        }
        return true;
    }

    String userInfo() {
        return "用户 " + uid + " ";
    }

    class TestButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.clear:
                    synchronized (interLock) {
                        logview.setText("");
                    }
                    break;
                case R.id.login:
                    closeInput();

                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] permissions = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
                        //验证是否许可权限
                        for (String str : permissions) {
                            if (RTV.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                //申请权限
                                RTV.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                            }
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                                    MainActivity.this.checkVersion();
                            if (client != null) {
                                if (client.isOnline()) {
                                    client.bye(false);
                                }
                            } else {
                                uid = getuid();
                                client = new RTMClient(endpoint, pid, uid, new RTMExampleQuestProcessor(), (Activity) mycontext);
                                if (client == null) {
                                    addLog("RTMclient " + "初始化失败");
                                    return;
                                }
                            }
                            login();
                        }
                    }).start();
                    break;
                case R.id.leave:
                    if (!checkClient())
                        return;

                    client.leaveVoiceRoom(new UserInterface.IRTMEmptyCallback() {
                        @Override
                        public void onResult(RTMStruct.RTMAnswer answer) {
                        }
                    }, currRoom.get());
                    astTake(userInfo() + "离开房间 " + currRoom.get());

                    RTV.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            laba.setBackgroundResource(R.drawable.voiceclose);
                            mic.setBackgroundResource(R.drawable.micclose);
                            currRoom.set(-1);
                            micphoneStatus = false;
                            micStatus = false;
                            textuid2.setText("");
                        }
                    });

                    break;
                case R.id.miclevel:
                    final EditText inputmiclevel = new EditText(mycontext);
                    AlertDialog.Builder buildermic = new AlertDialog.Builder(mycontext);
                    buildermic.setTitle("请输入麦克风增益倍数").setIcon(android.R.drawable.ic_dialog_info).setView(inputmiclevel)
                            .setNegativeButton("取消", null);
                    buildermic.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (client == null) {
                                alertDialog("请先登录");
                                return;
                            }
                            if (inputmiclevel.getText().toString().isEmpty()) {
                                return;
                            }
                            int miclevel = 0;
                            try {
                                miclevel = Integer.parseInt(inputmiclevel.getText().toString());
                            } catch (NumberFormatException e) {
                                alertDialog("请输入数字");
                                return;
                            }
                            RTCEngine.setMicphoneGain(miclevel);
                            addLog("设置麦克风增益等级:" + miclevel);
                        }
                    });
                    buildermic.show();
                    break;
                case R.id.create:
                    final EditText inputServer = new EditText(mycontext);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mycontext);
                    builder.setTitle("请输入房间号").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                            .setNegativeButton("取消", null);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (client == null) {
                                alertDialog("请先登录");
                                return;
                            }
                            if (inputServer.getText().toString().isEmpty()) {
                                alertDialog("请输入正确的房间号");
                                return;
                            }
                            long enterid = 0;
                            try {
                                enterid = Long.parseLong(inputServer.getText().toString());
                            } catch (NumberFormatException e) {
                                alertDialog("请输入正确的房间号");
                                return;
                            }
                            final long inputRid = enterid;
                            if (currRoom.get() > 0) {
                                client.leaveVoiceRoom(currRoom.get());
                                astTake(userInfo() + "离开房间 " + currRoom.get());
                            }
                            client.createVoiceRoom(new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {
                                    addLog("createVoiceRoom roomid " + inputRid + " " + transRet(answer));
                                    if (answer.errorCode == 0) {
                                        RTV.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                voiceStatus = true;
                                                currRoom.set(inputRid);
                                                textuid2.setText("房间id " + inputRid);
                                                client.setActivityRoom(inputRid);
                                                laba.setBackgroundResource(R.drawable.voiceopen);
                                                mic.setBackgroundResource(R.drawable.micclose);
                                            }
                                        });

                                    }
                                }
                            }, inputRid);
                        }
                    });
                    builder.show();
                    break;
                case R.id.enter:
                    final EditText inputServer1 = new EditText(mycontext);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mycontext);
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

                            if (currRoom.get() == inputRid1) {
                                astTake("进入房间  " + inputRid1 + "成功");
                                return;
                            }

                            if (currRoom.get() > 0) {
                                client.leaveVoiceRoom(currRoom.get());
                                astTake(userInfo() + "离开房间 " + currRoom.get());
                            }
                            client.enterVoiceRoom(new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {
                                    astTake("进入房间  " + inputRid1 + " " + transRet(answer));
                                    if (answer.errorCode == 0) {
                                        currRoom.set(inputRid1);
                                        RTMStruct.RTMAnswer bb = client.setActivityRoom(inputRid1);
                                        if (bb.errorCode != 0) {
                                            addLog("设置活跃房间失败");
                                            return;
                                        }
                                        RTV.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                voiceStatus = true;
                                                micStatus = false;
                                                textuid2.setText("房间id-" + inputRid1);
                                                laba.setBackgroundResource(R.drawable.voiceopen);
                                                mic.setBackgroundResource(R.drawable.micclose);
                                            }
                                        });
                                    }
                                }
                            }, inputRid1);
                        }
                    });
                    builder1.show();
                    break;
                case R.id.roommembers:
                    if (!checkClient())
                        return;
                    RTMStruct.RoomInfo answer1 = client.getVoiceRoomMembers(currRoom.get());
                    if (answer1.errorCode != 0)
                        astTake("获取房间成员列表失败 " + answer1.getErrInfo());
                    else
                        astTake("房间 " + currRoom.get() + " 成员列表:" + " " + answer1.uids.toString());
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.bye();
    }


    public String getToken() {
        TCPClient kk = TCPClient.create("161.189.171.91:13777", true);
        Quest ll = new Quest("getUserToken");
        String gettoken = "";
        ll.param("pid", pid);
        ll.param("uid", uid);
        try {
            Answer ret = kk.sendQuest(ll, 10);
            if (ret.getErrorCode() == 0) {
                gettoken = ret.wantString("token");
                if (gettoken.isEmpty()) {
                    alertDialog("getUserToken is empty");
                }
            } else
                alertDialog("getUserToken " + ret.getErrorCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gettoken;
    }

    void login() {
        String token = getToken();
        if (token.isEmpty())
            return;
        RTMStruct.RTMAnswer answertoken = client.login(token);
        if (answertoken.errorCode == 0) {
            RTMStruct.RTMAnswer answer1 = client.initRTMVoice();
            if (answer1.errorCode != 0) {
                alertDialog("初始化音频 " + answer1.getErrInfo());
                return;
            }
            addLog("RTM登陆成功");
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uidtext.setText("用户id-" + uid);
                }
            });
        } else {
            addLog("RTM登录失败 " + answertoken.getErrInfo());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        laba = $(R.id.laba);
        mic = $(R.id.mic);
        uidtext = $(R.id.textuid);
        textuid2 = $(R.id.textuid2);

        laba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                voiceStatus = !voiceStatus;
                RTMStruct.RTMAnswer ret = client.setVoiceStat(voiceStatus);
                if (ret.errorCode != 0) {
                    addLog("setVoiceStat error " + ret.getErrInfo());
                    return;
                }
                if (!voiceStatus) {
                    micStatus = false;
                    addLog("关闭语音");
                    laba.setBackgroundResource(R.drawable.voiceclose);
                    mic.setBackgroundResource(R.drawable.micclose);
                } else {
                    addLog("打开语音");
                    laba.setBackgroundResource(R.drawable.voiceopen);
                }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                if (!voiceStatus)
                    return;
                if (currRoom.get() <= 0) {
                    alertDialog("请先进入房间");
                    return;
                }
                micStatus = !micStatus;
                RTMStruct.RTMAnswer answermi = client.canSpeak(micStatus);
                if (answermi.errorCode != 0) {
                    addLog(" set micStatus error " + answermi.getErrInfo());
                    return;
                }
                if (!micStatus) {
                    mic.setBackgroundResource(R.drawable.micclose);
                    addLog("关闭麦克风");
                } else {
                    mic.setBackgroundResource(R.drawable.micopen);
                    addLog("打开麦克风");
                }
            }
        });

        logview = $(R.id.logview);
        logview.setTextSize(14);
        logview.setMovementMethod(ScrollingMovementMethod.getInstance());

        TestButtonListener testButtonListener = new TestButtonListener();

        for (String name : buttonNames) {
            int buttonId = getResources().getIdentifier(name, "id", getBaseContext().getPackageName());
            Button button = $(buttonId);
            button.setTextSize(14);
            button.setOnClickListener(testButtonListener);
        }
    }
}