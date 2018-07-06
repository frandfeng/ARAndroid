package com.jhqc.vr.travel.manager;

import android.content.Context;
import android.content.Intent;

import com.jhqc.vr.travel.BaseActivity;
import com.jhqc.vr.travel.LockScreenActivity;
import com.jhqc.vr.travel.MusicPlayerService;
import com.jhqc.vr.travel.util.LogUtils;

import java.util.ArrayList;

/**
 * Created by Solomon on 2017/10/29 0029.
 */

public class VrActivityManager {

    static VrActivityManager INSTANCE;

    ArrayList<BaseActivity> activities = new ArrayList<>();

    private VrActivityManager() {}

    public static VrActivityManager get() {
        if (INSTANCE == null) {
            INSTANCE = new VrActivityManager();
        }
        return INSTANCE;
    }

    public void add(BaseActivity activity) {
        activities.add(activity);
    }

    public BaseActivity getCurrentActivity() {
        if (activities.size() == 0) {
            return null;
        }
        return activities.get(0);
    }

    public void remove(BaseActivity activity) {
        activities.remove(activity);
        LogUtils.logUnity("移除对象： " + activity + " 剩余activity个数：" +activities.size());
    }

    public static void startLockScreenActivity(Context context) {
        Intent lockscreen = new Intent(context, LockScreenActivity.class);
//        lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(lockscreen);
    }
}
