package com.jhqc.vr.travel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.control.LocationControl;
import com.jhqc.vr.travel.control.MediaControl;
import com.jhqc.vr.travel.control.VrFactory;
import com.jhqc.vr.travel.location.LocationVRListener;
import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.manager.DataLoader;
import com.jhqc.vr.travel.manager.GlobManager;
import com.jhqc.vr.travel.manager.LocationsManager;
import com.jhqc.vr.travel.model.MConfig;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.unity.UnityConstants;
import com.jhqc.vr.travel.unity.model.UIntelligentState;
import com.jhqc.vr.travel.util.LoctionConvertUtils;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.OtherUtils;
import com.jhqc.vr.travel.util.ScreenUtils;
import com.jhqc.vr.travel.util.ToastUtils;
import com.jhqc.vr.travel.util.ViewUtils;
import com.jhqc.vr.travel.weight.MapPointView;
import com.jhqc.vr.travel.weight.MapView;
import com.jhqc.vr.travel.weight.MediaContainer;
import com.jhqc.vr.travel.weight.event.MapLongClickListener;
import com.jhqc.vr.travel.weight.event.support.MapMatrix;
import com.qozix.tileview.TileView;
import com.qozix.tileview.markers.MarkerLayout;
import com.vuforia.CameraDevice;
import com.vuforia.Vuforia;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TravelActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String POINT_TAG_LOCTION = "LOCATION";
    private static final String POINT_TAG_BIGGER = "BIGGER_";

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 4000;
    private static final int UI_ANIMATION_DELAY = 300;

    public static final double NORTH_WEST_LATITUDE = 0;
    public static final double NORTH_WEST_LONGITUDE = 0;
    public static double SOUTH_EAST_LATITUDE = 6000;
    public static double SOUTH_EAST_LONGITUDE = 4320;

    LocationControl locationControl = VrFactory.get().getLocationControl();
    LocationVRListener locationVrListener;

    MediaControl mediaControl = VrFactory.get().getMediaControl();
    MediaControl.MediaPlayListener mediaPlayListener;

    private FrameLayout mContentView;
    private MediaContainer mMPContainerView;
    private ViewGroup mTitleLayout;

    private TextView mTitleTextView;
    private ImageButton mTitleBackImgView;
