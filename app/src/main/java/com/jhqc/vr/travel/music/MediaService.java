package com.jhqc.vr.travel.music;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.jhqc.vr.travel.LockScreenActivity;
import com.jhqc.vr.travel.app.Constants;
import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.util.LogUtils;

/**
 * Created by Solomon on 2017/10/21 0021.
 * 为了可以使得在后台播放音乐，我们需要Service
 * Service就是用来在后台完成一些不需要和用户交互的动作
 *
 * @author Administrator
 */
public class MediaService extends Service {

    private final IBinder binder = new AudioBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    //在这里我们需要实例化MediaPlayer对象
    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver();
    }

    /**
     * 该方法在SDK2.0才开始有的，替代原来的onStart方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String ORDER = intent.getStringExtra(Constants.MEDIA_ORDER_COMMOND_KEY);
            String[] params = intent.getStringArrayExtra(Constants.MEDIA_ORDER_PARAM);
            String param1 = null;
            String param2 = null;
            if (params != null && params.length != 0) {
                param1 = params[0];
                if (params.length > 1) {
                    param2 = params[1];
                }
            }

            switch (ORDER) {
                case Constants.ORDER.PLAY:
                    play(param1);
                    break;
                case Constants.ORDER.PAUSE:
                    pause(param1);
                    break;
                case Constants.ORDER.RESUME:
                    resume(param1);
                    break;
                case Constants.ORDER.STOP:
                    stop(param1);
                    break;
                case Constants.ORDER.SEEK:
                    seek(param1, param2);
                    break;
                default:
                    break;
            }
            LogUtils.logMedia("onStartCommand..." + ORDER);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mLockReceiver, intentFilter);
        registerReceiver(mLockReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_MEDIA);
        registerReceiver(mMediaReceiver, intentFilter);
    }
    private void unRegisterReceiver() {
        unregisterReceiver(mLockReceiver);
        unregisterReceiver(mMediaReceiver);
    }

    /**
     * 为了和Activity交互，我们需要定义一个Binder对象
     */

    class AudioBinder extends Binder {

        MediaService getService() {
            return MediaService.this;
        }
    }

    public void play(String fileName) {
        MediaManager.get(this.getBaseContext()).play(ConfigManager.get(getBaseContext()).getMP3FileDescriptor(String.valueOf(fileName)));
    }

    public void pause(String fileName) {
        MediaManager.get(this.getBaseContext()).pause(fileName);
    }

    public void resume(String fileName) {
        MediaManager.get(this.getBaseContext()).resume(fileName);
    }

    public void stop(String fileName) {
        MediaManager.get(this.getBaseContext()).stop(fileName);
    }

    public void seek(String fileName, String indexDur) {
        MediaManager.get(this.getBaseContext()).seek(fileName, indexDur);
    }

    private BroadcastReceiver mLockReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF) && MediaManager.get(MediaService.this.getBaseContext()).isPlaying()) {
                Intent lockscreen = new Intent(MediaService.this, LockScreenActivity.class);
                lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(lockscreen);
            }
        }
    };

    private BroadcastReceiver mMediaReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_MEDIA)) {
            }
        }
    };
}
