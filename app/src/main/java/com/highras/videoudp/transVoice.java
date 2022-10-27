package com.highras.videoudp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMErrorCode;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class transVoice extends BaseActivity implements OnProcessorListener {
    public static boolean running = false;
    boolean micStatus = false;
    boolean usespeaker = true;
    LinearLayout leave;
    LinearLayout mic;
    LinearLayout speaker;
    ImageView speakerImageView;
    ImageView muteImageView;
    TextView muteTextView;
    TextView speakerTv;
    long activityRoom = 0;
    Activity myactivity = this;
    TextView roomdshow;
    RTMClient client;
    long userId;
    String language;
    Context mcontext;
    Utils utils;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    RoomAdapter roomAdapter;
    TranslateAdapter translateAdapter;
    RecyclerView translateRecycler;

    @Override
    public boolean reloginWillStart(long uid, int reloginCount) {
        return true;
    }

    @Override
    protected void setToolbar() {
        super.setToolbar();
//        customToolbarAndStatusBarBackgroundColor(true);
    }

    @Override
    public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
        if (successful) {
            if (activityRoom <= 0)
                return;
            client.enterRTCRoom(activityRoom, utils.currentLan,new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                @Override
                public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                    if (answer.errorCode == 0) {
                        startVoice(activityRoom, roomInfo, true);
                    } else {
                        mylog.log("重新进入失败");
                    }
                }
            });
        } else {
            mylog.log("重新进入失败");
            micStatus = false;
        }
    }

    @Override
    public void rtmConnectClose(long uid) {
        Log.d("fengzi", "rtmConnectClose: ");
    }

    @Override
    public void kickout() {

    }

    @Override
    public void pushVoiceTranslate(String text, String slang, long uid) {
        if (mapList.size() == 2)
            mapList.remove(0);
        Map<String, String> map = new HashMap<>();
        map.put("user", String.valueOf(uid));
        map.put("content", text);
        mapList.add(map);
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translateAdapter.notifyDataSetChanged();
                translateRecycler.scrollToPosition(mapList.size() - 1);
            }
        });
    }

    @Override
    public void pushEnterRTCRoom(long roomId, long userId, long time) {
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!userList.contains(String.valueOf(userId))) {
                    userList.add(String.valueOf(userId));
                    roomAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void pushExitRTCRoom(long roomId, long userId, long time) {
        //退出房间
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userList.contains(String.valueOf(userId))) {
                    userList.remove(String.valueOf(userId));
                    roomAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.onProcessorListeners.remove(this);
    }

    private void leaveRoom() {
        if (client == null)
            return;
        client.leaveRTCRoom(activityRoom, new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                client.closeRTM();
                client = null;
                finish();
            }
        });
    }


    Toolbar toolbar;
    List<String> userList = new ArrayList<>();
    List<Map<String, String>> mapList = new ArrayList<>();

    @Override
    protected void afterViews() {
        super.afterViews();
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mcontext = this;
        utils = Utils.INSTANCE;
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        customToolbarAndStatusBarBackgroundColor(true);
        mic = $(R.id.mic);
        muteImageView = $(R.id.muteImageView);
        speaker = $(R.id.speaker);
        speakerImageView = $(R.id.speakerImageView);
        leave = $(R.id.leave);
        muteTextView = $(R.id.muteTextView);
        TextView leaveTv = $(R.id.leaveTv);
        speakerTv = $(R.id.speakerTv);
        roomdshow = $(R.id.roomnum);

        RecyclerView recyclerView = $(R.id.user_list_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        roomAdapter = new RoomAdapter(this, userList);
        recyclerView.setAdapter(roomAdapter);

        translateRecycler = $(R.id.translate_recycler);
        translateRecycler.setLayoutManager(new LinearLayoutManager(this));
        translateAdapter = new TranslateAdapter(this, mapList);
        translateRecycler.setAdapter(translateAdapter);


        try {
            muteTextView.setText(Constants.languageObj.getString("mic_on"));
            leaveTv.setText(Constants.languageObj.getString("leave"));
            speakerTv.setText(Constants.languageObj.getString("speaker_on"));

            TextView currentUserTv = findViewById(R.id.current_user_tv);
            TextView roomUserTv = findViewById(R.id.room_user_tv);
            TextView livedataTv = findViewById(R.id.live_data_tv);
            currentUserTv.setText(Constants.languageObj.getString("currentUser"));
            roomUserTv.setText(Constants.languageObj.getString("roomUser"));
            livedataTv.setText(Constants.languageObj.getString("liveCaptioning"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        activityRoom = utils.currentRoomid;
        userId = utils.currentUserid;
        language = utils.currentLan;

        TextView currentUserIdTv = $(R.id.current_userid_tv);
        currentUserIdTv.setText(String.valueOf(userId));

        Constants.onProcessorListeners.add(this);
        client = utils.client;
        leave.setOnClickListener(view -> leaveRoom());


        speaker.setOnClickListener(view -> {
            if (client == null || !client.isOnline())
                return;
            try {
                setSpeakerStatus();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        mic.setOnClickListener(view -> {
            if (!client.isOnline() || activityRoom == 0)
                return;
            try {
                setMicStatus(!micStatus);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        realEnterRoom(activityRoom);

    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_transvoice;
    }

    void setMicStatus(boolean status) throws JSONException {
        if (!status) {
            client.closeMic();
            muteTextView.setText(Constants.languageObj.getString("mic_off"));
            muteImageView.setSelected(true);
        } else {
            client.openMic();
            muteTextView.setText(Constants.languageObj.getString("mic_on"));
            muteImageView.setSelected(false);
        }
        micStatus = status;
    }

    void setSpeakerStatus() throws JSONException {
        usespeaker = !usespeaker;
        if (!usespeaker) {
            speakerTv.setText(Constants.languageObj.getString("speaker_off"));
            speakerImageView.setSelected(true);
        } else {
            speakerTv.setText(Constants.languageObj.getString("speaker_on"));
            speakerImageView.setSelected(false);
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


    void startVoice(long roomId, RTMStruct.RoomInfo info) {
        startVoice(roomId, info, false);
    }


    void startVoice(long roomId, RTMStruct.RoomInfo roomInfo, boolean relogin) {
        if (relogin) {
            myactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usespeaker = true;
                }
            });
            return;
        }
        client.openMic();
        myactivity.runOnUiThread(() -> {
            micStatus = true;
            usespeaker = true;
            try {
                roomdshow.setText(Constants.languageObj.getString("roomid") + roomId);
                roomInfo.uids.forEach(aLong -> {
                    Log.d("fengzi", "startVoice: uid is " + aLong);
                    if (userId != aLong)
                        userList.add(String.valueOf(aLong));
                });
                roomAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    void realEnterRoom(final long roomId) {
        client.enterRTCRoom(roomId, utils.currentLan, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
            @Override
            public void onResult(RTMStruct.RoomInfo info, RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    startVoice(roomId, info);
                } else if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value())  {
                    client.createTranslateRTCRoom(roomId, utils.currentLan, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            if (answer.errorCode == 0) {
                                startVoice(roomId, roomInfo);
                            } else if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_EXIST.value()) {
                                client.enterRTCRoom(roomId, utils.currentLan, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                    @Override
                                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                        if (answer.errorCode == 0) {
                                            startVoice(roomId, roomInfo);
                                        }
                                    }
                                });
                            }
                            else {
                                Utils.alertDialog(transVoice.this, "进入房间 " + activityRoom + " 失败:" + answer.getErrInfo());
                            }
                        }
                    });
                }
                else {
                    Utils.alertDialog(transVoice.this, "进入房间 " + activityRoom + " 失败:" + answer.getErrInfo());
                }
            }
        });
    }
}