//    private MapView mMapView;
    private TileView tileView;

    private boolean superViewVisible;

    MapPointView locPointView;

    PointF initPointF;

    Animation mShowTopAnim, mShowBottomAnim, mHideTopAnim, mHideBottomAnim;

    List<MScenicSpot> dataSpotList;

    Map<Integer, MapPointView> dataSpotViewList;

    int viewWidth, viewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_travel);

        SCREEN_WIDTH = ScreenUtils.getScreenWidth(this);     // 屏幕宽度（像素）
        SCREEN_HEIGHT = ScreenUtils.getScreenHeight(this);   // 屏幕高度（像素）
        viewWidth = ViewUtils.dipToPixel(17, TravelActivity.this);
        viewHeight = ViewUtils.dipToPixel(50, TravelActivity.this);

        initData();
        initView();
        initListner();
        show();
        initPlayStatus();

        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_INTELLIGENTSSTATE,
                OtherUtils.packUnityJsonData(DataLoader.classToJson(new UIntelligentState(true))).toString());
        LogUtils.logError("TravelActivity.onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        tileView.resume();
        /*if (LocationsManager.isOpenGPS(this) && locationControl != null) {
            locationControl.startAutoGPS();
        }*/

        /*if (mMapView != null && mMapView.getPointMaps() != null) {
            MapPointView locPV = mMapView.getPointMaps().get(POINT_TAG_LOCTION);
            if (locPV != null) {
                mMapView.indexPiontView(locPV);
            }
        }*/
    }

    boolean isToastAble = true;
    private void initLocation() {
        locPointView = null;
        locationVrListener = new LocationVRListener<MScenicSpot>() {
            @Override
            public void onReceive(final Location location) {
                LogUtils.logLoc("OnReive:" + location +"\n-----------------------------");
                if (location == null) {
                    return;
                }
                //TODO
                if (mediaControl.getPlayingSpot() != null) {
                    location.setLatitude(mediaControl.getPlayingSpot().getLatitude() + 100);
                    location.setLongitude(mediaControl.getPlayingSpot().getLongitude());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //ToastUtils.show(TravelActivity.this,"更新位置：lat=" + location.getLatitude() +" lon=" + location.getLongitude());
                        if (locPointView == null) {
                            initPointF = new PointF((float) SOUTH_EAST_LONGITUDE, (float)SOUTH_EAST_LATITUDE);
                            GlobManager.get().setCurLocation(location);
                            float[] xy = LoctionConvertUtils.getXYbyOffset(initPointF, new float[]{
                                    (float) GlobManager.get().getCurLocation().getLatitude(), (float) GlobManager.get().getCurLocation().getLongitude()}, GlobManager.get().getMConfig());
                            locPointView = MapPointView.Builder.build(TravelActivity.this, xy[0], xy[1], MapPointView.MARK_POINT, false, "");
                            locPointView.setTag(POINT_TAG_LOCTION);
                            tileView.addMarker(locPointView, xy[0], xy[1], null, null);
                        } else {
                            float[] ds = LoctionConvertUtils.distanceBettwenXY(initPointF,
                                    new float[]{(float) GlobManager.get().getCurLocation().getLatitude(), (float) GlobManager.get().getCurLocation().getLongitude()},
                                    new float[]{(float) location.getLatitude(), (float) location.getLongitude()}, GlobManager.get().getMConfig());
                            //TODO  mMapView.moveMapLocationXY(locPointView, ds[0], ds[1]);
                            LogUtils.logOther("定位点移动：" + ds[0] +"  " + ds[1]);
                            //TODO
                            if (ds[0] != 0 && ds[1] != 0) {
                                location.setLatitude(location.getLatitude() + 100);
                                GlobManager.get().setCurLocation(location);
                                float[] xy = LoctionConvertUtils.getXYbyOffset(initPointF, new float[]{
                                        (float) GlobManager.get().getCurLocation().getLatitude(), (float) GlobManager.get().getCurLocation().getLongitude()}, GlobManager.get().getMConfig());
                                //locPointView.release();
                                //tileView.removeMarker(locPointView);
                                //locPointView.setTag(POINT_TAG_LOCTION);
                                //tileView.addMarker(locPointView, xy[0], xy[1], null, null);
                                tileView.moveMarker(locPointView, xy[0], xy[1]);
                            }
                        }
                        GlobManager.get().setCurLocation(location);
                        tileView.indexOfChild(locPointView);
                    }
                });
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                LogUtils.logLoc("onFailed:" + msg);
                if (isToastAble) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(TravelActivity.this, "定位失败");
                            isToastAble = false;
                        }
                    });
                }
            }

            @Override
            public void onAutoPlay(MScenicSpot scenicSpot) {
                LogUtils.logBle("自动播放：" + scenicSpot.getName());
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (pointView.getType() != MapPointView.TRAVEL_MUSICE_BLUE) {
                            pointView.updatePointType(MapPointView.TRAVEL_MUSICE_BLUE);
                        }
                        pointView.updatePointType(MapPointView.TRAVEL_MUSICE_ORIG);
                    }
                });*/
            }
        };
        locationControl.registerLocationListener(locationVrListener);
    }

    private void initMediaPlayer() {
        mediaPlayListener = new MediaControl.MediaPlayListener<MScenicSpot>() {
            @Override
            public void onStart(MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                int dur = mediaPlayer.getCurrentPosition();
                int total = mediaPlayer.getDuration();
                mMPContainerView.setPlaying(OtherUtils.changeToTimeStr(dur), OtherUtils.changeToTimeStr(total));
                mMPContainerView.setPlayEnable(true);

                mTitleTextView.setText(scenicSpot.getName());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPointTypePlaying(scenicSpot);
                    }
                });
            }

            @Override
            public void onResume(MediaPlayer mediaPlayer, MScenicSpot scenicSpot) {
                int dur = mediaPlayer.getCurrentPosition();
                int total = mediaPlayer.getDuration();
                mMPContainerView.setPlaying(OtherUtils.changeToTimeStr(dur), OtherUtils.changeToTimeStr(total));
            }

            @Override
            public void onPause(MediaPlayer mediaPlayer, MScenicSpot scenicSpot) {
                mMPContainerView.setPaused();
            }

            @Override
            public void seekTo(MediaPlayer mediaPlayer, MScenicSpot scenicSpot) {
                final int dur = mediaPlayer.getCurrentPosition();
                final int total = mediaPlayer.getDuration();
                if (mMPContainerView != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMPContainerView.setProccess(dur, total, OtherUtils.changeToTimeStr(dur), OtherUtils.changeToTimeStr(total));
                        }
                    });
                }
            }

            @Override
            public void onStop(MediaPlayer mediaPlayer, MScenicSpot scenicSpot) {
                int total = mediaPlayer.getDuration();
                mMPContainerView.reset(OtherUtils.changeToTimeStr(total));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPointTypePlaying(null);
                    }
                });
            }

            @Override
            public void onComplate(MediaPlayer mediaPlayer, MScenicSpot scenicSpot) {
                int total = mediaPlayer.getDuration();
                mMPContainerView.reset(OtherUtils.changeToTimeStr(total));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPointTypePlaying(null);
                    }
                });
            }

            @Override
            public void onError(MediaPlayer mediaPlayer, MScenicSpot scenicSpot, String error) {
                mMPContainerView.reset(null);
                ToastUtils.show(TravelActivity.this, "播放错误：" + error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPointTypePlaying(null);
                    }
                });
            }

            @Override
            public void onProccess(MediaPlayer mediaPlayer, final MScenicSpot scenicSpot) {
                final int dur = mediaPlayer.getCurrentPosition();
                final int total = mediaPlayer.getDuration();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTitleTextView != null && scenicSpot != null &&
                                !scenicSpot.getName().equals(mTitleTextView.getText())) {
                            mTitleTextView.setText(scenicSpot.getName());
                        }
                        if (mMPContainerView != null) {
                            mMPContainerView.setProccess(dur, total, OtherUtils.changeToTimeStr(dur), OtherUtils.changeToTimeStr(total));
                        }
                    }
                });
            }
        };
        mediaControl.registerListener(mediaPlayListener);
    }

    private void initPlayStatus() {
        if (mediaControl.isClickPaused()) {
            final int dur = mediaControl.getCurrentSpotDur();
            final int total = mediaControl.getCurrentSpotTotal();
            MScenicSpot scenicSpot = mediaControl.getPlayingSpot();
            if (mTitleTextView != null && scenicSpot != null &&
                    !scenicSpot.getName().equals(mTitleTextView.getText())) {
                mTitleTextView.setText(scenicSpot.getName());
            }
            if (mMPContainerView != null) {
                mMPContainerView.setProccess(dur, total, OtherUtils.changeToTimeStr(dur), OtherUtils.changeToTimeStr(total));
                mMPContainerView.setPaused();
            }
        }
    }

    /**
     * 更新当前播放小图标
     * @param scenicSpot
     */
    private void switchPointTypePlaying(MScenicSpot scenicSpot) {
        boolean isPlaying = scenicSpot != null;
        if (dataSpotViewList != null) {
            Iterator iterator = dataSpotViewList.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                if (isPlaying && key.equals(scenicSpot.getId())) {
                    continue;
                }
                if (POINT_TAG_LOCTION.equals(key)) {
                    continue;
                }
                if (key != null && POINT_TAG_BIGGER.startsWith(key.toString())) {
                    continue;
                }
                MapPointView pointView = dataSpotViewList.get(key);
                MScenicSpot spot = (MScenicSpot) pointView.getObject();

                if (spot != null && spot.isNearest) { //TODO 此处，如果是显示多个最近，则判断为isNear
                    pointView.updatePointType(MapPointView.TRAVEL_MUSICE_ORIG);
                } else if (pointView.getType() != MapPointView.TRAVEL_MUSICE_BLUE) {
                    pointView.updatePointType(MapPointView.TRAVEL_MUSICE_BLUE);
                }
            }
        }

        if (isPlaying) {
            MapPointView pointView = dataSpotViewList.get(scenicSpot.getId());
            if (pointView != null) {
                if (scenicSpot.isNearest) {
                    pointView.updatePointType(MapPointView.TRAVEL_MUSICE_PLAY_ORIG);
                } else {
                    pointView.updatePointType(MapPointView.TRAVEL_MUSICE_PLAY_BLUE);
                }
            }
        }
    }

    private void initView() {
        mContentView = (FrameLayout) findViewById(R.id.content_layout);
        mMPContainerView = (MediaContainer) findViewById(R.id.bottom_layout);
        mTitleLayout = (ViewGroup) findViewById(R.id.title_layout);

        mTitleBackImgView = (ImageButton) findViewById(R.id.title_btn_back);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
//        mMapView = (MapView) findViewById(R.id.mapview);
        tileView = new TileView(this);
        tileView.setId(R.id.tileview_id);
        mContentView.addView(tileView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mMPContainerView.setPlayEnable(GlobManager.get().getCurSpot() != null);

        mShowTopAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        mShowBottomAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        mHideTopAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        mHideBottomAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
    }

    void initData() {
//        mMapView.setScaleEnable(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (GlobManager.get().getMConfig() == null) {
                    return;
                }
                String fileName = GlobManager.get().getMConfig().getMapFileName();
                if (!TextUtils.isEmpty(fileName)) {
                    Bitmap bitmap = ConfigManager.get(getBaseContext()).getMapBitmapFile(fileName, 1, (int) SCREEN_WIDTH);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
//                        mMapView.setMap(bitmap, bitmap.getWidth(), bitmap.getHeight());
                        SOUTH_EAST_LONGITUDE = bitmap.getWidth();
                        SOUTH_EAST_LATITUDE = bitmap.getHeight();
                        tileView.setSize(bitmap.getWidth(), bitmap.getHeight());
                        tileView.setBackgroundColor(0xFFe7e7e7);
                        File PHOTO_DIR = new File(Environment.getExternalStorageDirectory()+"/yiheyuan");//设置保存路径
                        for (int i=1; i<2; i++) {
                            int scale = 2 << i;
                            if (i==-1) {
                                scale = 1;
                            }
                            String path = String.format("/yiheyuan_%dx", scale);
                            //4
//                            File avaterFile = new File(PHOTO_DIR, path);//设置文件名称
//                        tileView.addDetailLevel( 0.2500f, "yiheyuan/yiheyuan_3x_%d_%d.png" );
//                        tileView.addDetailLevel( 0.5000f, "yiheyuan/yiheyuan_6x_%d_%d.png" );
//                        tileView.addDetailLevel( 1.0000f, "yiheyuan/yiheyuan_12x_%d_%d.png" );
                            tileView.addDetailLevel( scale/4,  PHOTO_DIR+path+"_%d_%d.png");
                            Log.i("frand", "add detail level " + PHOTO_DIR+path+"_%d_%d.png");
                        }
                        // markers should align to the coordinate along the horizontal center and vertical bottom
                        tileView.setMarkerAnchorPoints( -0.5f, -1.0f );

                        // provide the corner coordinates for relative positioning
                        tileView.defineBounds(
                                NORTH_WEST_LONGITUDE,
                                NORTH_WEST_LATITUDE,
                                SOUTH_EAST_LONGITUDE,
                                SOUTH_EAST_LATITUDE
                        );
                        initPointView(GlobManager.get().getMConfig());
                        initLocation();
                        initMediaPlayer();
                    }
                    break;
            }
        }
    };

    private void initListner() {
        /** 自动延时隐藏 */
        /*mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (AUTO_HIDE) {
                    autoDelayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
                return false;
            }
        });*/
//        mMapView.setMapOnLongClickListener(new MapLongClickListener() {
//            @Override
//            public void onLongClick(float downX, float downY) {
////                mMapView.indexPointToCenter(downX, downY);
//            }
//        });
//
//        mMapView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        mTitleBackImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMPContainerView.setOnPlayClickListener(new MediaContainer.OnPlayClickListener() {
            @Override
            public void onPlayClick() {
                if (mediaControl.isPlaying()) {
                    mediaControl.pause(true);
                } else {
                    mediaControl.resume();
                }
            }
        });

        mMPContainerView.setSeekChangedListener(new SeekBar.OnSeekBarChangeListener() {
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

    private Map<Integer, MapPointView> initPointView(MConfig mConfig) {
        dataSpotList = mConfig.getScenicSpotList();
        if (dataSpotViewList!=null && dataSpotViewList.size()>0) {
            dataSpotViewList.clear();
        }
        dataSpotViewList = new HashMap<>();

        MapPointView pointView;
        if (dataSpotList != null) {
            boolean isPlaying = mediaControl.isPlaying();
            for (final MScenicSpot spot : dataSpotList) {
                int type = MapPointView.TRAVEL_MUSICE_BLUE;
                if (isPlaying && mediaControl.isPlaying(spot)) {
                    if (spot.isNearest) {
                        type = MapPointView.TRAVEL_MUSICE_PLAY_ORIG;
                    } else {
                        type = MapPointView.TRAVEL_MUSICE_PLAY_BLUE;
                    }
                } else if (spot.isNearest) {
                    type = MapPointView.TRAVEL_MUSICE_ORIG;
                }
//                ImageView pointView = new ImageView(this);
//                pointView.setTag(spot);
                /*pointView = MapPointView.Builder.build(this, xy[0] * mMapView.getScale() - viewWidth,
                        xy[1] * mMapView.getScale() - viewHeight, type, false, spot.getName());*/

                pointView = MapPointView.Builder.build(this, spot.getLatitude(), spot.getLongitude(), type, false, spot.getName());
                pointView.setTag(spot);
                pointView.setObject(spot);
                tileView.addMarker( pointView, spot.getLatitude(), spot.getLongitude(), null, null );
                dataSpotViewList.put(spot.getId(), pointView);
            }
            tileView.getMarkerLayout().setMarkerTapListener( new MarkerLayout.MarkerTapListener() {

                @Override
                public void onMarkerTap(View view, int x, int y) {
                    Object tag = view.getTag();
                    if (tag != null && tag instanceof MScenicSpot) {
                        MScenicSpot spot = (MScenicSpot)tag;
                        if (mediaControl.isPlaying(spot)) {
                            mediaControl.stop();
                        }
                        mediaControl.play(spot);
//                        initPointView(GlobManager.get().getMConfig());
                    }
                }
            });
        }
// test higher than 1
        tileView.setScaleLimits( 0, 2 );

        // start small and allow zoom
        tileView.setScale( 0.5f );

        // with padding, we might be fast enough to create the illusion of a seamless image
        tileView.setViewportPadding( 256 );

        // we're running from assets, should be fairly fast decodes, go ahead and render asap
        tileView.setShouldRenderWhilePanning( true );
//        mMapView.setPointMaps(dataSpotViewList);
        return dataSpotViewList;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        autoDelayedHide(100);
    }

    @Override protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
        mHideTopAnim.cancel();
        mHideTopAnim.reset();
        mHideTopAnim = null;

        mHideBottomAnim.cancel();
        mHideBottomAnim.reset();
        mHideBottomAnim = null;

        mShowTopAnim.cancel();
        mShowTopAnim.reset();
        mShowTopAnim = null;

        mShowBottomAnim.cancel();
        mShowBottomAnim.reset();
        mShowBottomAnim = null;

        locationControl.unregisterLocationListener(locationVrListener);
        locationControl = null;
        mediaControl.unregisterListener(mediaPlayListener);
        mediaControl = null;

//        mMapView.release();
        tileView.destroy();
        tileView = null;

        mMPContainerView.release();
        ViewUtils.releaseBackgroundDrawable(mTitleLayout);
        mMPContainerView = null;

        mTitleLayout.removeAllViews();
        mTitleLayout.removeCallbacks(null);
        ViewUtils.releaseBackgroundDrawable(mTitleLayout);
        mTitleLayout = null;

        mContentView.removeAllViews();
        mContentView.removeCallbacks(null);
        ViewUtils.releaseBackgroundDrawable(mContentView);
        mContentView = null;

        mTitleBackImgView.removeCallbacks(null);
        ViewUtils.releaseBackgroundDrawable(mTitleBackImgView);

        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_INTELLIGENTSSTATE,
                OtherUtils.packUnityJsonData(DataLoader.classToJson(new UIntelligentState(false))).toString());
        System.gc();
    }

    long lastClickMILLS = 0;
    private void toggle() {
        if (superViewVisible) {
            //hide();
        } else {
            show();
        }
        lastClickMILLS = System.currentTimeMillis();
    }

    private void show() {
        superViewVisible = true;
        handler.removeCallbacks(mHideRunnable);
        handler.postDelayed(mShowRunnable, UI_ANIMATION_DELAY);
    }

    private void hide() {
        superViewVisible = false;
        handler.removeCallbacks(mShowRunnable);
        handler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            mTitleLayout.setVisibility(View.VISIBLE);
            mTitleLayout.startAnimation(mShowTopAnim);
            mMPContainerView.setVisibility(View.VISIBLE);
            mMPContainerView.requestFocus();
            mMPContainerView.startAnimation(mShowBottomAnim);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mTitleLayout.setVisibility(View.GONE);
            mTitleLayout.startAnimation(mHideTopAnim);
            mMPContainerView.setVisibility(View.GONE);
            mMPContainerView.startAnimation(mHideBottomAnim);
        }
    };

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void autoDelayedHide(int delayMillis) {
        handler.postDelayed(mHideRunnable, delayMillis);
    }

    private MScenicSpot findSpotByID(String id) {
        if (dataSpotList == null || dataSpotList.isEmpty() || TextUtils.isEmpty(id) || !TextUtils.isDigitsOnly(id)) {
            return null;
        }
        Integer ID = Integer.parseInt(id);
        for (MScenicSpot spot : dataSpotList) {
            int sid = spot.getId();
            if (sid == ID) {
                return spot;
            }
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        tileView.pause();
    }

    /**
     * This is a convenience method to scrollToAndCenter after layout (which won't happen if called directly in onCreate
     * see https://github.com/moagrius/TileView/wiki/FAQ
     */
    public void frameTo( final double x, final double y ) {
        tileView.post( new Runnable() {
            @Override
            public void run() {
                tileView.scrollToAndCenter( x, y );
            }
        });
    }

}
