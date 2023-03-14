package com.highras.videoudp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.fpnn.sdk.ErrorRecorder;
import com.highras.videoudp.model.VoiceMember;
import com.rtcsdk.RTMCenter;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMErrorCode;
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

public enum Utils {
    INSTANCE;
    public long currentUserid;
    public long currentRoomid;
    public String  currentLan;
    public String  nickName;

    static class CItem1 {
        public String ID = "";
        public String Value = "";

        public boolean equals(Object obj) {
            if (obj instanceof  CItem1) {
                if (this.ID.equals(((CItem1) obj).ID) && this.Value.equals(((CItem1) obj).Value)) {
                    return true;
                }
                else {
                    return false;
                }
            }
            return false;
        }

        public CItem1(String _ID, String _Value) {
            ID = _ID;
            Value = _Value;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return ID;
        }

        public String getID() {
            return ID;
        }

        public String getValue() {
            return Value;
        }
    }

    public String address = "";
    public final int rtcPort = 13702;
    public final int rtmPort = 13321;

    public String rtmEndpoint;
    public String rtcEndpoint;


    final HashMap<String, ProjectInfo> testAddress = new HashMap(){{
//        put("test", new ProjectInfo(11000002,"rtm-intl-frontgate-test.ilivedata.com"));
        put("test", new ProjectInfo(11000001,"161.189.171.91","cXdlcnR5"));
        put("nx",new ProjectInfo(80000071,"rtm-nx-front.ilivedata.com","cXdlcnR5"));
//        put("intl",new ProjectInfo(80000087,"rtm-intl-frontgate.ilivedata.com","OTRjMDRhYTMtOWExMi00MmFhLTg2NGQtMWU4OTI4YTg2ZGVk"));
//        put("intl",new ProjectInfo(80000087,"18.138.19.251","OTRjMDRhYTMtOWExMi00MmFhLTg2NGQtMWU4OTI4YTg2ZGVk"));
        put("intl",new ProjectInfo(80000087,"18.136.225.133","OTRjMDRhYTMtOWExMi00MmFhLTg2NGQtMWU4OTI4YTg2ZGVk"));
//        put("intl",new ProjectInfo(80000087,"35.167.66.29","OTRjMDRhYTMtOWExMi00MmFhLTg2NGQtMWU4OTI4YTg2ZGVk"));
    }
    };

    public static void alertDialog(final Activity activity, final String str){
        alertDialog(activity, str, false);
    }

    public static void alertDialog(final Activity activity, final String str, boolean finish){
        //        Looper.prepare();
        //        new AlertDialog.Builder(activity).setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialog, int which) {
        //            }
        //        }).show();
        //        Looper.loop();
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                                                if (finish)
                                                    activity.finish();
                    }
                });
                if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
                    return;
                }
                builder.show();
            }
        });
    }


    public ErrorRecorder errorRecorder = new TestErrorRecorder();
    public RTMClient client;

    public void login(Activity activity, UserInterface.IRTMEmptyCallback callback) {
        ProjectInfo info = testAddress.get(address);
        rtmEndpoint = info.host +  ":" + rtmPort;
        rtcEndpoint = info.host +  ":" + rtcPort;

        if (client == null)
            client = RTMCenter.initRTMClient(rtmEndpoint, rtcEndpoint, info.pid, currentUserid, new RTMPushProcessor(), activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long ts = System.currentTimeMillis()/1000;
                String realToken = ApiSecurityExample.genHMACToken(info.pid, currentUserid, ts, info.key);
                client.login(realToken, "zh", null, ts, new UserInterface.IRTMEmptyCallback() {
                    @Override
                    public void onResult(RTMStruct.RTMAnswer answer) {
                        if (answer.errorCode == 0){
                            client.setUserInfo(nickName, "", new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {

                                }
                            });
                        }
                        callback.onResult(answer);
                    }
                });
            }
        }).start();
    }

    String getToken(long uid) {
        int port = 0;
        String token = "";
        if (address.equals("test"))
            port = 8099;
        else if (address.equals("nx"))
            port = 8090;
        else if (address.equals("intl"))
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
            }
            else {
                errorRecorder.recordError("http return error " + code);
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorRecorder.recordError("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }

    public static Boolean isEmpty(String data) {
        if (data == null || data.length() == 0)
            return true;
        else return false;
    }

    //泛型接口 带有一个返回值的回调函数 (请优先判断answer的错误码 泛型值有可能为null)
    public  interface MyCallback<T> {
        void onResult(T t);
    }


    public void realEnterRoom(final long roomId, RTMStruct.RTCRoomType roomtype, Activity activity, MyCallback<RTMStruct.RoomInfo> callback) {
        client.enterRTCRoom(roomId, currentLan, new UserInterface.IRTMEmptyCallback() {
            @Override
            public void onResult(RTMStruct.RTMAnswer answer) {
                if (answer.errorCode == 0) {
                    client.getRTCRoomMembers(roomId, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                        @Override
                        public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                            callback.onResult(roomInfo);
                        }
                    });

                } else if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value()) {
                    client.createRTCRoom(roomId, roomtype, currentLan, new UserInterface.IRTMEmptyCallback() {
                        @Override
                        public void onResult(RTMStruct.RTMAnswer answer) {
                            if (answer.errorCode == 0) {
                                client.getRTCRoomMembers(roomId, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                    @Override
                                    public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                        callback.onResult(roomInfo);
                                    }
                                });
                            } else if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_EXIST.value()) {
                                client.enterRTCRoom(roomId, currentLan, new UserInterface.IRTMEmptyCallback() {
                                    @Override
                                    public void onResult(RTMStruct.RTMAnswer answer) {
                                        if (answer.errorCode == 0) {
                                            client.getRTCRoomMembers(roomId, new UserInterface.IRTMCallback<RTMStruct.RoomInfo>() {
                                                @Override
                                                public void onResult(RTMStruct.RoomInfo roomInfo, RTMStruct.RTMAnswer answer) {
                                                    callback.onResult(roomInfo);
                                                }
                                            });
                                        }
                                        else{
                                            alertDialog(activity,"进入RTC房间-" + roomId + "失败：" + answer.getErrInfo(), true);
                                        }
                                    }
                                });
                            }
                            else{
                                alertDialog(activity,"创建RTC房间-" + roomId + "失败：" + answer.getErrInfo(), true);
                            }
                        }
                    });
                }
                else{
                    alertDialog(activity,"进入RTC房间-" + roomId + "失败：" + answer.getErrInfo(), true);
                }
            }
        });
    }
}
