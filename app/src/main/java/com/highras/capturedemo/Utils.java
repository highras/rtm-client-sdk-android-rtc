package com.highras.capturedemo;

import android.app.Activity;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtmsdk.RTMClient;
import com.rtmsdk.RTMPushProcessor;
import com.rtmsdk.RTMStruct;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {
    class ProjectInfo{
        long pid;
        String host;
        ProjectInfo(long _pid, String _host){
            pid = _pid;
            host = _host;
        }
    }

    public void dispose (){
        if (rtmClient == null)
            return;
        rtmClient.bye();
        rtmClient.closeRTM();
        rtmClient = null;
    }


    public  Utils (ErrorRecorder _errorRecorder, RTMPushProcessor _serverPush, Activity currentActivity,String address){
        curraddress = address;
        errorRecorder = _errorRecorder;
        serverPush = _serverPush;
        activity = new WeakReference<>(currentActivity);
        RTMStruct.RTMAnswer answer = new RTMStruct.RTMAnswer(ErrorCode.FPNN_EC_OK.value(),"");

        if (rtmClient != null && rtmClient.isOnline())
            return ;

        info = testAddress.get(address);
        String rtmEndpoint = info.host +  ":" + rtmPort;
        String rtcEndpoint = info.host +  ":" + rtcPort;
        uid = getuid();
        rtmClient = new RTMClient(rtmEndpoint, rtcEndpoint,info.pid, uid, serverPush, activity.get());

    }

    final HashMap<String, ProjectInfo> testAddress = new HashMap(){{
            put("test", new ProjectInfo(11000002,"161.189.171.91"));
            put("nx",new ProjectInfo(80000071,"rtm-nx-front.ilivedata.com"));
            put("intl",new ProjectInfo(80000087,"rtm-intl-frontgate.ilivedata.com"));
        }
    };

    ProjectInfo info;
    String curraddress;
    final int rtcPort = 13702;
    final int rtmPort = 13321;
    long uid = 0;
    Random rand = new Random();
    public RTMClient rtmClient;
    public ErrorRecorder errorRecorder;
    public RTMPushProcessor serverPush;
    public WeakReference<Activity> activity;

    int getuid() {
        return rand.nextInt(20000 - 1 + 1) + 1;
    }

    RTMStruct.RTMAnswer login(){
        RTMStruct.RTMAnswer answer = new RTMStruct.RTMAnswer(ErrorCode.FPNN_EC_OK.value(),"");
        String token = "";
        if (curraddress.equals("test"))
            token = getToken();
        else {
            token = httpGettoken();
        }
        return rtmClient.login(token);
    }

    public  String getToken() {
        TCPClient kk = TCPClient.create("161.189.171.91:13777", true);
        Quest ll = new Quest("getUserToken");
        String gettoken = "";
        ll.param("pid", info.pid);
        ll.param("uid", uid);
        try {
            Answer ret = kk.sendQuest(ll, 10);
            if (ret.getErrorCode() == 0) {
                gettoken = (String)ret.want("token");
                if (gettoken.isEmpty()) {
                    errorRecorder.recordError("getUserToken is empty");
                }
            } else
                errorRecorder.recordError("getUserToken failed" + ret.getErrorMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gettoken;
    }

    public  String httpGettoken() {
        int port = 0;
        String token = "";
        if (curraddress.equals("nx"))
            port = 8090;
        else if (curraddress.equals("intl"))
            port = 8091;

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
            errorRecorder.recordError("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }
}
