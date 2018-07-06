package com.jhqc.vr.travel.music;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.jhqc.vr.travel.app.Constants;
import com.jhqc.vr.travel.util.FileUtils;
import com.jhqc.vr.travel.util.LogUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Solomon on 2017/10/21 0021.
 */

public class MediaManager {

    static final int MILLS = 1000;

    static byte[] lock = new byte[1];

    static MediaManager INSTANCE;

    Context mContext;

    MediaPlayer mPlayer;

    HashSet<MediaCallBack> callbacks = new HashSet<>();

    Timer timer;

    private MediaManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static MediaManager get(Context context) {
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    INSTANCE = new MediaManager(context);
                }
            }
        }

        return INSTANCE;
    }

    private void init() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //播放结束
                    LogUtils.logMedia("onCompletion");
                    for (MediaCallBack listener : callbacks) {
                        listener.onComplate(mp);
                    }
                }
            });
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //准备就绪
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    for (MediaCallBack listener : callbacks) {
                        listener.onError(mp, what + "" + extra);
                    }
//                    cancelAutoProccess();
                    return false;
                }
            });
        }
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return (mPlayer != null && mPlayer.isPlaying());
    }

    public int getDur() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getTotal() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    public void execCommond(Context context, String cmd, String... params) {
        if (context == null) {
            for (MediaCallBack listener : callbacks) {
                listener.onError(mPlayer, "IllParamException: context can not null!");
            }
        }
        Intent intent = new Intent(context, MediaService.class);
        intent.putExtra(Constants.MEDIA_ORDER_COMMOND_KEY, cmd);
        intent.putExtra(Constants.MEDIA_ORDER_PARAM, params);
        context.startService(intent);
    }

    /**
     * 播放
     */
    boolean play(AssetFileDescriptor descriptor) {
        boolean isSeccuss = true;

        if (isPlaying()) {
            mPlayer.stop();
        }
        init();
        try {
            mPlayer.reset();
            mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(),
                    descriptor.getLength());
            mPlayer.prepare();

            start();
        } catch (Exception e) {
            LogUtils.logMedia(e.getMessage() + e.getLocalizedMessage());
            for (MediaCallBack listener : callbacks) {
                listener.onError(mPlayer, e.getMessage());
            }
            LogUtils.logError(e.getMessage() + e.getLocalizedMessage());
            isSeccuss = false;
        } finally {
            FileUtils.closeAssetFileDescriptor(descriptor);
        }
        return isSeccuss;
    }

    /**
     * 继续播放
     *
     * @return 当前播放的位置 默认为0
     */
    boolean resume(String fileName) {
        /*if (!isPlaying()) {
            for (MediaCallBack listener : callbacks) {
                listener.onError(mPlayer, "resume Error!");
            }
            return false;
        }*/
        if (mPlayer != null) {
            mPlayer.start();
            try {
                autoProccess();
            } catch (Exception e) {
                LogUtils.logMedia(e.getMessage() + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        for (MediaCallBack listener : callbacks) {
            listener.onResume(mPlayer);
        }
        return true;
    }

    /**
     * 暂停播放
     *
     * @return 当前播放的位置
     */
    boolean seek(String fileName, String indexDur) {
        float indexScale = -1;
        try {
            indexScale = Float.parseFloat(indexDur);
        } catch (Exception e) {
        }
        if (!isPlaying() || indexScale == -1) {
            for (MediaCallBack listener : callbacks) {
                listener.onError(mPlayer, "seek error! not playing or index is not digst!");
            }
            return false;
        }
        if (mPlayer != null) {
            int index = (int) (mPlayer.getDuration() * indexScale);
            mPlayer.seekTo(index);
        }
        for (MediaCallBack listener : callbacks) {
            listener.seekTo(mPlayer);
        }
        return true;
    }

    /**
     * 暂停播放
     *
     * @return 当前播放的位置
     */
    boolean pause(String fileName) {
        if (!isPlaying()) {
            for (MediaCallBack listener : callbacks) {
                listener.onError(mPlayer, "pause error!");
            }
            return false;
        }
        if (mPlayer != null) {
            mPlayer.pause();
        }
        for (MediaCallBack listener : callbacks) {
            listener.onPause(mPlayer);
        }
        return true;
    }

    /**
     * 停
     */
    boolean stop(String fileName) {
        if (!isPlaying()) {
            return false;
        }
        if (mPlayer != null) {
            mPlayer.stop();
//            mPlayer.release();
        }
        for (MediaCallBack listener : callbacks) {
            listener.onStop(mPlayer);
        }
        return true;
    }

    private void start() {
        if (mPlayer != null) {
            mPlayer.start();
            try {
                autoProccess();
            } catch (Exception e) {
                LogUtils.logMedia(e.getMessage() + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        for (MediaCallBack listener : callbacks) {
            listener.onStart(mPlayer);
        }
    }

    private void autoProccess() throws Exception {
        if (this.timer != null) {
            try {
                cancelAutoProccess();
            } catch (Exception e) {
            }
        }

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtils.logMedia("进度....isPlaying()=" + isPlaying());
                if (isPlaying() && callbacks != null) {
                    LogUtils.logMedia("Timer进度....");
                    for (MediaCallBack listener : callbacks) {
                        listener.onProccess(mPlayer);
                    }
                } else { //TODO isPlaying不准
                    LogUtils.logMedia("Timer.cancel....");
                    try {
                        cancelAutoProccess();
                    } catch (Exception e) {
                    }
                }
            }
        }, MILLS, MILLS);
    }

    private void cancelAutoProccess() throws Exception {
        if (timer != null) {
            timer.cancel();
        }
        this.timer = null;
    }

    public void addCallBack(MediaCallBack listener) {
        this.callbacks.add(listener);
    }

    public void removeCallBack(MediaCallBack listener) {
        this.callbacks.remove(listener);
    }

    public void clearCallBacks() {
        this.callbacks.clear();
    }

    public interface MediaCallBack {
        /**
         * 开始
         */
        void onStart(MediaPlayer mp);

        /**
         * 继续
         */
        void onResume(MediaPlayer mp);

        /**
         * 暂停
         */
        void onPause(MediaPlayer mp);

        /**
         * 继续
         */
        void onStop(MediaPlayer mp);

        /**
         * 跳转(快进/快退)
         */
        void seekTo(MediaPlayer mp);

        /**
         * 停止
         */
        void onComplate(MediaPlayer mp);

        /**
         * 播放错误
         */
        void onError(MediaPlayer mp, String error);

        /**
         * 播放进度更新
         */
        void onProccess(MediaPlayer mPlayer);
    }

}
