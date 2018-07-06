package com.jhqc.vr.travel.weight;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.music.VideoPlayerStatus;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Solomon on 2017/10/21 0021.
 * 控制视频进度,暂停,播放,声音,亮度.
 */
@Deprecated
public class MediaPlayerControler extends FrameLayout implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "VideoPlayer";
    //默认时间
    private static final int sDefaultTimeout = 1000;
    //顶部,底部控制栏飞出
    private static final int FADE_OUT = 1;
    //进度更新
    private static final int SHOW_PROGRESS = 2;

    //新的播放进度
    private int newPosition = 0;
    //中部透明浮层高度
    private int gLayerHeight = 0;
    //手势控制浮层快进/退 宽度
    private int seekLayerWidth = 0;
    //手势控制浮层音量/亮度宽度
    private int volLayerWidth = 0;
    //进度是否改变
    private boolean isProgressChanged = false;
    //是否拖动进度
    boolean mDragging;
    //顶部,底部状态栏是否显示
    boolean mShowing = true;
    //是否单击屏幕
    private boolean mTouched = false;
    //中间手势浮层视图是否已加载
    private boolean mCenterControlViewStubInit = false;
    //中间loading视图是否已加载
    private boolean mCenterLoadingStubInit = false;
    //底部浮层视图是否已加载
    private boolean mBottomViewStubInit = false;
    //中间提示浮层视图是否已加载
    private boolean mCenterTipViewStubInit = false;
    //手势开关
    private boolean mGesture = true;
    //是否需要快进
    private boolean mNeedSeek = false;

    //顶部title容器
    private LinearLayout mTitleLayout;

    //中间手势快进/退时间容器
    private LinearLayout mCenterControlTimeLayout;

    //中间浮层按钮容器
    private LinearLayout mCenterTipButtonLayout;

    //中间透明浮层容器
    private LinearLayout mCenterControlLayout;

    //底部播放控制进度容器
    private RelativeLayout mControlLayout;

    //顶部返回按钮
    private Button mBackButton;

    //底部浮层文字按钮(按需显示)
    private Button mBottomLayeTextButton;

    //中间浮层左边按钮
    private Button mCenterTipLeftButton;

    //中间浮层右边按钮
    private Button mCenterTipRightButton;

    //底部浮层关闭按钮
    private ImageButton mBottomLayerClose;

    //播放/暂停按钮
    private LinearLayout mPlaytButton;

    private ImageView mPlayButtonIcon;

    //中间浮层手势控制图标
    private ImageView mCenterControllerIcon;

    //中间浮层提示图标
    private ImageView mCenterTipIcon;

    //顶部title文字
    private TextView mTitleTextView;

    //当前播放时长文字
    private TextView mPlayedTimeTextView;

    //播放总时长文字
    private TextView mTotalTimeTextView;

    //中间浮层提示文字
    private TextView mCenterTipTextView;

    //中间透明浮层手势控制快进/快退当前时长
    private TextView mCenterControllerCTimeView;

    //中间透明浮层手势控制快进/快退总时长
    private TextView mCenterDurationView;

    //中间透明浮层文字提示
    private TextView mCenterDesView;

    //底部浮层文字描述
    private TextView mBottomLayerDes;

    //底部进度条
    private SeekBar mSeekBar;

    //中间浮层手势控制进度条
    private ProgressBar mCenterControllerBar;

    //中间浮层手势控制view
    private ViewStub mControllerViewStub;

    //底部浮层view
    private ViewStub mBottomLayerViewStub;

    //中间浮层提示view
    private ViewStub mCenterTipViewStub;

    private Context mContext;

    private Activity mActivity;

    private StringBuilder mStringBuilder = new StringBuilder();

    private StringBuilder mFormatBuilder;

    private Formatter mFormatter;

    private GestureDetector gestureDetector;

    VideoPlayerListener mVideoPlayerListener;

    public MediaPlayerControler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaPlayerControler(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        gestureDetector = new GestureDetector(context, new GestureListener());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_player, this);
        initView(view);

    }

    private void initView(View view) {
        mTitleLayout = (LinearLayout) view.findViewById(R.id.title_content_layout);
        //mLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        mControlLayout = (RelativeLayout) view.findViewById(R.id.control_layout);
        mBackButton = (Button) view.findViewById(R.id.back_btn);
        mPlaytButton = (LinearLayout) view.findViewById(R.id.play_button_layout);
        mPlayButtonIcon = (ImageView) view.findViewById(R.id.play_button_icon);
        mTitleTextView = (TextView) view.findViewById(R.id.title_textview);
        mPlayedTimeTextView = (TextView) view.findViewById(R.id.duration_played_textview);
        mTotalTimeTextView = (TextView) view.findViewById(R.id.duration_total_textview);
        mControllerViewStub = (ViewStub) view.findViewById(R.id.video_player_center_controller_view);
        mBottomLayerViewStub = (ViewStub) view.findViewById(R.id.video_player_bottom_layer_viewstub);
        mCenterTipViewStub = (ViewStub) view.findViewById(R.id.video_player_center_tip_view);
        mTitleLayout.getBackground().setAlpha(153);
        mControlLayout.getBackground().setAlpha(153);
        mPlaytButton.setOnClickListener(mPlayListener);
        mBackButton.setOnClickListener(mBackListener);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(1000);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        mTitleTextView.setText(title);
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 隐藏Loading
     *
     * @param mHideController 隐藏控制台
     */
    public void hideLoading(boolean mHideController) {
        if (mHideController) {
            hide();
        }
    }

    /**
     * 显示Loading
     *
     * @param mShowController 显示控制台
     */

    public void showLoading(boolean mShowController) {
        if (mShowController) {
            show();
//            setBackgroundColor(getResources().getColor(R.color.video_player_bg));
            hide();
        }

    }


    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * 显示
     */
    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            if (mPlaytButton != null) {
                mPlaytButton.requestFocus();
            }
            mShowing = true;
        }
        updatePlayStatus();

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        if (mTitleLayout.getVisibility() != VISIBLE) {
            mTitleLayout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_top));
            mTitleLayout.setVisibility(VISIBLE);
        }
        if (mControlLayout.getVisibility() != VISIBLE) {
            mControlLayout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_bottom));
            mControlLayout.setVisibility(VISIBLE);
        }
        if (mBottomLayerClose != null) {
            mBottomLayerClose.setImageResource(R.drawable.video_layer_close_normal);
        }

        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    /**
     * 隐藏
     */
    public void hide() {
        if (mShowing) {
            mHandler.removeMessages(SHOW_PROGRESS);
            mTitleLayout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_top));
            mControlLayout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_bottom));
            if (mBottomLayerClose != null) {
                mBottomLayerClose.setImageResource(R.drawable.video_layer_close_blue);
            }
            mTitleLayout.setVisibility(GONE);
            mControlLayout.setVisibility(GONE);

            mShowing = false;
        }
    }


    public void reset() {
        mGesture = false;
        showTitleLayout();
        mSeekBar.setProgress(0);
        resetBackground();
    }

    public void resetBackground() {
        setBackgroundColor(getResources().getColor(R.color.translate));
    }


    /**
     * 显示底部浮层
     *
     * @param title  浮层title
     * @param bText  浮层按钮名称
     * @param url    跳转地址
     * @param bColor 浮层文字按钮颜色
     */
    public void showBottomLayer(String title, String bText, String url, String bColor) {
        if (TextUtils.isEmpty(title))
            return;
        initBottomViewStub();
        mBottomLayerDes.setVisibility(View.VISIBLE);
        mBottomLayerDes.setText(title);
        if (!TextUtils.isEmpty(bText)) {
            int color = Color.parseColor(bColor);
            mBottomLayeTextButton.setText(bText);
            mBottomLayeTextButton.setTextColor(color);
            mBottomLayeTextButton.setTag(url);
        }
    }

    /**
     * 隐藏底部浮层
     */
    public void hideBottomLayer() {
        initBottomViewStub();
        mBottomLayerViewStub.setVisibility(View.GONE);
    }


    /**
     * 显示中间视图提示
     *
     * @param des  中间文字提示
     * @param type TYPE_SHOW_NETWORK_ERROR:网络错误 TYPE_SHOW_VIDEO_LOAD_FAIED:视频加载失败
     *             TYPE_SHOW_VIDEO_PLAY_ERROR:播放出错 TYPE_SHOW_VIDEO_TRIAL_COMPLETED:试看结束
     *             TYPE_SHOW_VIDEO_PLAY_NEXT:播放下一节 TYPE_SHOW_VIDEO_PLAY_COMPLETED:播放完成
     */
    public void showCenterTip(String des, String leftBText, String rightBText, int type) {
        LogUtils.logMedia("showCenterTip");
        initCenterTipViewStub();
        hideLoading(true);
        mCenterTipTextView.setText(des);
        switch (type) {
            case VideoPlayerStatus.TYPE_SHOW_NETWORK_ERROR:
            case VideoPlayerStatus.TYPE_SHOW_VIDEO_LOAD_FAIED:
                mCenterTipLeftButton.setVisibility(View.GONE);
                mCenterTipIcon.setVisibility(View.VISIBLE);
                mCenterTipIcon.setImageResource(R.drawable.video_warn);
                mCenterTipRightButton.setBackgroundResource(R.drawable.video_player_blue_btn);
                mCenterTipRightButton.setText(rightBText);
                mCenterTipRightButton.setTextColor(getResources().getColor(R.color.video_player_blue));
                mCenterTipRightButton.setVisibility(View.VISIBLE);
                mCenterTipRightButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_LOAD_FAIED);
                break;
            case VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_ERROR:
                mCenterTipButtonLayout.setVisibility(View.GONE);
                mCenterTipIcon.setVisibility(View.VISIBLE);
                mCenterTipIcon.setImageResource(R.drawable.video_warn);
                mCenterTipRightButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_ERROR);
                break;
            case VideoPlayerStatus.TYPE_SHOW_VIDEO_TRIAL_COMPLETED:
                mCenterTipLeftButton.setVisibility(View.GONE);
                mCenterTipIcon.setVisibility(View.VISIBLE);
                mCenterTipIcon.setImageResource(R.drawable.video_completed);
                mCenterTipRightButton.setBackgroundResource(R.drawable.video_player_orange_btn);
                mCenterTipRightButton.setText(rightBText);
                mCenterTipRightButton.setTextColor(getResources().getColor(R.color.video_player_orange));
                mCenterTipRightButton.setVisibility(View.VISIBLE);
                mCenterTipRightButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_TRIAL_COMPLETED);

                break;
            case VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_NEXT:
                mCenterTipIcon.setVisibility(View.GONE);
                mCenterTipRightButton.setVisibility(View.VISIBLE);
                mCenterTipRightButton.setBackgroundResource(R.drawable.video_player_blue_btn);
                mCenterTipRightButton.setText(rightBText);
                mCenterTipRightButton.setTextColor(getResources().getColor(R.color.video_player_blue));

                mCenterTipLeftButton.setVisibility(View.VISIBLE);
                mCenterTipLeftButton.setBackgroundResource(R.drawable.video_player_gray_btn);
                mCenterTipLeftButton.setText(leftBText);
                mCenterTipLeftButton.setTextColor(getResources().getColor(R.color.video_player_gray));
                mCenterTipRightButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_NEXT);
                mCenterTipLeftButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_NEXT);
                break;
            case VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_COMPLETED:
                //JLog.e(TAG, "show completed");
                mCenterTipLeftButton.setVisibility(View.GONE);
                mCenterTipIcon.setVisibility(View.VISIBLE);
                mCenterTipIcon.setImageResource(R.drawable.video_completed);
                mCenterTipRightButton.setBackgroundResource(R.drawable.video_player_blue_btn);
                mCenterTipRightButton.setText(rightBText);
                mCenterTipRightButton.setTextColor(getResources().getColor(R.color.video_player_blue));
                mCenterTipRightButton.setVisibility(View.VISIBLE);
                mCenterTipRightButton.setTag(VideoPlayerStatus.TYPE_SHOW_VIDEO_PLAY_COMPLETED);
                break;
        }
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleLayout.setBackgroundColor(getResources().getColor(R.color.translate));
        mTitleTextView.setVisibility(View.GONE);
        mBackButton.setTextColor(getResources().getColor(R.color.video_player_duration_gray));
        mGesture = false;
        setEnabled(false);
        hide();
        setBackgroundColor(getResources().getColor(R.color.video_player_bg));
        mCenterTipViewStub.setVisibility(View.VISIBLE);
    }

    /**
     * 显示恢复title默认状态
     */
    public void showTitleLayout() {
        mTitleLayout.setBackgroundColor(getResources().getColor(R.color.black));
        mTitleLayout.getBackground().setAlpha(153);
        mTitleTextView.setVisibility(View.VISIBLE);
        mBackButton.setTextColor(getResources().getColor(R.color.white));
    }


    /**
     * 隐藏中间提示
     */
    public void hideCenterTip() {
        initCenterTipViewStub();
        mCenterTipViewStub.setVisibility(View.GONE);
    }

    /**
     * 设置播放器
     */
    public void setVideoPlayer(VideoPlayerListener player) {
        mVideoPlayerListener = player;
        updatePlayStatus();
    }

    public void release() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    /**
     * 更新播放按钮状态,播放/暂停
     */
    private void updatePlayStatus() {
        if (mVideoPlayerListener != null && mVideoPlayerListener.isPlaying()) {
            mPlayButtonIcon.setImageResource(R.drawable.pause);
        } else {
            mPlayButtonIcon.setImageResource(R.drawable.play);
        }
    }

    private void doPauseResume() {
        updatePlayStatus();
        if (mVideoPlayerListener.isPlaying()) {
            mVideoPlayerListener.pause();
            show(3600000);
        } else {
            mVideoPlayerListener.start();
            show();
        }

    }

    /**
     * 设置播放进度
     */
    int setProgress() {
        if (mVideoPlayerListener == null || mDragging) {
            return 0;
        }
        int position = mVideoPlayerListener.getCurrentPosition();
        int duration = mVideoPlayerListener.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mVideoPlayerListener.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        setTime(position, duration);
        return position;
    }


    /**
     * 设置播放时间
     */
    private void setTime(long position, long duration) {
        mStringBuilder.setLength(0);
        //mStringBuilder.append(changeToTimeStr(position));
        mStringBuilder.append("/");
        mStringBuilder.append(changeToTimeStr(duration));
        if (mPlayedTimeTextView != null) {
            mPlayedTimeTextView.setText(changeToTimeStr(position));
        }
        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.setText(mStringBuilder);
        }
    }

    /**
     * s转换为00:00:00时间显示
     *
     * @return 00:00:00
     */
    private String changeToTimeStr(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = seconds / 60;
        long second = seconds % 60;
        long hours = seconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, second).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, second).toString();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPlaytButton.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
        mGesture = enabled;

    }

    /**
     * 动态初始化中间手势视图
     */
    private void initCenterViewStub() {
        if (!mCenterControlViewStubInit) {
            View view = mControllerViewStub.inflate();
            view.getBackground().setAlpha(153);
            mCenterControlLayout = (LinearLayout) view.findViewById(R.id.video_player_center_controller_layout);
            mCenterControllerIcon = (ImageView) view.findViewById(R.id.video_player_center_controller_icon);
            mCenterControlTimeLayout = (LinearLayout) view.findViewById(R.id.video_player_center_controller_time_layout);
            mCenterControllerCTimeView = (TextView) view.findViewById(R.id.video_player_center_controller_ctime);
            mCenterDurationView = (TextView) view.findViewById(R.id.video_player_center_controller_duration);
            mCenterDesView = (TextView) view.findViewById(R.id.video_player_center_controller_text);
            mCenterControllerBar = (ProgressBar) view.findViewById(R.id.video_player_center_controller_bar);
            mCenterControlViewStubInit = true;
            volLayerWidth = ViewUtils.dipToPixel(90, mContext);
            gLayerHeight = ViewUtils.dipToPixel(80, mContext);
            seekLayerWidth = ViewUtils.dipToPixel(80, mContext);
        }
    }

    /**
     * 动态初始化底部浮层
     */
    private void initBottomViewStub() {
        if (!mBottomViewStubInit) {
            View view = mBottomLayerViewStub.inflate();
            view.getBackground().setAlpha(153);
            mBottomLayeTextButton = (Button) view.findViewById(R.id.video_player_bottom_layer_button);
            mBottomLayerClose = (ImageButton) view.findViewById(R.id.video_player_bottom_layer_close);
            mBottomLayerDes = (TextView) view.findViewById(R.id.video_player_bottom_layer_des);
            mBottomLayeTextButton.setOnClickListener(mBottomLayerTextListener);
            mBottomLayerClose.setOnClickListener(mBottomLayerCloseListener);
            mBottomViewStubInit = true;
        }
    }

    /**
     * 动态初始化中部提示view
     */
    private void initCenterTipViewStub() {
        if (!mCenterTipViewStubInit) {
            View view = mCenterTipViewStub.inflate();
            mCenterTipIcon = (ImageView) view.findViewById(R.id.video_player_center_tip_icon);
            mCenterTipTextView = (TextView) view.findViewById(R.id.video_player_center_tip_text);
            mCenterTipLeftButton = (Button) view.findViewById(R.id.video_player_center_tip_left_button);
            mCenterTipRightButton = (Button) view.findViewById(R.id.video_player_center_tip_right_button);
            mCenterTipButtonLayout = (LinearLayout) view.findViewById(R.id.video_player_center_tip_button_layout);
            mCenterTipLeftButton.setOnClickListener(mCenterTipLeftClickListener);
            mCenterTipRightButton.setOnClickListener(mCenterTipRightClickListener);
            mCenterTipViewStubInit = true;
        }
    }

    public void setCLayerText(int width, int height, String content) {
        if (mCenterControlLayout == null) {
            return;
        }
        initCenterViewStub();
        setTimerTask();
        ViewGroup.LayoutParams params = mCenterControlLayout.getLayoutParams();
        params.width = ViewUtils.dipToPixel(width, mContext);
        params.height = ViewUtils.dipToPixel(height, mContext);
        mCenterControlLayout.setLayoutParams(params);
        mControllerViewStub.setVisibility(View.VISIBLE);
        mCenterControllerIcon.setVisibility(View.GONE);
        mCenterControlTimeLayout.setVisibility(View.GONE);
        mCenterControllerBar.setVisibility(View.GONE);
        mCenterDesView.setVisibility(View.VISIBLE);
        mCenterDesView.setText(content);
    }

    /**
     * 手势控制声音显示
     */
    private void setSoundsView(int value, int max) {
        initCenterViewStub();
        setTimerTask();
        ViewGroup.LayoutParams params = mCenterControlLayout.getLayoutParams();
        params.width = volLayerWidth;
        params.height = gLayerHeight;
        mCenterControlLayout.setLayoutParams(params);
        mControllerViewStub.setVisibility(View.VISIBLE);
        mCenterControllerIcon.setVisibility(View.VISIBLE);
        mCenterControlTimeLayout.setVisibility(View.GONE);
        mCenterDesView.setVisibility(View.GONE);
        mCenterControllerBar.setVisibility(View.VISIBLE);
        mCenterControllerBar.setMax(max);
        mCenterControllerBar.setProgress(value);
        if (value > 0) {
            mCenterControllerIcon.setImageResource(R.drawable.volume);
        } else {
            mCenterControllerIcon.setImageResource(R.drawable.novolume);
        }
    }

    /**
     * 手势控制亮度显示
     */
    private void setBrightnessView(int value) {
        initCenterViewStub();
        setTimerTask();
        ViewGroup.LayoutParams params = mCenterControlLayout.getLayoutParams();
        params.width = volLayerWidth;
        params.height = gLayerHeight;
        mCenterControlLayout.setLayoutParams(params);
        mControllerViewStub.setVisibility(View.VISIBLE);
        mCenterControllerIcon.setVisibility(View.VISIBLE);
        mCenterControlTimeLayout.setVisibility(View.GONE);
        mCenterDesView.setVisibility(View.GONE);
        mCenterControllerBar.setVisibility(View.VISIBLE);
        mCenterControllerIcon.setImageResource(R.drawable.bright);
        mCenterControllerBar.setMax(51);
        mCenterControllerBar.setProgress(value);
    }

    /**
     * 手势控制快进/后退显示
     */
    private void setSwipeProgress(boolean isForward) {
        initCenterViewStub();
        setTimerTask();
        ViewGroup.LayoutParams params = mCenterControlLayout.getLayoutParams();
        params.width = seekLayerWidth;
        params.height = gLayerHeight;
        mCenterControlLayout.setLayoutParams(params);
        mControllerViewStub.setVisibility(View.VISIBLE);
        mCenterControllerIcon.setVisibility(View.VISIBLE);
        mCenterControlTimeLayout.setVisibility(View.VISIBLE);
        mCenterDesView.setVisibility(View.GONE);
        String currentPosition = changeToTimeStr(newPosition);
        String duration = mTotalTimeTextView.getText().toString();
        mCenterControllerCTimeView.setText(currentPosition);
        mCenterDurationView.setText(duration);
        if (isForward) {
            mCenterControllerIcon.setImageResource(R.drawable.forward);
        } else {
            mCenterControllerIcon.setImageResource(R.drawable.backward);
        }
        mCenterControllerBar.setVisibility(View.GONE);
    }


    /**
     * 设置手势控制时间任务,用于控制视图定时消失
     */
    private void setTimerTask() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, sDefaultTimeout);
    }

    /**
     * 滑动手势监听
     */
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final float STEP_PROGRESS = 2f;//根据屏幕像素设定滑动步长,防止滑动过快
        private static final int DEFAULT_SEEK_PROGRESS = 1000;//默认快进后退阀值
        private static final int VOLUME_THRESHOLD = 3;//音量阀值
        private static final int BRIGHT_THRESHOLD = 5;//亮度阀值
        private static final float BRIGHTNESS_MAX_VALUE = 255f;//最大亮度值

        private boolean firstScroll = false;
        private float mSaveBrightness = 0f;
        private int mCurrentVolume;
        private int mScreenWidth;
        private int mMaxVolume;
        private AudioManager mAudioManager;
        private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量,3.调节亮度
        private static final int GESTURE_MODIFY_PROGRESS = 1;
        private static final int GESTURE_MODIFY_VOLUME = 2;
        private static final int GESTURE_MODIFY_BRIGHT = 3;

        public GestureListener() {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mMaxVolume = mMaxVolume * VOLUME_THRESHOLD;//设置最大音量进度值,*VOLUME_THRESHOLD为了滑动更平滑
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstScroll = true;
            return true;
        }


        /**
         * 双击事件 控制暂停/播放
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //if (e.getAction() == MotionEvent.ACTION_DOWN) {
            LogUtils.logMedia("onDoubleTap");
            if (mVideoPlayerListener != null) {
                doPauseResume();
            }
            // }
            return true;
        }

        /**
         * 单击事件
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mShowing) {
                        hide();
                        mTouched = true;
                    } else {
                        show(4000);
                        mTouched = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mTouched) {
                        mTouched = false;
                        show(4000);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    hide();
                    break;
                default:
                    break;
            }
            return true;

        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (e1 == null || e2 == null) {
                return false;
            }
            mScreenWidth = getMeasuredWidth();
            if (mScreenWidth <= 0) {
                return false;
            }
            float mOldX = e1.getX();
            if (firstScroll) {//避免在屏幕上操作切换混乱
                if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                    GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
                } else {
                    if (mOldX > mScreenWidth * 3.0 / 5) {// 屏幕右侧,调节音量
                        GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                    } else if (mOldX < mScreenWidth * 2.0 / 5) {// 屏幕左侧,调节亮度
                        GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                    }
                }
            }
            switch (GESTURE_FLAG) {
                case GESTURE_MODIFY_PROGRESS:
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                        if (distanceX >= ViewUtils.dipToPixel(STEP_PROGRESS, getContext())) {
                            // 快退
                            onSwipeLeft(DEFAULT_SEEK_PROGRESS);
                        } else if (distanceX <= -ViewUtils.dipToPixel(STEP_PROGRESS, getContext())) {
                            // 快进
                            onSwipeRight(DEFAULT_SEEK_PROGRESS);
                        }
                    }
                    break;
                case GESTURE_MODIFY_VOLUME:
                case GESTURE_MODIFY_BRIGHT:
                    if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                        if (distanceY >= ViewUtils.dipToPixel(STEP_PROGRESS, getContext())) {
                            //增大(音量,亮度)
                            onSwipeTop(GESTURE_FLAG);
                        } else if (distanceY <= -ViewUtils.dipToPixel(STEP_PROGRESS, getContext())) {
                            //减小(音量,亮度)
                            onSwipeBottom(GESTURE_FLAG);
                        }
                    }
                    break;
            }

            firstScroll = false;// 第一次scroll执行完成，修改标志
            return false;
        }

        /**
         * 向右滑动
         */
        void onSwipeRight(int seekTime) {
            int cx = mVideoPlayerListener.getCurrentPosition();
            int rx = mVideoPlayerListener.getDuration();
            if (mNeedSeek) {
                cx = newPosition;
            }
            if (rx - cx > DEFAULT_SEEK_PROGRESS) {
                mNeedSeek = true;
                newPosition = cx + seekTime;
            } else {
                newPosition = rx;
            }
            setProgress();
            setSwipeProgress(true);
        }

        /**
         * 向左滑动
         */
        void onSwipeLeft(int seekTime) {
            int cx = mVideoPlayerListener.getCurrentPosition();
            if (mNeedSeek) {
                cx = newPosition;
            }
            if (cx > DEFAULT_SEEK_PROGRESS) {
                mNeedSeek = true;
                newPosition = cx - seekTime;
            } else {
                newPosition = 1;
            }
            setProgress();
            setSwipeProgress(false);
        }

        /**
         * 向上滑动
         *
         * @param flag 如果是在左边区域则设置亮度,反之设置音量
         */
        void onSwipeTop(int flag) {
            if (flag == GESTURE_MODIFY_VOLUME) {
                setVoiceVolume(true);
            } else {
                setScreenBrightness(true);
            }
        }

        /**
         * 向下滑动
         *
         * @param flag 如果是在左边区域则设置亮度,反之这支音量
         */
        void onSwipeBottom(int flag) {
            if (flag == GESTURE_MODIFY_VOLUME) {
                setVoiceVolume(false);
            } else {
                setScreenBrightness(false);
            }
        }

        /**
         * 设置音量
         */
        private void setVoiceVolume(boolean isAdd) {
//            float volumeOffsetAccurate = mMaxVolume * percent;
//            Log.e(TAG,"volumeOffsetAccurate: "+volumeOffsetAccurate);
//            int volumeOffset = (int) volumeOffsetAccurate;
//            Log.e(TAG,"volumeOffset: "+volumeOffset);
//            if (volumeOffset == 0 && Math.abs(volumeOffsetAccurate) > 0.2f) {
//                if (isAdd) {
//                    volumeOffset = 1;
//                } else  {
//                    volumeOffset = -1;
//                }
//            }
//
//            if(mVolume == 0){
//                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            }
//            mVolume += volumeOffset;
//            if (mVolume < 0) {
//                mVolume = 0;
//            } else if (mVolume >= mMaxVolume) {
//                mVolume = mMaxVolume;
//            }
            if (mMaxVolume == 0) {
                mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mMaxVolume = mMaxVolume * VOLUME_THRESHOLD;
            }
            LogUtils.logMedia("setVoiceVolume: " + mCurrentVolume +"-"+ mMaxVolume);
            if (isAdd && mCurrentVolume <= mMaxVolume) {
                mCurrentVolume += 1;
            } else if (mCurrentVolume > 0) {
                mCurrentVolume -= 1;
            }
            if (mCurrentVolume < 0) {
                mCurrentVolume = 0;
            } else if (mCurrentVolume > mMaxVolume) {
                mCurrentVolume = mMaxVolume;
            }
            setSoundsView(mCurrentVolume, mMaxVolume);
            int mAdjustValue = mCurrentVolume / VOLUME_THRESHOLD;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAdjustValue, 0);
        }

        /**
         * 设置当前屏幕亮度值 0--255，并使之生效
         */
        private void setScreenBrightness(boolean isAdd) {
            if (null == mActivity || mActivity.isFinishing()) {
                return;
            }
            ContentResolver resolver = mActivity.getContentResolver();
            WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
//                Log.e(TAG,"currentBrightness: "+currentBrightness);
//                float brightnessOffset = percent * 5;
//                currentBrightness += brightnessOffset;
//                if (currentBrightness < 0) {
//                    currentBrightness = 0;
//                } else if (currentBrightness > 1) {
//                    currentBrightness = 1;
//                }
//                int progress = (int) (currentBrightness * 100);
//                Log.e(TAG,"progress: "+progress);
//                mActivity.getWindow().setAttributes(lp);
//                setBrightnessView(progress);

            /*try {
                float system_brightness = Settings.Global.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
                if (isAdd && mSaveBrightness <= BRIGHTNESS_MAX_VALUE) {
                    mSaveBrightness = mSaveBrightness >= 0 ?
                            mSaveBrightness + BRIGHT_THRESHOLD : system_brightness + BRIGHT_THRESHOLD;
                } else if (mSaveBrightness > 0) {
                    mSaveBrightness = mSaveBrightness >= 0 ?
                            mSaveBrightness - BRIGHT_THRESHOLD : system_brightness - BRIGHT_THRESHOLD;
                }
                if (mSaveBrightness < 0) {
                    mSaveBrightness = 0;
                } else if (mSaveBrightness > BRIGHTNESS_MAX_VALUE) {
                    mSaveBrightness = BRIGHTNESS_MAX_VALUE;
                }
                lp.screenBrightness = mSaveBrightness * (1f / BRIGHTNESS_MAX_VALUE);
                mActivity.getWindow().setAttributes(lp);
                Float value = mSaveBrightness / BRIGHT_THRESHOLD;
                setBrightnessView(value.intValue());
            } catch (Exception e) {
                mSaveBrightness = 0;
            }*/

        }
    }

    /**
     * 返回按钮监听
     */
    private final OnClickListener mBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == mActivity || mActivity.isFinishing()) {
                return;
            }
            mActivity.finish();
        }
    };

    /**
     * 播放按钮监听
     */
    private final OnClickListener mPlayListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mVideoPlayerListener != null) {
                doPauseResume();
            }
        }
    };


    /**
     * 底部浮层关闭按钮监听
     */
    private final OnClickListener mBottomLayerCloseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mBottomLayerViewStub.setVisibility(View.GONE);
        }
    };

    /**
     * 底部浮层文字按钮监听
     */
    private final OnClickListener mBottomLayerTextListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            if (mVideoPlayerListener.isPlaying()) {
                mVideoPlayerListener.pause();
            }
            mVideoPlayerListener.clickEvent(VideoPlayerStatus.POSITION_BOTTOM, url);

        }
    };

    private final OnClickListener mCenterTipLeftClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Object obj = v.getTag();
            mVideoPlayerListener.clickEvent(VideoPlayerStatus.POSITION_CENTER_LEFT, obj);
        }
    };

    private final OnClickListener mCenterTipRightClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Object obj = v.getTag();
            mVideoPlayerListener.clickEvent(VideoPlayerStatus.POSITION_CENTER_RIGHT, obj);
        }
    };

    /**
     * 开始拖动
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mVideoPlayerListener != null) {
            show(3600000);
            mDragging = true;
            mGesture = false;
            mHandler.removeMessages(SHOW_PROGRESS);
        }
    }

    /**
     * 进度变化
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (null == mVideoPlayerListener || !fromUser) {
            return;
        }
        long duration = mVideoPlayerListener.getDuration();
        long position = (duration * progress) / 1000L;
        newPosition = (int) position;
        setProgress();
        isProgressChanged = true;
    }

    /**
     * 拖动停止
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mVideoPlayerListener == null) {
            return;
        }
        if (isProgressChanged) {
            mVideoPlayerListener.seekTo(newPosition);
            setTime(newPosition, mVideoPlayerListener.getDuration());
            isProgressChanged = false;
        }
        mGesture = true;
        mDragging = false;
        setProgress();
        updatePlayStatus();
        show(3000);

        mShowing = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGesture && gestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (mNeedSeek && event.getAction() == MotionEvent.ACTION_UP) {
            mNeedSeek = false;
            if (mControllerViewStub != null) {
                mControllerViewStub.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.cover_anim_invsible));
                mControllerViewStub.setVisibility(View.GONE);
            }
            mVideoPlayerListener.seekTo(newPosition);
            showLoading(false);
        }
        return super.onTouchEvent(event);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                if (mPlaytButton != null) {
                    mPlaytButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mVideoPlayerListener.isPlaying()) {
                mVideoPlayerListener.start();
                updatePlayStatus();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mVideoPlayerListener.isPlaying()) {
                mVideoPlayerListener.pause();
                updatePlayStatus();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mControllerViewStub != null) {
                mControllerViewStub.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.cover_anim_invsible));
                mControllerViewStub.setVisibility(View.GONE);
            }
        }
    };

    private static class PlayerHandler extends Handler {
        WeakReference<MediaPlayerControler> mReference;

        PlayerHandler(MediaPlayerControler controller) {
            mReference = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPlayerControler controller = mReference.get();
            if (controller == null) {
                return;
            }
            int pos;
            switch (msg.what) {
                case FADE_OUT: //1
                    controller.hide();
                    break;
                case SHOW_PROGRESS: //2
                    pos = controller.setProgress();
                    if (!controller.mDragging && controller.mShowing
                            && controller.mVideoPlayerListener != null
                            && controller.mVideoPlayerListener.isPlaying()) {
                        // hideLoading(true);
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    private final Handler mHandler = new PlayerHandler(this);


    interface VideoPlayerListener {
        /**
         * 开始
         */
        void start();

        /**
         * 暂停
         */
        void pause();

        /**
         * 跳转(快进/快退)
         */
        void seekTo(int position);

        /**
         * 浮层按钮点击事件
         */
        void clickEvent(int position, Object object);

        /**
         * 总时长
         */
        int getDuration();

        /**
         * 当前播放位置
         */
        int getCurrentPosition();

        /**
         * 缓冲进度
         */
        int getBufferPercentage();

        /**
         * 是否在播放
         */
        boolean isPlaying();
    }
}

