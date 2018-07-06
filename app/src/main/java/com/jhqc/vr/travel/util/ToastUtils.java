package com.jhqc.vr.travel.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Solomon on 2017/10/20 0020.
 */

public class ToastUtils {

    public static void show(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

}
