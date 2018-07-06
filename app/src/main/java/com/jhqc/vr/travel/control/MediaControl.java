package com.jhqc.vr.travel.control;

import android.content.Context;
import android.media.MediaPlayer;

import com.jhqc.vr.travel.app.Constants;
import com.jhqc.vr.travel.manager.GlobManager;
import com.jhqc.vr.travel.music.MediaManager;
import com.jhqc.vr.travel.model.MScenicSpot;

import java.util.ArrayList;

/**
 * Created by Solomon on 2017/10/21 0021.
 */

public class MediaControl {

    Context mContext;

    ArrayList<MediaPlayListener<MScenicSpot>> mPlayListeners = new ArrayList<>();

    MediaManager manager;

    /** 是否抢占播放器资源而暂停 */
    boolean isFightPlayerAutoPaused = false;

    /** 手动暂停 */
    boolean isClickPaused = false;

    MediaControl(Context context) {
        this.mContext = context;

        manager = MediaManager.get(mContext);
        manager.addCallBack(callBack);
    }

    MediaControl(Context context, MediaPlayListener<MScenicSpot> playListener) {
        this.mContext = context;
        this.mPlayListeners.add(playListener);

        manager = MediaManager.get(mContext);
        manager.addCallBack(callBack);
    }

    public void registerListener(MediaPlayListener<MScenicSpot> playListener) {
        if (!mPlayListeners.contains(playListener)) {
            mPlayListeners.add(playListener);
        }
    }

    public void unregisterListener(MediaPlayListener<MScenicSpot> playListener) {
        mPlayListeners.remove(playListener);
    }


    public void play(MScenicSpot scenicSpot) {
        if (scenicSpot == null) {
            return;
        }
        //MScenicSpot oldSpot = GlobManager.get().getCurSpot();
        GlobManager.get().setCurSpot(scenicSpot);

        if (scenicSpot != null && scenicSpot.getId() != 0) {
            manager.execCommond(this.mContext, Constants.ORDER.PLAY, String.valueOf(scenicSpot.getId()));
        }
    }

    public void pause(boolean isActiveClick) {
        MScenicSpot scenicSpot = GlobManager.get().getCurSpot();
        if (scenicSpot != null && scenicSpot.getId() != 0) {
            manager.execCommond(this.mContext, Constants.ORDER.PAUSE, String.valueOf(scenicSpot.getId()));
        }
        setFightPlayerAutoPaused(!isActiveClick);
        setClickPaused(isActiveClick);
    }

    public void resume() {
        MScenicSpot scenicSpot = GlobManager.get().getCurSpot();
        if (scenicSpot != null && scenicSpot.getId() != 0) {
            manager.execCommond(this.mContext, Constants.ORDER.RESUME, String.valueOf(scenicSpot.getId()));
        }
    }

    public boolean seekTo(float indexDur) {
        MScenicSpot scenicSpot = GlobManager.get().getCurSpot();
        if (isPlaying(scenicSpot) && isPlaying()) {
            manager.execCommond(this.mContext, Constants.ORDER.SEEK, String.valueOf(scenicSpot.getId()), String.valueOf(indexDur));
            return true;
        } else {
            return false;
        }
    }

    public void stop() {
        MScenicSpot scenicSpot = GlobManager.get().getCurSpot();
        if (scenicSpot != null && scenicSpot.getId() != 0) {
            manager.execCommond(this.mContext, Constants.ORDER.STOP, String.valueOf(scenicSpot.getId()));
        }
    }

    /**
     * 获取当前正在播放或者暂停的音频进度
     * @return
     */
    public int getCurrentSpotDur() {
        if (manager != null && getPlayingSpot() != null) {
            return manager.getDur();
        }
        return 0;
    }

    /**
     * 获取当前正在播放或者暂停的音频总长度
     * @return
     */
    public int getCurrentSpotTotal() {
        if (manager != null && getPlayingSpot() != null) {
            return manager.getTotal();
        }
        return 0;
    }

    public boolean isPlaying() {
        return manager.isPlaying();
    }

    public boolean isPlaying(MScenicSpot scenicSpot) {
        if (scenicSpot == null || getPlayingSpot() == null ) {
            return false;
        }
        return scenicSpot.getId() == getPlayingSpot().getId();
    }

    public MScenicSpot getPlayingSpot () {
        return GlobManager.get().getCurSpot();
    }

    public boolean isFightPlayerAutoPaused() {
        return isFightPlayerAutoPaused;
    }

    public MediaControl setFightPlayerAutoPaused(boolean fightPlayerAutoPaused) {
        isFightPlayerAutoPaused = fightPlayerAutoPaused;
        return this;
    }

    public boolean isClickPaused() {
        return isClickPaused;
    }

    public MediaControl setClickPaused(boolean clickPaused) {
        isClickPaused = clickPaused;
        return this;
    }

    public void release(boolean isStopMedia) {
        if (isStopMedia) {
            this.stop();
        }
        this.manager.removeCallBack(callBack);
        this.manager = null;
        this.mContext = null;
        mPlayListeners.clear();
    }

    MediaManager.MediaCallBack callBack = new MediaManager.MediaCallBack() {
        @Override
        public void onStart(MediaPlayer mp) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onStart(mp, getPlayingSpot());
            }
        }

        @Override
        public void onResume(MediaPlayer mp) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onResume(mp, getPlayingSpot());
            }
        }

        @Override
        public void onPause(MediaPlayer mp) {
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onPause(mp, getPlayingSpot());
            }
        }

        @Override
        public void onStop(MediaPlayer mp) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onStop(mp, getPlayingSpot());
            }
        }

        @Override
        public void seekTo(MediaPlayer mp) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.seekTo(mp, getPlayingSpot());
            }
        }

        @Override
        public void onComplate(MediaPlayer mp) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onComplate(mp, getPlayingSpot());
            }
        }

        @Override
        public void onError(MediaPlayer mp, String error) {
            setFightPlayerAutoPaused(false);
            setClickPaused(false);
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onError(mp, getPlayingSpot(), error);
            }
        }

        @Override
        public void onProccess(MediaPlayer mPlayer) {
            for (MediaPlayListener mPlayListener : mPlayListeners) {
                mPlayListener.onProccess(mPlayer, getPlayingSpot());
            }
        }
    };

    public interface MediaPlayListener<T> {

        /**
         * 开始
         */
        void onStart(MediaPlayer mp, T t);

        /**
         * 继续
         */
        void onResume(MediaPlayer mp, T t);

        /**
         * 暂停
         */
        void onPause(MediaPlayer mp, T t);

        /**
         * 跳转(快进/快退)
         */
        void seekTo(MediaPlayer mp, T t);

        /**
         * 停止
         */
        void onStop(MediaPlayer mp, T t);

        /**
         * 播放结束
         */
        void onComplate(MediaPlayer mp, T t);

        /**
         * 播放错误
         */
        void onError(MediaPlayer mp, T t, String error);

        /**
         * 播放错误
         */
        void onProccess(MediaPlayer mp, T t);
    }

}
