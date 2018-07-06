package com.jhqc.vr.travel.weight;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ViewUtils;

/**
 * Created by Solomon on 2017/10/21 0021.
 */

public class MediaContainer extends FrameLayout {

    boolean playEnable;

    ImageView playIconView;

    SeekBar seekBar;

    TextView durTV;

    TextView totalTV;

    OnPlayClickListener onPlayClickListener;

    Drawable pauseDrawable, playDrawable;

    public MediaContainer(Context context) {
        super(context);
        init();
    }

    public MediaContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.player_container_layout, null);
        playIconView = (ImageView) view.findViewById(R.id.play_button_icon);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        durTV = (TextView) view.findViewById(R.id.duration_played_textview);
        totalTV = (TextView) view.findViewById(R.id.duration_total_textview);

        this.seekBar.setMax(1000);
        this.addView(view);

        playDrawable = getResources().getDrawable(R.drawable.play);
        pauseDrawable = getResources().getDrawable(R.drawable.pause);

        playIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            if (onPlayClickListener != null) {
                onPlayClickListener.onPlayClick();
            }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setSeekChangedListener(SeekBar.OnSeekBarChangeListener changedListener) {
        seekBar.setOnSeekBarChangeListener(changedListener);
    }

    public void setPlaying(String durStr, String totalStr) {
        this.playIconView.setImageDrawable(pauseDrawable);
        this.durTV.setText(durStr);
        this.totalTV.setText(totalStr);
    }

    public void setPaused() {
        this.playIconView.setImageDrawable(playDrawable);
    }

    public void setProccess(long dur, long total, String durStr, String totalStr) {
        int pos = (int) (1000L * dur / total);
        this.seekBar.setProgress(pos);

        this.durTV.setText(durStr);
        this.totalTV.setText(totalStr);
        this.playIconView.setImageDrawable(pauseDrawable);
        LogUtils.logMedia("setProccess.." + pos +"/1000");
    }

    public void reset(String totalStr) {
        this.seekBar.setProgress(0);
        this.playIconView.setImageDrawable(playDrawable);
        this.durTV.setText("00:00");
        this.totalTV.setText(TextUtils.isEmpty(totalStr) ? "00:00" : totalStr);
    }

    public void release() {
        ViewUtils.releaseDrawable(playDrawable);
        ViewUtils.releaseDrawable(pauseDrawable);
        ViewUtils.releaseBackgroundDrawable(playIconView);
        this.playIconView.removeCallbacks(null);
        this.removeCallbacks(null);
        this.removeAllViews();
    }

    public boolean isPlayEnable() {
        return playEnable;
    }

    public MediaContainer setPlayEnable(boolean playEnable) {
        this.playEnable = playEnable;
        //TODO 图片置灰
        playIconView.setClickable(this.playEnable);
        return this;
    }

    public MediaContainer setOnPlayClickListener(OnPlayClickListener onPlayClickListener) {
        this.onPlayClickListener = onPlayClickListener;
        return this;
    }

    public OnPlayClickListener getOnPlayClickListener() {
        return onPlayClickListener;
    }

    public interface OnPlayClickListener {

        public void onPlayClick();

    }

}
