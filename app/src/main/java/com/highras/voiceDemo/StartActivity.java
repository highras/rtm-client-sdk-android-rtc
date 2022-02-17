package com.highras.voiceDemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static android.view.View.GONE;

public class StartActivity extends AppCompatActivity {
    int REQUEST_CODE_CONTACT = 101;
    EditText editText;
    Button startAudio;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
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


        editText = $(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editText.getText().toString().isEmpty())
                    startAudio.setVisibility(View.INVISIBLE);
                else
                    startAudio.setVisibility(View.VISIBLE);
            }
        });

        startAudio = $(R.id.startAudio);
        startAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int roomid = Integer.parseInt(editText.getText().toString());
                if (roomid == 0)
                    return;
                Intent intent = new Intent(StartActivity.this, TestvoiceActivity.class);
                intent.putExtra("roomid",roomid);
                startActivity(intent);
            }
        });

    }
}