package com.example.rtvdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtmsdk.RTMAudio;
import com.rtmsdk.RTMClient;
import com.rtmsdk.RTMConfig;
import com.rtmsdk.RTMStruct;
import com.rtmsdk.TranscribeLang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RTM extends Activity {
    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }
    final String rtmEndpoint = "161.189.171.91:13321";
    final String rtcEndpoint = "161.189.171.91:13702";
    Chronometer timer;
    Context mycontext = this;
    int REQUEST_CODE_CONTACT = 101;
    File recordFile;
    //    RTMUtils.audioUtils1 audioManage = RTMUtils.audioUtils1.getInstance();
    RTMAudio audioManage = RTMAudio.getInstance();

    TestClass ceshi;
    final String[] buttonNames = {"chat", "message", "history", "friend", "group", "room", "file", "data", "system", "user"};
    Map<Integer, String> testButtons = new HashMap<Integer, String>();

    public void testAduio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ceshi.startAudioTest();
            }
        }).start();
    }

    public void startTest(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ceshi != null) {
                        if (name == "stress")
                            ;
//                           ceshi.startStress();
                        else
                            ceshi.startCase(name);
                    }
                    else{
                        mylog.log("ceshi is null");
                    }
                } catch (InterruptedException e) {
                    mylog.log("startTest:" + name + " exception:" + e.getMessage());
                }
            }
        }).start();
    }

    public static byte[] toByteArray(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (true) {
            try {
                if (!(-1 != (n = input.read(buffer)))) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    boolean controlBackgroudVoice(Context context, boolean pause) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(pause){
            int result = am.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }else{
            int result = am.abandonAudioFocus(null);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    class AudioButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.record:
                    controlBackgroudVoice(getApplicationContext(),true);
                    timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                    timer.start();
                    audioManage.startRecord();
//                    TestClass.audioStruct = audioManage.stopRecord();
                    break;
                case R.id.stopAudio:
//                    controlBackgroudVoice(getApplicationContext(),false);
                    timer.stop();
                    TestClass.audioStruct = audioManage.stopRecord();
                    try {
                        testAduio();
                    } catch (Exception e) {
                        mylog.log("hehe:" + e.getMessage());
                    }
                    break;
                case R.id.broadAudio:
                    timer.stop();
                    audioManage.stopRecord();
//                    audioManage.broadRecoder(getResources().openRawResource(R.raw.demo));
//                    audioManage.broadRecoder(toByteArray((getResources().openRawResource(R.raw.demo))));
                    audioManage.broadAudio();
                    break;
                case R.id.stopBroad:
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mylog.log(Thread.currentThread().toString());
                    timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                    timer.start();
                    audioManage.startRecord();

//                    audioManage.stopAudio();
                    break;
            }
        }
    }

    class TestButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (testButtons.containsKey(v.getId())) {
                String testName = testButtons.get(v.getId());
                startTest(testName);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 判断该权限是否已经授权
                boolean grantFlas = false;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        //-----------存在未授权-----------
                        grantFlas = true;
                    }
                }

                if (grantFlas) {
                    boolean shouldShowRequestFlas = false;
                    for (String per : permissions) {
                        if (shouldShowRequestPermissionRationale(per)) {
                            //-----------存在未授权-----------
                            shouldShowRequestFlas = true;
                        }
                    }
                } else {
                    //-----------授权成功-----------
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    void startTestCase(final long pid, final long uid, final String token) {
        ceshi = new TestClass(pid,uid,token,rtmEndpoint);
        TestErrorRecorder mylogRecoder = new TestErrorRecorder();
        RTMConfig ll = new RTMConfig();
        ll.defaultErrorRecorder = mylogRecoder;

        ceshi.client  = new RTMClient(rtmEndpoint,rtcEndpoint, pid, uid, new RTMExampleQuestProcessor(),this,ll);
//        ceshi.client  = new RTMClient(endpoint, pid, uid, new RTMExampleQuestProcessor());
//        ceshi.client.setErrorRecoder(mylogRecoder);

        ceshi.mycontext = getApplicationContext();

        //test for push another rtm-client//
        ceshi.pushUserTokens = new HashMap<Long, String>() {
            {
//                put(101L, "2FB123B31E9188B1250A7DAF2196E64F");
            }
        };
        for (final long testuid : ceshi.pushUserTokens.keySet()) {
            RTMClient rtmUser = new RTMClient(rtmEndpoint,rtcEndpoint, pid, testuid, new RTMExampleQuestProcessor(),this,ll);
            ceshi.addClients(testuid, rtmUser);
        }

        String audioPath = "/sdcard/rtmCache";
        File audioFilePath = new File(audioPath);
        if (!audioFilePath.exists()) {
            boolean ret = audioFilePath.mkdir();
            if (!ret) {
                mylog.log("create dir " + audioPath + " error");
                return;
            }
        }
        File rtmaudiocache = new File(audioPath + "/audiocache");
        File audioSave = new  File(audioPath + "/audiosave");
        ceshi.audioSave = audioSave;

        RTMAudio.IAudioAction jj = new RTMAudio.IAudioAction() {
            @Override
            public void startRecord() {

            }

            @Override
            public void stopRecord() {

            }

            @Override
            public void broadAudio() {

            }

            @Override
            public void broadFinish() {

            }

            @Override
            public void listenVolume(double db) {
//                mylog.log("分贝值："+db);
            }
        };

        RTMAudio.getInstance().init(rtmaudiocache, TranscribeLang.EN_US.getName(),jj);
        byte[] audioData = null;
        try {
//            InputStream inputStream=getAssets().open("audiocache");
            InputStream inputStream=getAssets().open("audioDemo.amr");
//            InputStream inputStream=getAssets().open("audiocache-en");
            FileOutputStream outfile = new FileOutputStream(RTMAudio.getInstance().getRecordFile());
            audioData = toByteArray(inputStream);
            outfile.write(audioData);
            outfile.close();
            inputStream.close();
            ceshi.audioFile = RTMAudio.getInstance().getRecordFile();
            ceshi.rtmAudioData = audioData;
//            RTMAudio.getInstance().broadAudio(audioData);

            InputStream inputStream2=getAssets().open("videoDemo.mp4");
            ceshi.videoData = toByteArray(inputStream2);


            InputStream inputStream3=getAssets().open("taishan.wav");
            ceshi.audioData = toByteArray(inputStream3);

            InputStream inputStream4=getAssets().open("nihao.txt");
            ceshi.fileData = toByteArray(inputStream4);


            InputStream inputStream1=getAssets().open("testpic.jpeg");
//            InputStream inputStream1=getAssets().open("ct4.jpeg");
//            InputStream inputStream1=getAssets().open("testpic1.jpeg");
//            InputStream inputStream1=getAssets().open("bizhi.jpeg");
//            InputStream inputStream1=getAssets().open("testapk.apk");
            byte[] picdata = toByteArray(inputStream1);
            ceshi.piccontent = picdata;

        } catch (IOException e) {
            e.printStackTrace();
        }
        ceshi.loginRTM();
    }

    public String getToken(long  pid , long uid) {
        TCPClient kk = TCPClient.create("161.189.171.91:13777", true);
        Quest ll = new Quest("getUserToken");
        String gettoken = "";
        ll.param("pid", pid);
        ll.param("uid", uid);
        try {
            Answer ret = kk.sendQuest(ll, 10);
            if (ret.getErrorCode() == 0) {
                gettoken = ret.wantString("token");
                if (gettoken.isEmpty()) {
                    return gettoken;
                }
            } else
                return gettoken;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gettoken;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ceshi.client.bye();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtm);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        NetStateReceiver stateReceiver = new NetStateReceiver();
//        this.registerReceiver(new netchange(),intentFilter);

        TestButtonListener testButtonListener = new TestButtonListener();
        AudioButtonListener audioButtonListener = new AudioButtonListener();


        for (String name : buttonNames) {
            int buttonId = getResources().getIdentifier(name, "id", "com.example.rtvdemo");
            Button button = $(buttonId);
            button.setOnClickListener(testButtonListener);
            testButtons.put(buttonId, name);
        }

        timer = $(R.id.timer);
        Button startRecoder = $(R.id.record);
        Button broadcast = $(R.id.broadAudio);
        Button stopAudio = $(R.id.stopAudio);
        Button stopBroad = $(R.id.stopBroad);

        startRecoder.setOnClickListener(audioButtonListener);
        stopAudio.setOnClickListener(audioButtonListener);
        broadcast.setOnClickListener(audioButtonListener);
        stopBroad.setOnClickListener(audioButtonListener);

        final long uid = 1000;
        final long pid = 11000002;
        if (ceshi !=null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                startTestCase(pid, uid, getToken(pid,uid));
            }
        }).start();
    }
}