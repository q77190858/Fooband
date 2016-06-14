package com.foogeez.activity;

import android.app.Application;
import android.util.Log;

public class FoobandApplication extends Application {

    public static FoobandApplication sInstance;
    
    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        
        Log.d("FoobandApplication","onCreate.");
        
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }

    @Override
    public void onLowMemory() {

        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

}
