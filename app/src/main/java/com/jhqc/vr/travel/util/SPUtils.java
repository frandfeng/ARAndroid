package com.jhqc.vr.travel.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Solomon on 2017/10/20 0020.
 */

public class SPUtils {

    static final String SPNAME = "LOCATION";

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPNAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPNAME, 0);
        return sharedPreferences.getString(key,"");
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPNAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPNAME, 0);
        boolean result = sharedPreferences.getBoolean(key,false);
        return result;
    }

}
