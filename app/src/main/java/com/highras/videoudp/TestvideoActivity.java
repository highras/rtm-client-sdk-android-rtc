package com.highras.videoudp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fpnn.sdk.ErrorRecorder;
import com.livedata.rtc.RTCEngine;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TestvideoActivity extends Activity {

    long cpuusage = 0;
    long cputtotal = 0;
    long cpuidle = 0;
    AtomicBoolean run = new AtomicBoolean(false);


    public  int getMemPer(){
        if (mycontext==null){
            return 0;
        }
        ActivityManager activityManager= (ActivityManager)mycontext.getSystemService(Context.ACTIVITY_SERVICE);
        int[] pids=new int[1];
        pids[0]= android.os.Process.myPid();
        Debug.MemoryInfo[] processMemoryInfo = activityManager.getProcessMemoryInfo(pids);
        Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
        Debug.getMemoryInfo(memoryInfo);
        int totalPss = memoryInfo.getTotalPss();
        return totalPss;//单位kb
    }


    public  long readUsage()
    {
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/stat" ) ), 1000 );
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);
            long sum=(currTotal - cputtotal + currIdle - cpuidle);
            if (sum>0){
                cpuusage =(int)((currTotal - cputtotal) * 100.0f /sum) ;
            }
            cputtotal = currTotal;
            cpuidle = currIdle;
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
        }
        return cpuusage;
    }


    Utils  testUtils;
    SoundPool soundPool;
    MediaPlayer mediaPlayer;
    final String[] buttonNames = {"enter", "leave", "login", "clear","subscribe","unsubscribe"};
//    private SurfaceView decodeSurfaceView = null;
    private SurfaceView previewSurfaceView = null;
    RTMPushProcessor videopush =     new RTMVideoProcessor();
    public TestErrorRecorderVideo videorecoder = new TestErrorRecorderVideo();
    private HashMap<Long, SurfaceView> userSurfaces = new HashMap<>();
    LinearLayout surfaceshow;
    ConstraintLayout alllayout;
  //    private int viewHeght;
//    private int viewWidth;
    public AtomicLong videoRoom = new AtomicLong(0);
    public  static  boolean running = false;
    TextView logview;
    boolean micStatus = false;
    boolean voiceStatus = false;
    boolean usespeaker = true;
    ToggleButton checkbutton;
    public boolean cameraOpen = false;
    boolean useFront =  true;
    ImageView cameraSwitch;
    ImageView laba;
