package com.highras.videoudp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Random;
import com.highras.videoudp.Utils.CItem1;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

import lib.demo.spinner.MaterialSpinner;


public class LoginActivity extends BaseActivity {
    NiceSpinner checkbutton;
    int REQUEST_CODE_CONTACT = 101;
    private static final String TAG = "fengzi";
    MaterialSpinner niceSpinner;
    Utils utils;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    final LinkedList<CItem1> testtypevalue = new LinkedList<CItem1>(){{
        add(new CItem1("内网测试","test"));
        add(new CItem1("宁夏测试","nx"));
        add(new CItem1("国际测试","intl"));
    }
    };

    MyRTMPushProcessor myRTMPushProcessor = new MyRTMPushProcessor();

    EditText userID_edit;
    EditText roomID_edit;
    EditText nickname_edit;

    @Override
    protected int contentLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void setToolbar() {
        super.setToolbar();
        customToolbarAndStatusBarBackgroundColor(false);
    }

    boolean checkNess(){
        Object sroomid = roomID_edit.getText();
        if (sroomid == null) {
            Utils.alertDialog(this, "请输入房间号");
            return false;
        }

        Object suserid = userID_edit.getText();
        if (suserid == null) {
            Utils.alertDialog(this, "请输入用户id");
            return false;
        }
        if (MyUtils.isEmpty( sroomid.toString())) {
            try {
                Utils.alertDialog(this, Constants.languageObj.getString("roomidHint"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        if (MyUtils.isEmpty(userID_edit.getText().toString())) {
            try {
                Utils.alertDialog(this, Constants.languageObj.getString("useridHint"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        long roomid = 0;

        try {
            roomid = Long.parseLong(String.valueOf(sroomid));
        } catch (NumberFormatException ex) {
            Utils.alertDialog(this,"请输入正确的房间号");
            return false;
        }
        utils.currentRoomid = roomid;

        long userid = 0;
        try {
            userid = Long.parseLong(String.valueOf(suserid));
        } catch (NumberFormatException ex) {
            Utils.alertDialog(this,"请输入正确的用户id");
            return false;
        }
        if (roomid == 0 || userid ==0) {
            Utils.alertDialog(this, "请输入正确的用户id或者房间号");
            return false;
        }
        utils.currentRoomid = roomid;
        utils.currentUserid = userid;
        utils.currentLan = Constants.LANGUAGE_VALUE.get(niceSpinner.getSelectedIndex());
        utils.nickName = nickname_edit.getText().toString();
        CItem1 c1 = (CItem1)(checkbutton.getSelectedItem());
        utils.address = c1.Value;

        return true;
    }

    private void saveUserData(long uid, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString(String.valueOf(uid), name);
        editor.commit();
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CAMERA};
        //验证是否许可权限
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
            }
        }
        utils = Utils.INSTANCE;
        checkbutton = $(R.id.selecttype);
        checkbutton.attachDataSource(testtypevalue);
        checkbutton.setBackgroundResource(R.drawable.shape_nicespinner);

        niceSpinner = $(R.id.spinner);
        userID_edit = $(R.id.userID_edit);
        roomID_edit = $(R.id.roomID_edit);
        nickname_edit = $(R.id.usernick_edit);
        RelativeLayout startTransVoice = $(R.id.startTransVoice);
        RelativeLayout startVoice = $(R.id.startVoice);
        RelativeLayout startVideo = $(R.id.startVideo);
        
        LinkedList<String> data = new LinkedList<>(Constants.LANGUAGE);
        niceSpinner.setItems(data);
        niceSpinner.setBackgroundResource(R.drawable.shape_nicespinner);
        niceSpinner.setOnItemSelectedListener((view, position, id, item) -> setLanguage());
        userID_edit.setText(String.valueOf(getRandom()));
        startTransVoice.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            utils.login(LoginActivity.this, myRTMPushProcessor, new UserInterface.IRTMEmptyCallback() {
                @Override
                public void onResult(RTMStruct.RTMAnswer answer) {
                    if (answer.errorCode == 0){
                        Intent intent = new Intent(LoginActivity.this, transVoice.class);
                        startActivity(intent);
                    }
                    else {
                        Utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());
                    }
                }
            });
            saveUserData(utils.currentUserid, utils.nickName);
        });

        startVoice.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            Intent intent = new Intent(this, TestVoiceActivity.class);
            saveUserData(utils.currentUserid, utils.nickName);
            startActivity(intent);
        });

        startVideo.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            Intent intent = new Intent(this, TestVideoActivity.class);
            saveUserData(utils.currentUserid, utils.nickName);
            startActivity(intent);
        });

        setLanguage();
        checkbutton.setSelectedIndex(1);
    }

    private void setLanguage() {
        String fileName = Constants.LANGUAGE_SHOW_MAP.get(Constants.LANGUAGE_VALUE.get(niceSpinner.getSelectedIndex()));
        String content = MyUtils.getJson(fileName, this);
        try {
            Constants.languageObj = new JSONObject(content);
            refreshUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshUI() {
        try {
            TextView userIdTv = findViewById(R.id.userId_tv);
            TextView roomIdTv = findViewById(R.id.roomId_tv);
            TextView languageTv = findViewById(R.id.languagetv);
            TextView nickTV = findViewById(R.id.nickName_tv);
            TextView startAudioText = findViewById(R.id.startVoiceText);
            userIdTv.setText(Constants.languageObj.getString("userid"));
            roomIdTv.setText(Constants.languageObj.getString("roomid"));
            languageTv.setText(Constants.languageObj.getString("language"));
            startAudioText.setText(Constants.languageObj.getString("startVoice"));
            userID_edit.setHint(Constants.languageObj.getString("useridHint"));
            roomID_edit.setHint(Constants.languageObj.getString("roomidHint"));

            nickname_edit.setHint("");
            nickname_edit.setHint(Constants.languageObj.getString("roomNickname"));
        } catch (Exception e) {
            Log.d(TAG, "refreshUI: " + e.getMessage());
        }
    }


    private void openVoice() {
        Intent intent = new Intent(LoginActivity.this, transVoice.class);
        startActivity(intent);
    }

    private int getRandom() {
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
}