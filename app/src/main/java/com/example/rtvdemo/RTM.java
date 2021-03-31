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
                    //-----------未授权-----------
                    // 判断用户是否点击了不再提醒。(检测该权限是否还可以申请)
                    // shouldShowRequestPermissionRationale合理的解释应该是：如果应用之前请求过此权限
                    //但用户拒绝了请求且未勾选"Don’t ask again"(不在询问)选项，此方法将返回 true。
                    //注：如果用户在过去拒绝了权限请求，并在权限请求系统对话框中勾选了
                    //"Don’t ask again" 选项，此方法将返回 false。如果设备规范禁止应用具有该权限，此方法会返回 false。
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


    void startTestCase(final long pid, final long uid, final String token, final String endpoint) {
        ceshi = new TestClass(pid,uid,token,endpoint);
        TestErrorRecorder mylogRecoder = new TestErrorRecorder();
        RTMConfig ll = new RTMConfig();
        ll.defaultErrorRecorder = mylogRecoder;

        ceshi.client  = new RTMClient(endpoint, pid, uid, new RTMExampleQuestProcessor(),this,ll);
//        ceshi.client  = new RTMClient(endpoint, pid, uid, new RTMExampleQuestProcessor());
//        ceshi.client.setErrorRecoder(mylogRecoder);

        ceshi.mycontext = getApplicationContext();

        //test for push another rtm-client//
        ceshi.pushUserTokens = new HashMap<Long, String>() {
            {
//                put(101L, "2FB123B31E9188B1250A7DAF2196E64F");
//                put(102L, "C84E6F19C4D4386B273F19CA43A0B8BB");
//                put(103L, "04F1A70E3CF133C80007360F3BD43BB6");
//                put(104L, "E1DA4D1D1339AF26144DDFC02D0E954A");
//                put(105L, "8DED3F4B8871D8EFCEC73522EF4820F1");
//                put(106L, "40914C1100DB7DEF8648F9A6F8BF25AC");
//                put(107L, "1F6B13BC197E187C36240BCD2BBB244C");
//                put(108L, "30C7AD1C92A9642C86BF2F239639A62D");
            }
        };
        for (final long testuid : ceshi.pushUserTokens.keySet()) {
//            RTMClient rtmUser = new RTMClient(endpoint, pid, testuid, new RTMExampleQuestProcessor());
            RTMClient rtmUser = new RTMClient(endpoint, pid, testuid, new RTMExampleQuestProcessor(),this,ll);
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
                startTestCase(pid, uid, getToken(pid,uid), "161.189.171.91:13325");
            }
        }).start();

//        TestClass.mySleep(20);
//        ceshi.client.closeRTM();
//        ceshi.client = null;
    }
}