//    ImageView speakers;
    int agclevel = 0;
    TextView showbeishu;
    ImageView cameraStatus;
    HashSet<Long> subuids = new HashSet<>();
    ImageView mic;
    SeekBar agc;
    Context mycontext = this;
    Activity myactivity = this;
    int REQUEST_CODE_CONTACT = 101;
    TextView uidtext;
    CheckBox channellNum;
    TextView textuid2;
    RTMClient client;
    Spinner roomMembers, videoSelect;
    List<CItem > lst = new ArrayList<CItem>();
    ArrayList<String> video = new ArrayList<String>();
    ArrayList<String> roomMembervalue = new ArrayList<String>();
    ArrayAdapter adapterMembers;


    public class CItem {
        private int ID = 0;
        private String Value = "";
        public CItem () {
            ID = 0;
            Value = "";
        }
        public CItem (int _ID, String _Value) {
            ID = _ID;
            Value = _Value;
        }
        @Override
        public String toString() {           //为什么要重写toString()呢？因为适配器在显示数据的时候，如果传入适配器的对象不是字符串的情况下，直接就使用对象.toString()
            // TODO Auto-generated method stub
            return Value;
        }

        public int getID() {
            return ID;
        }

        public String getValue() {
            return Value;
        }
    }

    String userInfo() {
        return "用户 " + testUtils.uid + " ";
    }

    String transRet(RTMStruct.RTMAnswer answer) {
        return (answer.errorCode == 0 ? "成功" : "失败-" + answer.getErrInfo());
    }


    public void ClearDraw(){
        Canvas canvas = null;
        try{
            canvas = previewSurfaceView.getHolder().lockCanvas(null);
            canvas.drawColor(Color.WHITE);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        }catch(Exception e){
        }finally{
            if(canvas != null){
                previewSurfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }
    
    class RTMVideoProcessor extends RTMPushProcessor {
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
                final long id = videoRoom.get();
                if (id <= 0)
                    return;
                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode != 0) {
                            msg = userInfo() + "重新进入房间 " + id + answer.getErrInfo();
                        } else {
                            msg = userInfo() + "重新进入房间 " + id + " 成功";
                            if (cameraOpen)
                                client.openCamera();
                            TestvideoActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textuid2.setText("房间id " + id);
                                    mic.setBackgroundResource(R.drawable.micclose);
                                    roomMembervalue.clear();
                                    for (Long id: roomInfo.uids){
                                        roomMembervalue.add(String.valueOf(id));
                                    }
//                                    adapterMembers.clear();
//                                    adapterMembers.addAll(roomInfo.uids);
                                    adapterMembers.notifyDataSetChanged();
                                }
                            });
                            if (!userSurfaces.isEmpty()) {
                                RTMStruct.RTMAnswer subanswer = client.subscribeVideo(videoRoom.get(), userSurfaces);
                                addLog("订阅 " + userSurfaces.keySet().toString() + " 视频流 " + transRet(subanswer));
                            }
                        }
                        addLog(msg);
                    }
                },id);
            } else {
                subuids.clear();
                mic.setBackgroundResource(R.drawable.micclose);
                cameraStatus.setBackgroundResource(R.drawable.cameraclose);
                videoRoom.set(0);
                micStatus = false;
            }
        }

        public void rtmConnectClose(long uid) {
            TestvideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (logview) {
                        logview.append("RTM链接断开\n");
                    }
                    if (videoRoom.get() > 0) {
                        textuid2.setText("");
                        cameraStatus.setBackgroundResource(R.drawable.cameraclose);
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
                    videoRoom.set(0);
                    cameraStatus.setBackgroundResource(R.drawable.cameraclose);
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
                    if (userSurfaces.containsKey(userId)) {
                        try {
                            surfaceshow.removeView(userSurfaces.get(userId));
                        }
                        catch (Exception e){

                        }
                        userSurfaces.remove(userId);
                    }
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
        run.set(false);
        testUtils.rtmClient.leaveRTCRoom(videoRoom.get());
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
        testUtils = new Utils(videorecoder,videopush,this,"nx");
        testUtils.errorRecorder = videorecoder;
        client = testUtils.rtmClient;
        logview = $(R.id.logview);
        logview.setTextSize(14);
        logview.setTextColor(this.getResources().getColor(R.color.white));
        logview.setMovementMethod(ScrollingMovementMethod.getInstance());
        agc = $(R.id.huatiao);
        showbeishu = $(R.id.showbeishu);
        laba = $(R.id.laba);
        run.set(true);
//        speakers = $(R.id.speaker);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

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

//
//        mediaPlayer = MediaPlayer.create(this, R.raw.dearjohn);
//        mediaPlayer.start();
       /* SoundPool.Builder builder = new SoundPool.Builder();
        //传入最多播放音频数量,
        builder.setMaxStreams(10);
        //AudioAttributes是一个封装音频各种属性的方法
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        //设置音频流的合适的属性
        attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        //加载一个AudioAttributes
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        final int voiceId = soundPool.load(mycontext, R.raw.testmusic, 1);
        //异步需要等待加载完成，音频才能播放成功
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    //第一个参数soundID
                    //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
                    //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
                    //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
                    //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
                    //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
                    soundPool.play(voiceId, 0.5f, 0.5f, 1, 1, 1);
                }
            }
        });*/



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

//        speakers.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!checkClient())
//                    return;
//                setSpeakerStatus();
//            }
//        });


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

        surfaceshow = $(R.id.surfaceshow);
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
        videoSelect = $(R.id.videoLevel);
        CItem  ct = new CItem (1,"低");
        CItem  ct1 = new CItem (2,"中");
        CItem  ct2 = new CItem (3,"高");
        lst.add(ct);
        lst.add(ct1);
        lst.add(ct2);
        ArrayAdapter<CItem > AdapterVideo = new ArrayAdapter<CItem>(this,
                android.R.layout.simple_spinner_item, lst);

        videoSelect.setAdapter(AdapterVideo);

        videoSelect.setSelection(0, true);
        videoSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Field field =       AdapterView.class.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);  //设置mOldSelectedPosition可访问
                    field.setInt(videoSelect, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
                } catch (Exception e) {
                    e.printStackTrace();
                }

                CItem tt = (CItem)videoSelect.getSelectedItem();
//                mylog.log(tt.getID() + "");
                client.switchVideoQuality(tt.getID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

        cameraSwitch = $(R.id.cameraswitch);
        cameraStatus = $(R.id.camerastatus);

        previewSurfaceView = $(R.id.previewsurface);
        previewSurfaceView.setZOrderOnTop(true);

        previewSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

//        decodeSurfaceView = $(R.id.ndksurface);

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
                if (videoRoom.get() <= 0) {
                    alertDialog("请先进入房间");
                    return;
                }
                setMicStatus(!micStatus);
            }
        });

        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cameraOpen)
                    return;
                useFront = !useFront;
                myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectAnimator    animator = ObjectAnimator.ofFloat(cameraSwitch, "rotation", 0f, 360.0f);
