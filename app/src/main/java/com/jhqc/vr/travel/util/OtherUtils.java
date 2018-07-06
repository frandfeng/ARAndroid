package com.jhqc.vr.travel.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class OtherUtils {

    static final String COMMA = ",";

    public static float[] splitLocation(String loc) {
        if (TextUtils.isEmpty(loc)) {
            return null;
        }

        float[] locs = new float[2];
        String[] vs = loc.split(COMMA);

        if (vs != null && vs.length > 1) {
            locs[0] = Float.parseFloat(vs[0]);
            locs[1] = Float.parseFloat(vs[1]);
        }

        return locs;
    }

    /**
     * s转换为00:00:00时间显示
     *
     * @return 00:00:00
     */
    public static String changeToTimeStr(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = seconds / 60;
        long second = seconds % 60;
        long hours = seconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, second);
        } else {
            return String.format("%02d:%02d", minutes, second);
        }
    }

    public static void call(Context context, String phoneNum) {
        if (context != null && !TextUtils.isEmpty(phoneNum)) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(intent);
        }
    }


    public static StringBuffer packUnityJsonData(String body) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format("{\"params\":%s}", body));
        return stringBuffer;
    }

}
