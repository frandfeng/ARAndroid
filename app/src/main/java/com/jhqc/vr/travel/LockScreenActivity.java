package com.jhqc.vr.travel;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.control.MediaControl;
import com.jhqc.vr.travel.control.VrFactory;
import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.struct.Entry;
import com.jhqc.vr.travel.util.OtherUtils;
import com.jhqc.vr.travel.util.ViewUtils;

public class LockScreenActivity extends BaseActivity implements View.OnClickListener {

    private SeekBar mSeekBar;
    private TextView mDurTV;
    private TextView mTotalTV;
    private TextView mTitleTV;
//    private TextView nAudioDesTV;

    private ImageButton mPlayIBtn;
    //private ImageButton mBackIBtn;
    private ImageView nAudioIconIV;

    MediaControl mediaControl = VrFactory.get().getMediaControl();

    MediaControl.MediaPlayListener mediaPlayListener;

    Entry<String, BitmapDrawable> iconEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lock_screen);

        initView();
        initPlayer();
    }

    private void initView() {
        mSeekBar = (SeekBar) findViewById(R.id.lockscreen_seekbar);
        mDurTV = (TextView) findViewById(R.id.lockscreen_duration_played_textview);
        mTotalTV = (TextView) findViewById(R.id.lockscreen_duration_total_textview);
        mTitleTV = (TextView) findViewById(R.id.lockscreen_title_tv);
        //nAudioDesTV = findViewById(R.id.lockscreen_desc_tv);
        nAudioIconIV = (ImageView) findViewById(R.id.lockscreen_image);
        mPlayIBtn = (ImageButton) findViewById(R.id.lockscreen_play_button_icon);
        //mBackIBtn = findViewById(R.id.lockscreen_back_button_icon);
        mPlayIBtn.setOnClickListener(this);
        //mBackIBtn.setOnClickListener(this);

        mSeekBar.setMax(1000);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastProccess;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                lastProccess = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float value = (float) seekBar.getProgress() / (float) seekBar.getMax();
                boolean isSuc = mediaControl.seekTo(value);
                if (!isSuc) {
                    seekBar.setProgress(lastProccess);
                }
            }
        });
    }

    public void initPlayer() {
        mediaPlayListener = new MediaControl.MediaPlayListener<MScenicSpot>() {
            @Override
            public void onStart(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                final int dur = mediaPlayer.getCurrentPosition();
                final int total = mediaPlayer.getDuration();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayIBtn.setBackgroundResource(R.drawable.pause);
                        mSeekBar.setProgress(0);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText(OtherUtils.changeToTimeStr(dur));
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void onResume(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int dur = mediaPlayer.getCurrentPosition();
                        int total = mediaPlayer.getDuration();
                        int pos = (int) (1000L * dur / total);
                        mPlayIBtn.setBackgroundResource(R.drawable.pause);
                        mSeekBar.setProgress(pos);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText(OtherUtils.changeToTimeStr(dur));
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void onPause(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int dur = mediaPlayer.getCurrentPosition();
                        int total = mediaPlayer.getDuration();
                        int pos = (int) (1000L * dur / total);
                        mPlayIBtn.setBackgroundResource(R.drawable.play);
                        mSeekBar.setProgress(pos);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText(OtherUtils.changeToTimeStr(dur));
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void seekTo(MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                final int dur = mediaPlayer.getCurrentPosition();
                final int total = mediaPlayer.getDuration();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int pos = (int) (1000L * dur / total);
                        mPlayIBtn.setBackgroundResource(R.drawable.pause);
                        mSeekBar.setProgress(pos);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText(OtherUtils.changeToTimeStr(dur));
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void onStop(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int total = mediaPlayer.getDuration();
                        mPlayIBtn.setBackgroundResource(R.drawable.play);
                        mSeekBar.setProgress(0);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText("00:00");
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void onComplate(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager pm = (PowerManager) LockScreenActivity.this.getBaseContext().getSystemService(Context.POWER_SERVICE);
                        boolean screen = pm.isScreenOn();
                        if (screen) {
                            int total = mediaPlayer.getDuration();
                            mPlayIBtn.setBackgroundResource(R.drawable.play);
                            mSeekBar.setProgress(0);
                            mTitleTV.setText(scenicSpot.getName());
                            //nAudioDesTV.setText(scenicSpot.getDes());
                            nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                            mDurTV.setText("00:00");
                            mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                        } else {
                            finish();
                        }
                    }
                });
            }

            @Override
            public void onError(final MediaPlayer mediaPlayer,final MScenicSpot scenicSpot, String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int total = mediaPlayer.getDuration();
                        mPlayIBtn.setBackgroundResource(R.drawable.play);
                        mSeekBar.setProgress(0);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText("00:00");
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }

            @Override
            public void onProccess(final MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                final int dur = mediaPlayer.getCurrentPosition();
                final int total = mediaPlayer.getDuration();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int pos = (int) (1000L * dur / total);
                        mPlayIBtn.setBackgroundResource(R.drawable.pause);
                        mSeekBar.setProgress(pos);
                        mTitleTV.setText(scenicSpot.getName());
                        //nAudioDesTV.setText(scenicSpot.getDes());
                        nAudioIconIV.setBackground(getSpotIcon(scenicSpot.getIconFileName()));
                        mDurTV.setText(OtherUtils.changeToTimeStr(dur));
                        mTotalTV.setText(OtherUtils.changeToTimeStr(total));
                    }
                });
            }
        };
        mediaControl.registerListener(mediaPlayListener);
    }

    public BitmapDrawable getSpotIcon(String fileName) {
        BitmapDrawable bitmapDrawable = null;

        if (iconEntry == null) {
            bitmapDrawable = new BitmapDrawable(ConfigManager.get(LockScreenActivity.this.getBaseContext()).getIconBitmapFile(fileName, 0));
            iconEntry = new Entry<>(fileName, bitmapDrawable);
        } else if (!fileName.equals(iconEntry.getKey())) {
            ViewUtils.releaseDrawable(iconEntry.getValue());
            bitmapDrawable = new BitmapDrawable(ConfigManager.get(LockScreenActivity.this.getBaseContext()).getIconBitmapFile(fileName, 0));
            iconEntry.setKey(fileName);
            iconEntry.setValue(bitmapDrawable);
        } else {
            bitmapDrawable = iconEntry.getValue();
        }

        return bitmapDrawable;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.lockscreen_play_button_icon){
            if (mediaControl.isPlaying()) {
                mediaControl.pause(true);
            } else {
                mediaControl.resume();
            }
        } /*else if (id == R.id.lockscreen_back_button_icon) {
            finish();
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaControl.unregisterListener(mediaPlayListener);
        mediaControl = null;
        nAudioIconIV.removeCallbacks(null);
        ViewUtils.releaseImageViewResource(nAudioIconIV);
        ViewUtils.releaseBackgroundDrawable(nAudioIconIV);
        System.gc();
    }
}
