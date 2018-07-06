package com.jhqc.vr.travel;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ToastUtils;

public class PlayVideoActivity extends AppCompatActivity {

    public static final String RESID_TAG = "RESID_TAG";

    int resID;

    VideoView videoView;
    View loadingView;
    ImageButton backView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_player_layout);

        Intent intent = getIntent();
        resID = intent.getIntExtra(RESID_TAG, 0);
        videoView = (VideoView) findViewById(R.id.vedio_player);
        loadingView = findViewById(R.id.loading);
        backView = (ImageButton) findViewById(R.id.vedio_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View iView) {
                postPlayEnd();
                videoView.stopPlayback();
            }
        });
        MediaController mc = new MediaController(this);
//        mc.setMediaPlayer(videoView);
//        mc.setVisibility(View.VISIBLE);
        videoView.setMediaController(mc);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resID);
        videoView.setVideoURI((uri));
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                postPlayEnd();
                LogUtils.logVedio("播放完成");
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                postPlayEnd();
                LogUtils.logVedio("播放错误" + i + "  " + i1);
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                loadingView.setVisibility(View.INVISIBLE);
            }
        });
        loadingView.setVisibility(View.VISIBLE);
        videoView.setFocusable(true);
        videoView.requestFocus();
        videoView.start();
        mc.show(5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!videoView.isPlaying()) {
            videoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.suspend();
        postPlayEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnityBridgeHandler.postUnityPlayVideoEnd();
        releaseVideo();
        videoView = null;
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(new ContextWrapper(newBase)
        {
            @Override
            public Object getSystemService(String name)
            {
                if (Context.AUDIO_SERVICE.equals(name)) {
                    LogUtils.logVedio("使用全局Context!");
                    return getApplicationContext().getSystemService(name);
                }
                return super.getSystemService(name);
            }
        });
    }

/*    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
            postPlayEnd();
        }
        return super.dispatchKeyEvent(event);
    }*/

    private void postPlayEnd() {
        UnityBridgeHandler.postUnityPlayVideoEnd();
        releaseVideo();
        finish();
    }

    private void releaseVideo() {
        if (videoView != null) {
            try {
                videoView.suspend();
            } catch (Exception e) {
            }
            videoView.setOnPreparedListener(null);
            videoView.setOnErrorListener(null);
            videoView.setOnCompletionListener(null);
            videoView.setMediaController(null);
            videoView.stopPlayback();
        }
    }

}