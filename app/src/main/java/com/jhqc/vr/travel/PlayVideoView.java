package com.jhqc.vr.travel;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ScreenUtils;
import com.jhqc.vr.travel.util.ToastUtils;
import com.jhqc.vr.travel.util.ViewUtils;
import com.jhqc.vr.travel.weight.CircleImageView;
import com.jhqc.vr.travel.weight.MarqueeTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class PlayVideoView extends FrameLayout {

    public static final String RESNAME_TAG = "RESNAME_TAG";
    private int screenWidth;
    private int screenHeight;
    private int screenWidthHalf;
    private int statusHeight;

//    String resName;

    VideoView videoView;
    String resName;
    View loadingView;
//    ImageButton backView;

    public PlayVideoView(Context context) {
        super(context);
        init();
    }

    public PlayVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//    int pddding = ViewUtils.dipToPixel(PADDING, getContext());
    private void init(){
        initScreenWH();

//        imageView = new CircleImageView(this.getContext());
//        LayoutParams iLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        iLp.gravity = Gravity.CENTER;
//        iLp.setMargins(pddding, pddding, pddding, pddding);
//        imageView.setLayoutParams(iLp);
//        imageView.setImageResource(R.drawable.drag);
//
//        coverImageView = new ImageView(this.getContext());
//        LayoutParams coverLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        coverImageView.setLayoutParams(coverLp);
//        coverImageView.setBackgroundResource(R.drawable.drag_center);
//
//        textView = new MarqueeTextView(this.getContext());
//        LayoutParams tvLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        textView.setLayoutParams(tvLp);
//        textView.setSingleLine();
//        textView.setFocusable(true);
//        textView.setText("");
//        textView.setTextSize(ViewUtils.dipToPixel(ScreenUtils.getScreenType(getContext()) == ScreenUtils.ScreenType.SUPER_LARGE ? 2.31f : 3.8f,
//                getContext()));
//        textView.setTextColor(getResources().getColor(R.color.whrite_half));
//        textView.setShadowLayer(2, 2, 2, getResources().getColor(R.color.black));
//
//        this.addView(imageView);
//        this.addView(coverImageView);
//        this.addView(textView);

//        ViewLayoutInflater.from(getContext()).inflate(R.layout.video_player_layout, null);
//        resName = "tongting";

        videoView = new VideoView(this.getContext());
        LayoutParams coverLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(coverLp);
        this.addView(videoView, coverLp);
        setBackgroundResource(android.R.color.transparent);
//        loadingView = this.findViewById(R.id.loading);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.video_player_layout);
//
//        Intent intent = getIntent();
//        resName = intent.getStringExtra(RESNAME_TAG);
//        videoView = (VideoView) findViewById(R.id.vedio_player);
//        loadingView = findViewById(R.id.loading);
        MediaController mc = new MediaController(getContext());
        mc.setMediaPlayer(videoView);
        mc.setVisibility(View.VISIBLE);
        videoView.setMediaController(mc);

//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resID);

//        getAssets().open("filename");

//        String videoPath = "file:///android_asset/Video/"+resName+".mp4";
//        Log.d("frand", "video path "+ videoPath);
//        try {
//            videoView.setVideoURI(Uri.fromFile(inputstreamtofile(getContext().getAssets().open("Video/"+resName+".mp4"))));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        InputStream is = getAssets().open(videoPath);
//        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
//                postPlayEnd();
                LogUtils.logVedio("播放完成");
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                postPlayEnd();
                LogUtils.logVedio("播放错误" + i + "  " + i1);
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                loadingView.setVisibility(View.INVISIBLE);
                LogUtils.logVedio("播放准备");
            }
        });
//        loadingView.setVisibility(View.VISIBLE);
        videoView.setFocusable(true);
        videoView.requestFocus();
//        videoView.start();
        mc.show(5000);
    }

    public void initScreenWH() {
        screenWidth= ScreenUtils.getScreenWidth(getContext());
        screenWidthHalf = screenWidth/2;
        screenHeight=ScreenUtils.getScreenHeight(getContext());
        statusHeight=ScreenUtils.getStatusHeight(getContext());
    }

    public void setVideoUrl(String resName) {
        try {
            Log.d("frand", "try set video url "+resName);
            if (resName!=null && !resName.equals("")) {
                if (!resName.equals(this.resName)) {
                    Log.d("frand", "result set video url "+resName);
                    this.resName = resName;
                    videoView.setVideoURI(Uri.fromFile(inputstreamtofile(getContext().getAssets().open("Video/"+resName+".mp4"))));
                } else {
                    Log.d("frand", "result same video url and skip");
                }
            } else {
                videoView.setVideoURI(null);
                this.resName = null;
                Log.d("frand", "result set video url null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File inputstreamtofile(InputStream ins) {
        File file = new File("/sdcard/temp.mp4");
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!videoView.isPlaying()) {
//            videoView.resume();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        videoView.suspend();
//        postPlayEnd();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        UnityBridgeHandler.postUnityPlayVideoEnd();
//        releaseVideo();
//        videoView = null;
//    }
//
//    @Override
//    protected void attachBaseContext(Context newBase)
//    {
//        super.attachBaseContext(new ContextWrapper(newBase)
//        {
//            @Override
//            public Object getSystemService(String name)
//            {
//                if (Context.AUDIO_SERVICE.equals(name)) {
//                    LogUtils.logVedio("使用全局Context!");
//                    return getApplicationContext().getSystemService(name);
//                }
//                return super.getSystemService(name);
//            }
//        });
//    }
//
//    private void postPlayEnd() {
//        UnityBridgeHandler.postUnityPlayVideoEnd();
//        releaseVideo();
//        finish();
//    }
//
//    private void releaseVideo() {
//        if (videoView != null) {
//            try {
//                videoView.suspend();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            videoView.setOnPreparedListener(null);
//            videoView.setOnErrorListener(null);
//            videoView.setOnCompletionListener(null);
//            videoView.setMediaController(null);
//            videoView.stopPlayback();
//        }
//    }

}