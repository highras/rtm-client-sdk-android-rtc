package com.highras.videoudp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Looper;

import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.UserInterface;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MyUtils {

    public static RTMPushProcessor serverPush;

    public  static void getToken(long uid, IHttpCallback callback) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String getTokenUrl = "http://161.189.171.91:8090"  + "?uid=" + uid;
//        String getTokenUrl = "http://161.189.171.91:8099"  + "?uid=" + uid;

        try {
            URL url = new URL(getTokenUrl);
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
                callback.onSuccess(output.toString());
            } else {
                callback.onError(code, "");
            }
        } catch (Exception e) {
            callback.onError(400, e.getMessage());
        }

    }

    public static Boolean isEmpty(String data) {
        if (data == null || data.length() == 0)
            return true;
        else return false;
    }

    /**
     * 判断当前线程是否是主线程
     *
     * @return
     */
    public static Boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
