package com.highras.capturedemo;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.HttpAuthHandler;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.rtmsdk.RTMClient;
import com.rtmsdk.RTMPushProcessor;
import com.rtmsdk.RTMStruct;
import com.rtmsdk.UserInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TestvideoActivity extends Activity {
    Utils  testUtils;
    final String[] buttonNames = {"enter", "leave", "login", "clear","subscribe","unsubscribe"};
//    private SurfaceView decodeSurfaceView = null;
    private SurfaceView previewSurfaceView = null;
    RTMPushProcessor videopush =     new RTMVideoProcessor();
    class SurfaceInfo{
        SurfaceView surfaceView;
        LinearLayout decodeSurface;
        int index;
    }

    public TestErrorRecorderVideo videorecoder = new TestErrorRecorderVideo();
    private HashMap<Long, SurfaceInfo> userSurfaces = new HashMap<>();
    LinearLayout decodeSurface1;
    LinearLayout surfaceshow;
    ConstraintLayout alllayout;
    LinearLayout decodeSurface2;
//    private int viewHeght;
//    private int viewWidth;
    public AtomicLong videoRoom = new AtomicLong(0);
    public  static  boolean running = false;
    TextView logview;
    public boolean micStatus = false;
    public boolean cameraOpen = false;
    boolean useFront =  true;
    ImageView cameraSwitch;
    ImageView cameraStatus;
    HashSet<Long> subuids = new HashSet<>();
    ImageView mic;
    Context mycontext = this;
    Activity myactivity = this;
    int REQUEST_CODE_CONTACT = 101;
    TextView uidtext;
    CheckBox channellNum;
    TextView textuid2;
    RTMClient client;
    Spinner roomMembers;
    ArrayList<String> roomvalue = new ArrayList<String>();
    ArrayList<String> roomMembervalue = new ArrayList<String>();
    ArrayAdapter adapterMembers;


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
                            HashMap lala =  new HashMap();
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
                            for (Long ud:  userSurfaces.keySet()) {
                                SurfaceInfo kk = userSurfaces.get(ud);
                                lala.put(ud,kk.surfaceView.getHolder().getSurface());
                            }
                            client.subscribeVideo(new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {
                                    addLog("订阅 " + lala.keySet().toString() + " 视频流 " + transRet(answer));
                                }
                            },videoRoom.get(), lala);
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
                    if (!roomMembervalue.contains(userId)) {
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
                    SurfaceInfo  lala =  userSurfaces.get(userId);
                    if (lala != null) {
                        try {
                            lala.decodeSurface.removeView(lala.surfaceView);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.testvideo);

        testUtils = new Utils(videorecoder,videopush,this,"test");
        testUtils.errorRecorder = videorecoder;
        client = testUtils.rtmClient;

//        mylog.log("version " + Build.VERSION.SDK_INT);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().hide();

        decodeSurface1 = $(R.id.surface1);
        decodeSurface2 = $(R.id.surface2);
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
        roomMembers= $(R.id.roomsMembers);
        adapterMembers = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,roomMembervalue);
        roomMembers.setAdapter(adapterMembers);
        adapterMembers.notifyDataSetChanged();
        roomMembers.setSelection(0, true);
        roomMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        cameraSwitch = $(R.id.cameraswitch);
        cameraStatus = $(R.id.camerastatus);
        logview = $(R.id.logview);
        logview.setTextSize(14);
        logview.setTextColor(this.getResources().getColor(R.color.white));
        logview.setMovementMethod(ScrollingMovementMethod.getInstance());
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
                decodeSurface1.removeAllViews();
                decodeSurface2.removeAllViews();
                videoRoom.set(0);
                micStatus = false;
                cameraStatus.setBackgroundResource(R.drawable.cameraclose);
                cameraOpen = false;
                mic.setBackgroundResource(R.drawable.micclose);
                micStatus = false;
                textuid2.setText("");
                adapterMembers.clear();
                adapterMembers.notifyDataSetChanged();
//                ClearDraw();
            }
        });
    }

    void login() {
        RTMStruct.RTMAnswer  answer =  testUtils.login();
        if (answer.errorCode == 0) {
            boolean ret = channellNum.isChecked();
            addLog("RTM登陆成功");
            RTMStruct.RTMAnswer jj = client.initVideo(ret,previewSurfaceView.getHolder().getSurface(),0);
            if (jj.errorCode != 0 ){
                addLog("初始化 视频失败 " + jj.getErrInfo());
                return;
            }
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

                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] permissions = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
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
                    kk.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1f));

//                    inal LinearLayout.LayoutParams lpWW = new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);


//                    LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(viewWidth,viewHeght);
//                    kk.setLayoutParams(new LinearLayout.LayoutParams(viewWidth,viewHeght));
//                    ViewGroup.LayoutParams ll = new LinearLayout.LayoutParams();
//                    ll.height = 320;
//                    ll.width =  240;
//                    kk.setLayoutParams(ll);
                    SurfaceInfo tt = new SurfaceInfo();
                    tt.surfaceView = kk;
                    if (decodeSurface1.getChildCount() <3){
                        decodeSurface1.addView(kk);
                        tt.decodeSurface = decodeSurface1;
                        tt.index = decodeSurface1.indexOfChild(kk);
//                        decodeSurface2.addView(kk);
                    }
                    else if(decodeSurface1.getChildCount()>=3  && decodeSurface2.getChildCount()<3){
                        decodeSurface2.addView(kk);
                        tt.decodeSurface = decodeSurface2;
                        tt.index = decodeSurface2.indexOfChild(kk);
                    }
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
                            client.subscribeVideo(new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {
                                    if (answer.errorCode ==  0) {
                                        userSurfaces.put(selectMember, tt);
                                    }
                                    addLog("订阅 "+ selectMember +  " 视频流 " + transRet(answer));
                                }
                            },videoRoom.get(), new HashMap<Long, Surface>() {{
                                put(selectMember, kk.getHolder().getSurface());
                            }});
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
                        client.subscribeVideo(new UserInterface.IRTMEmptyCallback() {
                            @Override
                            public void onResult(RTMStruct.RTMAnswer answer) {
                                addLog("订阅 "+ selectMember +  " 视频流 " + transRet(answer));
                            }
                        },videoRoom.get(), new HashMap<Long, Surface>() {{
                            put(selectMember, userSurfaces.get(selectMember).surfaceView.getHolder().getSurface());
                        }});
                    }
                    break;
                case R.id.unsubscribe:
                    if (roomMembers.getSelectedItem() == null)
                        return;
                    long selectMember1 = Long.parseLong(roomMembers.getSelectedItem().toString());
                    client.unsubscribeVideo(videoRoom.get(), new HashSet<Long>(){{add(selectMember1);}});
                    addLog("取消订阅 "+ selectMember1 +  " 视频流 ");
                    SurfaceInfo  lala =  userSurfaces.get(selectMember1);
                    if (lala != null) {
                        try {
                            lala.decodeSurface.removeView(lala.surfaceView);
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
                                videoRoom.set(roomId);
                                myactivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setMicStatus(true);
                                        textuid2.setText("房间id-" + roomId);
                                        adapterMembers.clear();
                                        adapterMembers.addAll(roomInfo.uids);
                                        adapterMembers.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    },roomId, 2);
                }
            }
        }, roomId);
    }
}
