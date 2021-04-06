package com.example.rtvdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class startActivity extends AppCompatActivity {

    class TestButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Class kk = null;
            switch (v.getId()) {
                case R.id.RTMIM:
                    kk = RTM.class;
                    break;
                case R.id.RTMVoice:
                    kk = RTV.class;
                    break;
            }
            Intent intent = new Intent(startActivity.this,kk);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        TestButtonListener testbutton = new TestButtonListener();

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Button im = findViewById(R.id.RTMIM);
        Button voice = findViewById(R.id.RTMVoice);
        im.setOnClickListener(testbutton);
        voice.setOnClickListener(testbutton);
    }
}