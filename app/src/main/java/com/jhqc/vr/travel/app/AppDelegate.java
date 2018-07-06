package com.jhqc.vr.travel.app;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class AppDelegate {

    static VRApplication application;

    public static void init(VRApplication app) {
        application = app;
        CrashHandler.getInstance().init(app.getBaseContext());
    }

    public static VRApplication getApplication() {
        return application;
    }
}
