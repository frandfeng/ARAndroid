package com.jhqc.vr.travel.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class VRApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppDelegate.init(this);
    }
}