package com.highras.voiceDemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.highras.voiceDemo.adapter.MemberAdapter;
import com.highras.voiceDemo.common.DisplayUtils;
import com.highras.voiceDemo.model.Member;
import com.livedata.rtc.RTCEngine;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author fengzi
 * @date 2022/2/17 19:37
 */

/**
 * @author fengzi
 * @date 2022/2/17 19:37
 */
public class TestVideoActivity extends AppCompatActivity {
    private static final String TAG = "sdktest";

    List<Member> memberList = new ArrayList<>();
    MyHandler myHandler = new MyHandler(this);
    long activityRoom = 0;
    private SurfaceView previewSurfaceView = null;
    public AtomicLong videoRoom = new AtomicLong(0);
    private HashMap<Long, View> userSurfaces = new HashMap<>();
    ArrayList<Long> roomMembers = new ArrayList<>();
    ProjectInfo info = new ProjectInfo(80000071, "rtm-nx-front.ilivedata.com"); //宁夏
    RTMClient client;
    String curraddress = "nx";
    long uid = 0;
    //是否启用双声道
    Boolean channelNum = false;
    //视频质量
    int videoQulity = 1;
    //摄像头是否开启
    public boolean cameraOpen = false;
    boolean micStatus = false;
    boolean voiceStatus = false;
    boolean useFront = true;
    Random random = new Random();
    RTMVideoProcessor serverPush = new RTMVideoProcessor();
    TestErrorRecorder errorRecorder = new TestErrorRecorder();
    LinearLayout linearlayout;
    AudioManager am;

