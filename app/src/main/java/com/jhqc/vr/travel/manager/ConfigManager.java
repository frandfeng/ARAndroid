package com.jhqc.vr.travel.manager;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.LruCache;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.model.MConfig;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.model.Pack;
import com.jhqc.vr.travel.struct.PConfig;
import com.jhqc.vr.travel.util.FileUtils;
import com.jhqc.vr.travel.util.LogUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class ConfigManager {

    public static int DEFAULT_MUTI = 1;

    static byte[] lock = new byte[1];

    static ConfigManager instance;

    Context mContext;

    DataMapper mapper;

    ArrayList<MConfig> dataList = new ArrayList<>();

//    Map<String, SoftReference<Bitmap>> mBitmapCache = new HashMap<>();

    // LruCache通过构造函数传入缓存值，以KB为单位。
    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // 使用最大可用内存值的1/8作为缓存的大小。
    int cacheSize = maxMemory / 8;
    LruCache<String, Bitmap> mBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // 重写此方法来衡量每张图片的大小，默认返回图片数量。
            return bitmap.getByteCount() / 1024;
        }
    };

    static final String DIR_CONFIG = "Config";
    static final String DIR_SONG = "Song";
    static final String DIR_ICON = "Texture2d";

    ConfigManager(Context ctx) {
        this.mContext = ctx;
        mapper = new DataMapper();
    }

    public static ConfigManager get(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ConfigManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public ArrayList<MConfig> getConfig() {
        if (dataList.isEmpty()) {
            AssetManager manager = this.mContext.getAssets();
            InputStream is = null;
            try {
                is = mContext.getResources().openRawResource(R.raw.config);
                String content = FileUtils.read2String(is);
                Pack pack = DataLoader.loadDataByJson(content, Pack.class);

                if (pack != null && pack.getDatas().size() != 0) {
                    pack = fullData(manager, pack);
                    dataList.addAll(pack.getDatas());
                }

            } catch (Exception e) {
                LogUtils.logError(e.getMessage() + e.getLocalizedMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return dataList;
    }

    public Bitmap getIconBitmapFile(String fileName, int scale) {
        if(mBitmapCache.get(fileName) != null) {
            return mBitmapCache.get(fileName);
        }
        fileName = DIR_ICON + "/" + fileName + FileUtils.FileExpand.PNG;
        Bitmap bitmap = null;
        AssetManager am = mContext.getResources().getAssets();
        InputStream is = null;
        try {
            is = am.open(fileName);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inSampleSize = scale == 0 ? DEFAULT_MUTI : scale;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            mBitmapCache.put(fileName, bitmap);
        } catch (IOException e) {
            LogUtils.logView(e.getMessage());
        }  finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return bitmap;
    }

    public AssetFileDescriptor getMP3FileDescriptor(String fileName) {
        fileName = DIR_SONG + "/" + fileName + FileUtils.FileExpand.MP3;
        AssetManager am = mContext.getResources().getAssets();
        try {
            return am.openFd(fileName);
        } catch (IOException e) {
            LogUtils.logView(e.getMessage());
        }
        return null;
    }

    public MediaPlayer playRing(String fileName) {
        fileName = DIR_SONG + "/" + fileName + FileUtils.FileExpand.MP3;
        AssetManager assetManager = mContext.getResources().getAssets();
        MediaPlayer player = null;
        AssetFileDescriptor fileDescriptor = null;
        try {
            player = new MediaPlayer();
            fileDescriptor = assetManager.openFd(fileName);
            player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getStartOffset());
            player.prepare();
            player.start();
        } catch (IOException e) {
            LogUtils.logError(e.getMessage() + e.getLocalizedMessage());
        }
        FileUtils.closeAssetFileDescriptor(fileDescriptor);
        return player;
    }

    Pack fullData(AssetManager manager, Pack pack) {
        PConfig config;
        ArrayList<MScenicSpot> mScenicSpots;
        for (MConfig mConfig : pack.getDatas()) {
            try {
                config = getPConfigByFile(manager, mConfig.getConfigID() + FileUtils.FileExpand.TXT);
                mScenicSpots = mapper.convertP2M(config.getDatas());

                mConfig.setScenicSpotList(mScenicSpots);
            } catch (Exception e) {
                LogUtils.logError(e.getMessage() + e.getLocalizedMessage());
            }
        }

        return pack;
    }

    PConfig getPConfigByFile(AssetManager manager, String fileName) throws Exception {
        PConfig config = null;
        InputStream is = manager.open(DIR_CONFIG + "/" + fileName);
        String content = FileUtils.read2String(is);
        config = DataLoader.loadDataByJson(content, PConfig.class);
        is.close();
        return config;
    }

}
