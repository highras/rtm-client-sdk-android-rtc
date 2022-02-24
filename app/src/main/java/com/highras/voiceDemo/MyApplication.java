package com.highras.voiceDemo;

import android.app.Application;

/**
 * @author fengzi
 * @date 2022/2/23 11:21
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
