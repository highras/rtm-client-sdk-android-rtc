package com.highras.voiceDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtcsdk.RTMCenter;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.UserInterface;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

public enum Utils {
    INSTANCE;

    String address;
    public final int rtcPort = 13702;
    public final int rtmPort = 13321;

    public String rtmEndpoint;
    public String rtcEndpoint;

    final HashMap<String, ProjectInfo> testAddress = new HashMap(){{
        put("test", new ProjectInfo(11000006,"rtm-intl-frontgate-test.ilivedata.com"));
        put("nx",new ProjectInfo(80000071,"rtm-nx-front.ilivedata.com"));
        put("intl",new ProjectInfo(80000087,"rtm-intl-frontgate.ilivedata.com"));
    }
    };


    public void alertDialog(final Activity activity, final String str){
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
                        //                        if (activity.equals(M))
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
    public RTMPushProcessor serverPush;
    public RTMClient client;

    public void login(String curraddress, long userid, UserInterface.IRTMEmptyCallback callback, Activity activity, RTMPushProcessor _processor) {
        address = curraddress;
        ProjectInfo info = testAddress.get(address);
        rtmEndpoint = info.host +  ":" + rtmPort;
        rtcEndpoint = info.host +  ":" + rtcPort;

        serverPush = _processor;
        client = RTMCenter.initRTMClient(rtmEndpoint, rtcEndpoint, info.pid, userid, serverPush, activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.login(callback, getToken(userid));
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
}
