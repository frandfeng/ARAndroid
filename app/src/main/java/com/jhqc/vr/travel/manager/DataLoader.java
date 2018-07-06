package com.jhqc.vr.travel.manager;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class DataLoader {

    static Gson gson;

    public static <T> T loadDataByJson(String json, Class<T> clz) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        T obj = getGson().fromJson(json, clz);
        return obj;
    }

    public static <T> T loadDataByJson(String json, Type type) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        T obj = getGson().fromJson(json, type);
        return obj;
    }

    public static <T> T loadDataByJson(Reader reader, Class<T> clz) {
        T obj = getGson().fromJson(reader, clz);
        return obj;
    }

    public static String classToJson(Object object) {
        String obj = getGson().toJson(object);
        return obj;
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
