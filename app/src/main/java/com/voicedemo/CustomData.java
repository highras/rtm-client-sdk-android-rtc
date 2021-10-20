package com.voicedemo;

import com.rtcvoice.RTMClient;

import java.util.Random;

public class CustomData {
    private static CustomData instance = null;
//    private final String rtcEndpoint = "161.189.171.91:40000";
    private final long pid = 11000002;
    public static  long voiceRoom = 1233210;
    public RTMClient clent;
    int REQUEST_CODE_CONTACT = 101;
    Random rand = new Random();
    public  long uid;

    public static CustomData getInstance() {
        if (instance == null) {
            synchronized (CustomData.class) {
                if (instance == null) {
                    instance = new CustomData();
                }
            }
        }
        return instance;
    }

    private CustomData(){
        uid = 100000 + rand.nextInt(100000);
    }

//    public void loginRtm(RTMPushProcessor processor, Activity activity){
//        if (clent == null) {
//            if (Build.VERSION.SDK_INT >= 23) {
//                String[] permissions = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.CAMERA};
//                //验证是否许可权限
//                for (String str : permissions) {
//                    if (activity.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
//                        //申请权限
//                        activity.requestPermissions(permissions, REQUEST_CODE_CONTACT);
//                    }
//                }
//            }
//
//            clent = new RTMClient(rtmEndpoint, rtcEndpoint, pid, uid, processor, activity);
//            clent.setErrorRecoder(new TestErrorRecorder());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    RTMStruct.RTMAnswer answer = clent.login(getToken());
//                    if (answer.errorCode != 0)
//                        mylog.log("rtm login failed " + answer.getErrInfo());
//                }
//            }).start();
//        }
//    }

}