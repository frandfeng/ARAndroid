package com.jhqc.vr.travel.util;

import android.content.res.AssetFileDescriptor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class FileUtils {

    public static String read2String(InputStream inputStream) {
        try {
            int size=inputStream.available();

            byte[] buffer = new byte[size];

            inputStream.read(buffer);

            return new String(buffer,"UTF-8");
        } catch (Exception e) {
            LogUtils.logView(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    public static void closeAssetFileDescriptor(AssetFileDescriptor descriptor) {
        if (descriptor != null) {
            try {
//                descriptor.createInputStream();
                descriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final class FileExpand {

        public static final String TXT = ".txt";

        public static final String MP3 = ".mp3";

        public static final String PNG = ".png";

        public static final String JPG = ".jpg";

    }
}
