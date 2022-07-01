package com.highras.voiceDemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.highras.voiceDemo.common.MyUtils;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements CustomeEdittext.OnSuccessListener, TextWatcher {
    int REQUEST_CODE_CONTACT = 101;
    CustomeEdittext roomno;
    CustomeEdittext useridtext;
    CustomeEdittext usernametext;
    Button startAudio;
    Button startVideo;
    TextView versionshow;


    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }


    String getVersionName() {
        String versionname;// 版本号

        try {

            PackageManager pm = getPackageManager();

            PackageInfo pi = pm.getPackageInfo("com.highras.voiceDemo", 0);

            versionname = pi.versionName;// 获取在AndroidManifest.xml中配置的版本号

        } catch (PackageManager.NameNotFoundException e) {

            versionname = "";

        }
        return "Ver:" + versionname;
    }

    void alertDialog(final String str) {
//        Looper.prepare();
//        new AlertDialog.Builder(activity).setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        }).show();
//        Looper.loop();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder((Activity) StartActivity.this).setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CAMERA};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }

        class ceshi{
            int a= 10;
            int b = 20;

            public ceshi(int a, int b) {
                this.a = a;
                this.b = b;
            }
        }
        ArrayList<ceshi> jj = new ArrayList<ceshi>(){{
            add(new ceshi(1,2));
            add(new ceshi(3,4));
            add(new ceshi(5,6));
        }};

        for (ceshi ooo: jj){
            if (ooo.a == 3){
                ooo.b = 20;
                break;
            }
        }

        for (ceshi ooo: jj){
            mylog.log("a " + ooo.a + " b " + ooo.b);
        }


        versionshow = $(R.id.versionshow);
        versionshow.setText(getVersionName());
        roomno = $(R.id.roomno);
        useridtext = $(R.id.useridtext);
        usernametext = $(R.id.usernametext);
        startAudio = $(R.id.startAudio);

        useridtext.edt_content.addTextChangedListener(this);
        roomno.setOnSuccessListener(this);
        useridtext.setOnSuccessListener(this);
        usernametext.setOnSuccessListener(this);
        startAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(0);
            }
        });
        startVideo = $(R.id.startVideo);
        startVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(1);
            }
        });

    }
    class  testclass{
        int a= 0;
        int b = 3;
    }
    private void openActivity(int type) {
        String roomId = roomno.getContent().trim();
        String userId = useridtext.getContent().trim();
        String username = usernametext.getContent().trim();
        if (Utils.isEmpty(roomId)) {
            Toast.makeText(StartActivity.this, "请输入房间号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.isEmpty(userId)) {
            Toast.makeText(StartActivity.this, "请输入用户id", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.isEmpty(username)) {
            Toast.makeText(StartActivity.this, "请输入用户名称", Toast.LENGTH_SHORT).show();
            return;
        }
        long roomid = 0;
        long userid = 0;
        try {
            roomid = Long.parseLong(roomId);
        } catch (NumberFormatException ex) {
            alertDialog("请输入正确的房间号");
            return;
        }

        try {
            userid = Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            alertDialog("请输入正确的用户id");
            return;
        }
        if (roomid == 0)
            return;

        Intent intent;
        if (type == 0) {
            intent = new Intent(StartActivity.this, TestVoiceActivity.class);
        } else {
            intent = new Intent(StartActivity.this, TestVideoActivity.class);
//            intent = new Intent(StartActivity.this, TestActivity2.class);
        }
        saveUserData(userid, username);
        intent.putExtra("roomid", roomid);
        intent.putExtra("userid", userid);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void saveUserData(long uid, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString(String.valueOf(uid), name);
        editor.commit();
    }

    private String getUserData(String uid) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return sharedPreferences.getString(uid, "");
    }

    @Override
    public void onSuccess(String phone) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("fengzi", "afterTextChanged: " + s.toString());
        String userData = getUserData(s.toString());
        Log.d("fengzi", "afterTextChanged1: " + userData);
        if (!MyUtils.isEmpty(userData)) {
            usernametext.setsBottomText(userData);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}