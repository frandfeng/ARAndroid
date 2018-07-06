package com.jhqc.vr.travel.util;

import android.util.Log;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class LogUtils {

    static String TAG_E = "TRAVELE_VIEW";
    static String TAG_UI = "TRAVEL_UI";
    static String TAG_LOC = "TRAVEL_LOC";
    static String TAG_MEDIA = "TRAVEL_MEDIA";
    static String TAG_OTHER = "TRAVEL_OTHER";
    static String TAG_UNITY = "TRAVEL_UNITY";
    static String TAG_EXCEPTION = "TRAVEL_EXCEPTION";
    static String TAG_BLUETOOTH = "TRAVEL_BLUETOOTH";
    static String TAG_VEDIO = "TRAVEL_VEDIO";

    static boolean isDebug = false;

    public static void logView(String msg) {
        if (isDebug) {
            Log.i(TAG_E, msg);
        }
    }

    public static void logArith(String msg) {
        if (isDebug) {
            Log.i(TAG_UI, msg);
        }
    }

    public static void logLoc(String msg) {
        if (isDebug) {
            Log.i(TAG_BLUETOOTH, msg);
        }
    }

    public static void logUnity(String msg) {
        if (isDebug) {
            Log.i(TAG_UNITY, msg);
        }
    }

    public static void logMedia(String msg) {
        if (isDebug) {
            Log.i(TAG_MEDIA, msg);
        }
    }

    public static void logOther(String msg) {
        if (isDebug) {
            Log.i(TAG_OTHER, msg);
        }
    }

    public static void logBle(String msg) {
        if (isDebug) {
            Log.i(TAG_BLUETOOTH, msg);
        }
    }

    public static void logError(String msg) {
        if (isDebug) {
            Log.e(TAG_EXCEPTION, msg);
        }
    }

    public static void logVedio(String msg) {
        if (isDebug) {
            Log.e(TAG_VEDIO, msg);
        }
    }

}
