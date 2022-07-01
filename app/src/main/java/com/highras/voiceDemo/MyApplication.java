package com.highras.voiceDemo;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

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
        CrashReport.initCrashReport(getApplicationContext(), "de41024654", true);
    }
}
