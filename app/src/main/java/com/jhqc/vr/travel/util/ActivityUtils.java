package com.jhqc.vr.travel.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.struct.Entry;

import java.util.List;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class ActivityUtils {

    public static final boolean startActivity(Context cxt, Class<?> clz, Entry<String, String>...args) {
        LogUtils.logError("startActivity..start");
        if (cxt == null) {
            //throw new IllegalAccessException("The params 'cxt' can not is null!");
            return false;
        }
        Intent intent = new Intent(cxt, clz);
        if (args != null) {
            for (Entry<String, String> arg : args) {
                intent.putExtra(arg.getKey(), arg.getValue());
            }
        }
        cxt.startActivity(intent);
        return true;
    }


    public static final boolean startActivityForResult(Activity cxt, Class<Activity> clz, int code, Entry<String, String>...args) {
        if (cxt == null) {
            //throw new IllegalAccessException("The params 'cxt' can not is null!");
            return false;
        }
        Intent intent = new Intent(cxt, clz);
        if (args != null) {
            for (Entry<String, String> arg : args) {
                intent.putExtra(arg.getKey(), arg.getValue());
            }

        }
        cxt.startActivityForResult(intent, code);
        return true;
    }

    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