    ImageView mic_image;
    ImageView camera_image;
    TextView mic_text;
    TextView camera_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testvideo);
        activityRoom = getIntent().getIntExtra("roomid", 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        linearlayout = findViewById(R.id.linearlayout);
        mic_image = findViewById(R.id.mic_image);
        mic_text = findViewById(R.id.mic_text);
        camera_image = findViewById(R.id.camera_image);
        camera_text = findViewById(R.id.camera_text);
        TextView current_member = findViewById(R.id.current_member);
        uid = getuid();
        current_member.setText(String.valueOf(uid));
        toolbar.setTitle("房间-" + activityRoom);
        previewSurfaceView = findViewById(R.id.preview_surface);
//        setSurfaceViewCorner(previewSurfaceView, 30);
        previewSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        previewSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
//                mylog.log("width-" + previewSurfaceView.getWidth() + " hight-" + previewSurfaceView.getHeight());
                previewSurfaceView.getLayoutParams();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
        new Thread(() -> login()).start();
    }

    final int rtcPort = 13702;
    final int rtmPort = 13321;

    void login() {
        if (client != null)
            client.closeRTM();
        String rtmEndpoint = info.host + ":" + rtmPort;
        String rtcEndpoint = info.host + ":" + rtcPort;
        Log.d(TAG, "login: 当前登录用户是" + uid);
        client = new RTMClient(rtmEndpoint, rtcEndpoint, info.pid, uid, serverPush, this);
        client.setErrorRecoder(errorRecorder);
        RTMStruct.RTMAnswer answer = client.login(httpGettoken());
        if (answer.errorCode == 0) {
            Log.d(TAG, "login: 登录成功");
            client.switchVideoQuality(videoQulity);
            RTMStruct.RTMAnswer jj = client.initRTC(channelNum);
            if (jj.errorCode != 0) {
                return;
            }
            client.setPreview(previewSurfaceView);
            realEnterRoom(activityRoom);
        } else {
            Log.d(TAG, "login: 登录失败 " + answer.getErrInfo());
        }
    }

    void realEnterRoom(final long roomId) {
        client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    videoRoom.set(roomId);
                    voiceStatus = true;
                    Log.d(TAG, "onResult: 进入房间" + roomId + " " + transRet(answer));
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = info;
                    myHandler.sendMessage(message);
                } else {
                    client.createRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            Log.d(TAG, "onResult: 创建房间  " + roomId + " " + transRet(answer));
                            if (answer.errorCode == 0) {
                                videoRoom.set(roomId);
                                Message message = Message.obtain();
                                message.what = 1;
                                message.obj = info;
                                myHandler.sendMessage(message);
                            }
                        }
                    }, roomId, 2, 0);
                }
            }
        }, roomId);
    }


    public String httpGettoken() {
        int port = 0;
        if (curraddress.equals("test"))
            port = 8099;
        else if (curraddress.equals("nx"))
            port = 8090;
        else if (curraddress.equals("internationnal"))
            port = 8098;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String tourl = "http://161.189.171.91:" + port + "?uid=" + uid;

        try {
            URL url = new URL(tourl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true); // 同意输入流，即同意下载
            httpURLConnection.setUseCaches(false); // 不使用缓冲
            httpURLConnection.setRequestMethod("GET"); // 使用get请求
            httpURLConnection.setConnectTimeout(20 * 1000);
            httpURLConnection.setReadTimeout(20 * 1000);
            httpURLConnection.connect();

            int code = httpURLConnection.getResponseCode();

            if (code == 200) { // 正常响应
                InputStream inputStream = httpURLConnection.getInputStream();

                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = inputStream.read(buffer))) {
                    output.write(buffer, 0, n);
                }

                inputStream.close();
            } else {
                errorRecorder.recordError("http return error " + code);
                return "";
            }
        } catch (Exception e) {
            errorRecorder.recordError("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }

    public void onclick(View view) {
        if (view.getId() == R.id.mic_relayout) {
            setMicStatus();
        } else if (view.getId() == R.id.camera_relayout) {
            setCameraStatus();
        } else if (view.getId() == R.id.user_relayout) {
            showUsersList();
        } else if (view.getId() == R.id.camera_switch_image) {
            cameraSwitch();
        }
    }

    class RTMVideoProcessor extends RTMPushProcessor {
        String msg = "";

        public boolean reloginWillStart(long uid, int reloginCount) {
            if (reloginCount >= 10) {
                return false;
            }
            Log.d(TAG, "reloginWillStart: 用户 " + uid + " 开始重连第 " + reloginCount + "次");
            return true;
        }


        public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
            Log.d(TAG, "reloginCompleted: 用户 " + uid + " 重连结束 共 " + reloginCount + "次，结果 " + transRet(answer));
            if (successful) {
                final long id = videoRoom.get();
                if (id <= 0)
                    return;
                client.enterRTCRoom(new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                    @Override
                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode != 0) {
                            Log.d(TAG, "reloginCompleted: 用户 " + uid + "重新进入房间 " + id + answer.getErrInfo());
                        } else {
                            Log.d(TAG, "reloginCompleted: 用户 " + uid + "重新进入房间 " + id + " 成功");
                            if (cameraOpen)
                                client.openCamera();
                            if (!userSurfaces.isEmpty()) {
                                HashMap<Long, SurfaceView> map = new HashMap<>();
                                userSurfaces.forEach((aLong, view) -> {
                                    map.put(aLong, (SurfaceView) view.findViewById(R.id.member_surface));
                                });
                                RTMStruct.RTMAnswer subanswer = client.subscribeVideos(videoRoom.get(), map);
                                Log.d(TAG, "onResult: " + "订阅 " + userSurfaces.keySet().toString() + " 视频流 " + transRet(subanswer));
                            }
                        }
                    }
                }, id);
            } else {
                videoRoom.set(0);
            }
        }

        public void rtmConnectClose(long uid) {

        }

        public void kickout() {

        }


        @Override
        public void pushPullRoom(long roomId, RTMStruct.RoomInfo info) {
            Log.d(TAG, "pushPullRoom: " + uid + "被拉入房间 " + roomId + info.getErrInfo());
        }

        @Override
        public void pushEnterRTCRoom(final long roomId, final long userId, long time) {
            memberList.add(new Member(userId, true));
            myHandler.sendEmptyMessage(4);
            Message message = Message.obtain();
            message.what = 2;
            message.obj = userId;
            myHandler.sendMessage(message);
            Log.d(TAG, "pushPullRoom: " + userId + "进入房间 " + roomId);
        }

        @Override
        public void pushAdminCommand(int command, HashSet<Long> uids) {
            Log.d(TAG, "pushAdminCommand: " + command + " uids " + uids.toString());
        }

        @Override
        public void pushExitRTCRoom(final long roomId, final long userId, long time) {
            Iterator<Member> iterator = memberList.iterator();
            while (iterator.hasNext()) {
                Member member = iterator.next();
                if (member.uid == userId) iterator.remove();
            }
            myHandler.sendEmptyMessage(4);
            Message message = Message.obtain();
            message.what = 3;
            message.obj = userId;
            myHandler.sendMessage(message);
            Log.d(TAG, "pushExitRTCRoom: " + userId + "退出房间 " + roomId);
        }

        @Override
        public void pushRTCRoomClosed(long roomId) {
            Log.d(TAG, "pushRTCRoomClosed: 房间" + roomId + "已关闭");
        }

        @Override
        public void pushKickoutRTCRoom(final long roomId) {
            realLeaveRoom();
            Log.d(TAG, "pushKickoutRTCRoom: 被踢出语音房间" + roomId);
        }
    }

    void setCameraStatus(boolean status) {
        cameraOpen = status;
        if (!cameraOpen) {
            camera_image.setImageResource(R.mipmap.camera_close);
            camera_text.setText("开摄像头");
            client.closeCamera();
            Log.d(TAG, "setCameraStatus: 关闭摄像头");
        } else {
            camera_image.setImageResource(R.mipmap.camera_open);
            camera_text.setText("关摄像头");
            client.openCamera();
            Log.d(TAG, "setCameraStatus: 打开摄像头");
        }
    }

    void setMicStatus(boolean status) {
        if (!status) {
            mic_image.setImageResource(R.mipmap.mic_close);
            mic_text.setText("解除静音");
            client.closeMic();
            Log.d(TAG, "setMicStatus:关闭麦克风");
        } else {
            mic_image.setImageResource(R.mipmap.mic_open);
            mic_text.setText("静音");
            client.openMic();
            Log.d(TAG, "setMicStatus: 打开麦克风");
        }
        micStatus = status;
    }

    void realLeaveRoom() {
        client.leaveRTCRoom(videoRoom.get(), new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    Log.d(TAG, "onResult: 离开房间");
                }
            }
        });
    }

    int getuid() {
        return random.nextInt(20000 - 1 + 1) + 1;
    }

    String transRet(RTMStruct.RTMAnswer answer) {
        return (answer.errorCode == 0 ? "成功" : "失败-" + answer.getErrInfo());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.leaveRTCRoom(videoRoom.get(), new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0)
                    realLeaveRoom();
            }
        });
    }

    private void initMember(RTMStruct.RoomInfo info) {
        setCameraStatus();
        setMicStatus();
        HashSet<Long> uids = info.uids;
        Log.d(TAG, "initMember: " + info + " uids:" + uids);
        if (uids != null) {
            uids.stream().filter(item -> item != uid).forEach(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    memberList.add(new Member(aLong, true));
                    addMember(aLong);
                }
            });
        }
    }

    void setVoiceStatus(boolean status) {
        if (status == voiceStatus)
            return;

        RTMStruct.RTMAnswer ret = client.setVoiceStat(status);
        if (ret.errorCode != 0) {
            Log.d(TAG, "setVoiceStatus: " + status + " error " + ret.getErrInfo());
            return;
        }
        if (!status) {
            micStatus = false;
            client.closeMic();
        } else {
            Log.d(TAG, "setVoiceStatus:打开语音");
        }
        voiceStatus = status;
    }

    private void addMember(Long uid) {
        View view = getLayoutInflater().inflate(R.layout.member_item, null);
        TextView textView = view.findViewById(R.id.member_name);
        SurfaceView surfaceView = view.findViewById(R.id.member_surface);
        setSurfaceViewCorner(surfaceView, 30, uid);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 5, 5, 5);
        view.setLayoutParams(lp);
        linearlayout.addView(view);
        textView.setText(String.valueOf(uid));
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                RTMStruct.RTMAnswer jj = client.subscribeVideos(videoRoom.get(), new HashMap<Long, SurfaceView>() {{
                    put(uid, surfaceView);
                }});
                if (jj.errorCode == 0) {
                    userSurfaces.put(uid, view);
                }
                Log.d(TAG, "surfaceCreated: 订阅" + uid + " 视频流 " + transRet(jj));
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
        userSurfaces.put(uid, view);
    }

    private void removeMember(long uid) {
        if (userSurfaces.containsKey(uid)) {
            linearlayout.removeView(userSurfaces.get(uid));
            userSurfaces.remove(uid);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> weakReference;

        public MyHandler(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            TestVideoActivity activity = (TestVideoActivity) weakReference.get();
            if (msg.what == 1) {
                activity.initMember((RTMStruct.RoomInfo) msg.obj);
            }
            if (msg.what == 2) {
                activity.addMember((Long) msg.obj);
            }
            if (msg.what == 3) {
                activity.removeMember((Long) msg.obj);
            }
            if (msg.what == 4) {
                activity.refreshMemberList();
            }
        }
    }

    int itemWidth = 0;

    private void setSurfaceViewCorner(SurfaceView surfaceView, final float radius, long uid) {
        surfaceView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                if (itemWidth == 0) {
                    itemWidth = rect.right - rect.left;
                }
                int leftMargin = 0;
                int topMargin = 0;
                Rect selfRect = new Rect(leftMargin, topMargin,
                        itemWidth - leftMargin,
                        rect.bottom - rect.top - topMargin);
                outline.setRoundRect(selfRect, radius);
                Log.d("fengzi", "fengzi: uid:" + uid);
                Log.d("fengzi", "getOutline: rect left:" + rect.left + " top:" + rect.top + " right:" + rect.right + " bottom:" + rect.bottom);
                Log.d("fengzi", "selfRect: selfRect left:" + selfRect.left + " top:" + selfRect.top + " right:" + selfRect.right + " bottom:" + selfRect.bottom);
            }
        });
        surfaceView.setClipToOutline(true);
    }

    private void setMicStatus() {
        setMicStatus(!micStatus);
    }

    private void setCameraStatus() {
        setCameraStatus(!cameraOpen);
    }

    private void cameraSwitch() {
        if (!cameraOpen)
            return;
        useFront = !useFront;
        RTCEngine.switchCamera(useFront);
    }

    MemberAdapter memberAdapter;

    private void showUsersList() {
        Dialog dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.memberlist_layout, null);
        //初始化控件
        RecyclerView recyclerView = inflate.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(this, memberList);
        recyclerView.setAdapter(memberAdapter);
        memberAdapter.setOnClickListener((position, uid, isOff) -> {
            Member member = null;
            for (int i = 0; i < memberList.size(); i++) {
                if (memberList.get(i).uid == uid) {
                    memberList.get(i).subscribe = isOff;
                    member = memberList.get(i);
                    break;
                }
            }
            if (member != null) {
                switchItem(member);
                memberAdapter.notifyDataSetChanged();
            }
        });
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = DisplayUtils.getScreenHeight(this) / 2;
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    private void refreshMemberList() {
        Optional.ofNullable(memberAdapter).ifPresent(memberAdapter -> {
            memberAdapter.notifyDataSetChanged();
        });
    }

    /**
     * 取消或者订阅
     */
    private void switchItem(Member member) {
        if (member.subscribe) {
            addMember(member.uid);
        } else {
            HashSet<Long> hashSet = new HashSet<>();
            hashSet.add(member.uid);
            client.unsubscribeVideo(activityRoom, hashSet);
            removeMember(member.uid);
        }
    }
}