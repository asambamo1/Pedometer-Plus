package com.pedometer.asamapps.UtilsAndOthers;

import android.app.Application;
import android.content.Context;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

public final class MyApplication extends Application {
    public static Context instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Iconify.with(new FontAwesomeModule());
        instance = getApplicationContext();
    }

    public static Context getAppContext() {
        return instance;
    }
}