//                        animator.setDuration(2000);
//                        animator.setInterpolator(new LinearInterpolator());//不停顿
//                        animator.setRepeatCount(-1);//设置动画重复次数
//                        animator.setRepeatMode(ValueAnimator.RESTART);//动画重复模式

                        animator.start();//开始动画
                    }catch (Exception e){
                        mylog.log("出错了 " +e.getMessage());
                    }
                }
            });

                RTCEngine.switchCamera(useFront);
            }
        });

        cameraStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkClient())
                    return;
                if (videoRoom.get() <= 0) {
                    alertDialog("请先进入房间");
                    return;
                }
                setCameraStatus(!cameraOpen);
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

    public class TestErrorRecorderVideo extends ErrorRecorder {
        public TestErrorRecorderVideo() {
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
        client.leaveRTCRoom(videoRoom.get());
        addLog( "离开房间 " + videoRoom);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userSurfaces.clear();
                videoRoom.set(0);
//                setVoiceStatus(false);
                cameraStatus.setBackgroundResource(R.drawable.cameraclose);
                cameraOpen = false;
                mic.setBackgroundResource(R.drawable.micclose);
                laba.setBackgroundResource(R.drawable.voiceclose);
                micStatus = false;
                textuid2.setText("");
                adapterMembers.clear();
                adapterMembers.notifyDataSetChanged();
                surfaceshow.removeAllViews();
//                ClearDraw();
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

            CItem tt = (CItem)videoSelect.getSelectedItem();
            client.switchVideoQuality(tt.getID());
//            RTMStruct.RTMAnswer jj = client.initRTMVideo(ret,previewSurfaceView, RTMStruct.CaptureLevle.Normal.value());
            RTMStruct.RTMAnswer jj = client.initRTMVideo(ret,previewSurfaceView, tt.getID());
            if (jj.errorCode != 0 ){
                addLog("初始化 视频失败 " + jj.getErrInfo());
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
//            myactivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        ObjectAnimator    animator = ObjectAnimator.ofFloat(previewSurfaceView, "rotation", 0f, 360.0f);
//                        animator.setDuration(2000);
//                        animator.setInterpolator(new LinearInterpolator());//不停顿
//                        animator.setRepeatCount(-1);//设置动画重复次数
//                        animator.setRepeatMode(ValueAnimator.RESTART);//动画重复模式
//
//                        animator.start();//开始动画
//                    }catch (Exception e){
//                        mylog.log("出错了 " +e.getMessage());
//                    }
//                }
//            });

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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            login();
                        }
                    }).start();
                    break;
                case R.id.leave:
