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

//        this.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
//            private int activityStartCount = 0;
//            @Override
//            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
//
//            }
//
//            @Override
//
//            public void onActivityStarted(Activity activity) {
//
//                activityStartCount++;
//                mylog.log(" onActivityStarted " + activityStartCount);
//
//                //数值从0变到1说明是从后台切到前台
//
//                if (activityStartCount == 1){
//                    mylog.log(" 进入前台 ");
//                }
//
//            }
//
//            @Override
//            public void onActivityResumed(@NonNull Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityPaused(@NonNull Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityStopped(@NonNull Activity activity) {
//                activityStartCount--;
//                mylog.log(" onActivityStopped " + activityStartCount);
//
//                //数值从0变到1说明是从后台切到前台
//
//                if (activityStartCount == 0){
//                    mylog.log(" 进入后台 ");
//                }
//            }
//
//            @Override
//            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
//
//            }
//
//            @Override
//            public void onActivityDestroyed(@NonNull Activity activity) {
//
//            }
//        });

        TestButtonListener testbutton = new TestButtonListener();

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Button im = findViewById(R.id.RTMIM);
        Button voice = findViewById(R.id.RTMVoice);
        im.setOnClickListener(testbutton);
        voice.setOnClickListener(testbutton);
    }
}