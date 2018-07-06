package com.jhqc.vr.travel;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.manager.VrActivityManager;

import java.io.IOException;

public class MusicPlayerService extends Service
        implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    public static final String TAG = "MusicPlayerService";
    private String id;
    private MediaSession mediaSession;
    private MediaSession.Token sessionToken;
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                VrActivityManager.startLockScreenActivity(MusicPlayerService.this);
            }
        }
    };

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        PlaybackState.Builder builder = new PlaybackState.Builder();
        builder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1);
        Bundle bundle = new Bundle();
        bundle.putInt("duration", mp.getDuration());
        builder.setExtras(bundle);
        mediaSession.setPlaybackState(builder.build());
    }

    public class LocalBinder extends Binder {
        public MediaSession.Token getMediaToken() {
            return sessionToken;
        }

        public void setId(String id_) {
            id = id_;
        }

        public String getId() {
            return id;
        }
    }

    public MusicPlayerService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaSession = new MediaSession(this, TAG);
        sessionToken = mediaSession.getSessionToken();
        mediaSession.setCallback(new MediaSession.Callback () {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onPlay() {
                try {
                    int duration = mediaPlayer.getDuration();
                    if (duration == -1 || duration == 1){
                        mediaPlayer.reset();
//                        ConfigManager.get(MusicPlayerService.this).get
                        AssetFileDescriptor fd = getAssets().openFd("stream/Song/" + id);
                        mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                        mediaPlayer.prepareAsync();
                    }
                    else {
                        mediaPlayer.start();
                        PlaybackState.Builder builder = new PlaybackState.Builder();
                        builder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1);
                        Bundle bundle = new Bundle();
                        bundle.putInt("duration", mediaPlayer.getDuration());
                        builder.setExtras(bundle);
                        mediaSession.setPlaybackState(builder.build());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onPause() {
                mediaPlayer.pause();
                PlaybackState.Builder builder = new PlaybackState.Builder();
                builder.setState(PlaybackState.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1);
                Bundle bundle = new Bundle();
                bundle.putInt("duration", mediaPlayer.getDuration());
                builder.setExtras(bundle);
                mediaSession.setPlaybackState(builder.build());
            }

            @Override
            public void onSeekTo(long pos) {
                mediaPlayer.seekTo((int)pos);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onCustomAction(String action, Bundle args) {
                if (action.compareTo("init") == 0) {
                    PlaybackState.Builder builder = new PlaybackState.Builder();
                    int state = PlaybackState.STATE_PAUSED;
                    if (mediaPlayer.isPlaying()) {
                        state = PlaybackState.STATE_PLAYING;
                    }
                    builder.setState(state, mediaPlayer.getCurrentPosition(), 1);
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", mediaPlayer.getDuration());
                    builder.setExtras(bundle);
                    mediaSession.setPlaybackState(builder.build());
                }
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder();
    }
}