//                    int jjjj = am.getMode();
//                    mylog.log("current mode is " + jjjj);
                    if (!checkClient())
                        return;
                    if (videoRoom.get() <= 0 )
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
                case R.id.subscribe:
                    if (roomMembers.getSelectedItem() == null)
                        return;
                    long selectMember = Long.parseLong(roomMembers.getSelectedItem().toString());
                    if (!userSurfaces.containsKey(selectMember)){
                    SurfaceView kk = new SurfaceView(mycontext);
//                    ConstraintLayout.LayoutParams tmp = new ConstraintLayout.LayoutParams(0,0);
//                    tmp.startToStart = R.id.surfaceshow;
//                    tmp.endToEnd = R.id.surfaceshow;

//                        LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,640);
                        int  realwidth = surfaceshow.getWidth();
                        int width = 480;
                        int height = 640;

                        int jj = surfaceshow.getWidth();
                        if (jj >300 && jj< 480) {
                            width = (jj/3) * 3;
                            height = (jj/3) * 4;
                        }
                        LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(width,height);
                        if (realwidth > width)
                            tmp.leftMargin = (realwidth-width)/2;
                        surfaceshow.addView(kk,tmp);

//                    inal LinearLayout.LayoutParams lpWW = new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);


//                    LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(viewWidth,viewHeght);
//                    kk.setLayoutParams(new LinearLayout.LayoutParams(viewWidth,viewHeght));
//                    ViewGroup.LayoutParams ll = new LinearLayout.LayoutParams();
//                    ll.height = 320;
//                    ll.width =  240;
//                    kk.setLayoutParams(ll);

                    kk.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                            kk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
//                                    try {
//                                        for (Long id:userSurfaces.keySet()){
//                                            SurfaceInfo kk = userSurfaces.get(id);
//                                            if (kk.surfaceView.equals(view)){
//                                                view.setScaleY(2.5f);
//                                                kk.decodeSurface.removeView(view);
//                                                SurfaceView kk1= new SurfaceView(mycontext);
//                                                kk1.setZOrderOnTop(true);
//                                                alllayout.addView(kk1,new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
//                                                kk1.setZOrderMediaOverlay(true);
//                                            }
//                                        }
//                                    }
//                                    catch (Exception e){
//                                    }

                                }
                                });
                            RTMStruct.RTMAnswer jj = client.subscribeVideo(videoRoom.get(), new HashMap<Long, SurfaceView>() {{
                                put(selectMember, kk);
                            }});
                            if (jj.errorCode ==  0) {
                                userSurfaces.put(selectMember, kk);
                            }
                            addLog("订阅 "+ selectMember +  " 视频流 " + transRet(jj));
                        }

                        @Override
                        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//                            mylog.log("surfaceChanged haha");

                        }

                        @Override
                        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

                        }
                    });}
                    else {
                        RTMStruct.RTMAnswer tt = client.subscribeVideo(videoRoom.get(), new HashMap<Long, SurfaceView>() {{
                            put(selectMember, userSurfaces.get(selectMember));
                        }});
                        addLog("订阅 "+ selectMember +  " 视频流 " + transRet(tt));
                    }
                    break;
                case R.id.unsubscribe:
                    if (roomMembers.getSelectedItem() == null)
                        return;
                    long selectMember1 = Long.parseLong(roomMembers.getSelectedItem().toString());
                    client.unsubscribeVideo(videoRoom.get(), new HashSet<Long>(){{add(selectMember1);}});
                    addLog("取消订阅 "+ selectMember1 +  " 视频流 ");
                    if (userSurfaces.containsKey(selectMember1)) {
                        try {
                            surfaceshow.removeView(userSurfaces.get(selectMember1));
                        }
                        catch (Exception e){

                        }
                        userSurfaces.remove(selectMember1);
                    }
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
//        RTMStruct.RTMAnswer ret = null;
//        usespeaker = !usespeaker;
//        if (usespeaker)
//            ret = client.openSpeaker();
//        else
//            ret = client.openEarpiece();
//
//        if (ret.errorCode != 0){
//            addLog("setSpeakerStatus:" +  usespeaker + " error " + ret.getErrInfo() );
//            return;
//        }
//        if(!usespeaker) {
//            micStatus = false;
//            addLog("打开听筒");
//            speakers.setBackgroundResource(R.drawable.speakeroff);
//        }
//        else {
//            addLog("打开扬声器");
//            speakers.setBackgroundResource(R.drawable.speakeron);
//        }
    }

    void setCameraStatus(boolean status){
        cameraOpen = status;
        if (!cameraOpen) {
            cameraStatus.setBackgroundResource(R.drawable.cameraclose);
            addLog("关闭摄像头");
            client.closeCamera();
        } else {
            cameraStatus.setBackgroundResource(R.drawable.cameraopen);
            addLog("打开摄像头");
            client.openCamera();
        }
    }

    void realEnterRoom(final long roomId){
        client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    addLog("进入房间  " + roomId + " " + transRet(answer));
                    videoRoom.set(roomId);
                    myactivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            laba.setBackgroundResource(R.drawable.voiceopen);
                            voiceStatus = true;
                            textuid2.setText("房间id-" + roomId);
                            setMicStatus(true);
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
                                videoRoom.set(roomId);
                                myactivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        laba.setBackgroundResource(R.drawable.voiceopen);
                                        voiceStatus = true;
                                        setMicStatus(true);
                                        textuid2.setText("房间id-" + roomId);
                                        adapterMembers.clear();
                                        adapterMembers.addAll(roomInfo.uids);
                                        adapterMembers.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    },roomId, 2,1);
                }
            }
        }, roomId);
    }
}
