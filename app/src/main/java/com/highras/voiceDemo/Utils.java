package com.highras.voiceDemo;

import android.app.Activity;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
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
    String rtcEndpoint = "rtm-nx-front.ilivedata.com:13702";
    String rtmEndpoint = "rtm-nx-front.ilivedata.com:13321";
    Random rand = new Random();
    public ErrorRecorder errorRecorder = new TestErrorRecorder();
    public RTMPushProcessor serverPush;
    public RTMClient client;
    public long uid;

    int getuid() {
        return rand.nextInt(20000 - 1 + 1) + 1;
    }

    public void login(UserInterface.IRTMEmptyCallback callback, Activity activity, RTMPushProcessor _processor){
        uid = getuid();
        serverPush = _processor;
        client = new RTMClient(rtmEndpoint, rtcEndpoint, 80000071, uid, serverPush, activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.login(callback, getToken());
            }
        }).start();
    }

    String getToken() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String tourl = "http://161.189.171.91:8090?uid=" + uid;
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
            errorRecorder.recordError("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }

}
