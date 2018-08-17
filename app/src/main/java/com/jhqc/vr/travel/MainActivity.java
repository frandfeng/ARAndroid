package com.jhqc.vr.travel;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.app.AppDelegate;
import com.jhqc.vr.travel.control.LocationControl;
import com.jhqc.vr.travel.control.MediaControl;
import com.jhqc.vr.travel.control.VrFactory;
import com.jhqc.vr.travel.location.LocationVRListener;
import com.jhqc.vr.travel.manager.ConfigManager;
import com.jhqc.vr.travel.manager.GlobManager;
import com.jhqc.vr.travel.manager.LocationsManager;
import com.jhqc.vr.travel.manager.VrActivityManager;
import com.jhqc.vr.travel.model.MConfig;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.unity.IUnity;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.unity.UnityConstants;
import com.jhqc.vr.travel.util.ActivityUtils;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.OtherUtils;
import com.jhqc.vr.travel.util.SPUtils;
import com.jhqc.vr.travel.util.ToastUtils;
import com.jhqc.vr.travel.util.ViewUtils;
import com.jhqc.vr.travel.weight.DragFloatActionButton;
import com.jhqc.vr.travel.weight.VrUnityPlayer;
import com.vuforia.CameraDevice;
import com.vuforia.Vuforia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements IUnity {

    private static final String MARK_HINT = "ASK_LOCATION";

    static VrUnityPlayer mUnityPlayer;

    ObjectAnimator roteAnim;

    DragFloatActionButton fab;
    ViewGroup root;
    ViewGroup view;

    MediaControl mediaControl;
    LocationControl locationControl;
    Dialog hintDialog;

    ArrayList<MConfig> datas;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 针对线程的相关策略
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .penaltyDialog()
                .build());

        // 针对VM的相关策略
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());*/

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawableResource(R.drawable.dialog_normal_black_bg);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBX_8888);
        setContentView(R.layout.activity_main2);

        if (GlobManager.isRelease && !hasPermrisson()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("提示");
            alertDialog.setMessage("对不起，您的设备没有授权，请联系发布方授权!");
            alertDialog.setCancelable(false);
            alertDialog.setButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                    //退出程序
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            });
            alertDialog.show();
        } else {
            if (mUnityPlayer == null) {
                mUnityPlayer = new VrUnityPlayer(this);
            }
            root = (ViewGroup) findViewById(R.id.root_layout);

            if (mUnityPlayer != null) {
                root.addView(mUnityPlayer, 0);
                mUnityPlayer.requestFocus();
                //TODO
                mUnityPlayer.resume();
            }

            initData();
            initControl();
            initControlBar();
            UnityBridgeHandler.currentActivity.registerUnity(this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cutImageAndSave();
                }
            }).start();


            /*GDLocationHandler handler = new GDLocationHandler(this);
            handler.start();*/

            /** 监听来电事件 */
            TelephonyManager phoneyMana = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            phoneyMana.listen(new VrPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private boolean hasPermrisson() {
        String SerialNumber = android.os.Build.SERIAL;
        String[] str = getResources().getStringArray(R.array.devices_serial);
        ArrayList list = new ArrayList(str.length);
        Collections.addAll(list, str);
        return list.contains(SerialNumber);
    }

    //用来控制应用前后台切换的逻辑
    private boolean isCurrentRunningForeground = true;
    @Override
    protected void onStart() {
        super.onStart();
        //TODO 1
        /*if (!isCurrentRunningForeground) {
            if (mUnityPlayer != null) {
                mUnityPlayer.resume();
            }
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        isCurrentRunningForeground = ActivityUtils.isRunningForeground(this);
        //TODO 1
        /*if (!isCurrentRunningForeground) {
            if (mUnityPlayer != null) {
                mUnityPlayer.pause();
            }
        }*/
        if (mUnityPlayer != null) {
            //TODO
//            mUnityPlayer.pause();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mUnityPlayer != null && root != null) {
            root.removeView(mUnityPlayer);
            mUnityPlayer.requestFocus();
        }
        if (mUnityPlayer != null) {
            root.addView(mUnityPlayer, 0);
            //TODO
            mUnityPlayer.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (mUnityPlayer != null) {
            mUnityPlayer.pause();
        }*/
        CameraDevice.getInstance().stop();
        CameraDevice.getInstance().deinit();
        Vuforia.onPause();

        isPaused = true;
    }

    boolean isPaused;
    @Override
    protected void onResume() {
        super.onResume();
        /*if (mUnityPlayer != null) {
            mUnityPlayer.resume();
        }*/
        if (isPaused) {
            CameraDevice.getInstance().start();
            Vuforia.onResume();
        }
        isPaused = false;
    }

    /*Dialog dialog;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void playVedio(int resID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View vedioLayout = inflater.inflate(R.layout.video_player_layout, null);
        final VideoView videoView = (VideoView) vedioLayout.findViewById(R.id.vedio_player);
        final View loadingView = vedioLayout.findViewById(R.id.loading);
        ImageButton backView = (ImageButton) vedioLayout.findViewById(R.id.vedio_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View iView) {
                postPlayEnd();
                videoView.stopPlayback();
            }
        });
        MediaController mc = new MediaController(this);
        mc.setMediaPlayer(videoView);
        mc.setVisibility(View.VISIBLE);
        videoView.setMediaController(mc);

        Uri uri =  Uri.parse("android.resource://" + getPackageName() + "/"+ resID);
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
//                loadingView.setVisibility(View.INVISIBLE);
            }
        });
//        loadingView.setVisibility(View.VISIBLE);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.show(MainActivity.this, "点击事件");
            }
        });
        videoView.setFocusable(true);
        videoView.requestFocus();
        videoView.start();

        dialog = new Dialog(this, R.style.Dialog_Fullscreen);
        dialog.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉dialog的标题
        dialog.setContentView(vedioLayout);
        dialog.show();
    }

    private void postPlayEnd() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog.cancel();
        }
        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_ONPLAYVIDEOEND, OtherUtils.packUnityJsonData("{\"errCode\":0}").toString());
    }*/

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                datas =  ConfigManager.get(AppDelegate.getApplication().getBaseContext()).getConfig();
                if (datas != null && !datas.isEmpty()) {
                    GlobManager.get().setMConfig(datas.get(0));
                    VrFactory.get().getLocationControl().initData(GlobManager.get().getMConfig().getScenicSpotList());
                }
            }
        }).start();
    }

    private void initControl() {
        mediaControl = VrFactory.get().getMediaControl();
        locationControl = VrFactory.get().getLocationControl();

        if (!SPUtils.getBoolean(this, MARK_HINT)) {
            SPUtils.saveBoolean(this, MARK_HINT, true);
            hintDialog = new Dialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.loc_hint_layout, null);
            ImageButton iBtn = (ImageButton) view.findViewById(R.id.btn);

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewUtils.dipToPixel(500, this), ViewGroup.LayoutParams.MATCH_PARENT);
            hintDialog.addContentView(view, lp);
            hintDialog.setCancelable(false);
            hintDialog.setCanceledOnTouchOutside(false);
            iBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hintDialog.dismiss();
                    hintDialog.cancel();
                    startLocation();
                }
            });
            hintDialog.show();
        } else {
            startLocation();
        }

        mediaControl.registerListener(new MediaControl.MediaPlayListener<MScenicSpot>() {
            @Override
            public void onStart(MediaPlayer mp, MScenicSpot scenicSpot) {
                startRoteBar();
                if (scenicSpot != null) {
                    updateIcon(scenicSpot);
                    scenicSpot.isPlayed = true;

                    PowerManager pm= (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if(!pm.isScreenOn()) {
                        //熄屏状态
                        VrActivityManager.startLockScreenActivity(MainActivity.this);
                    }
                }
            }

            @Override
            public void onResume(MediaPlayer mp, MScenicSpot scenicSpot) {
                roteAnim.resume();
            }

            @Override
            public void onPause(MediaPlayer mp, MScenicSpot scenicSpot) {
                roteAnim.pause();
            }

            @Override
            public void seekTo(MediaPlayer mp, MScenicSpot scenicSpot) {
            }

            @Override
            public void onStop(MediaPlayer mp, MScenicSpot scenicSpot) {
                endRoteBar();
            }

            @Override
            public void onComplate(MediaPlayer mp, MScenicSpot scenicSpot) {
                endRoteBar();
            }

            @Override
            public void onError(MediaPlayer mp, MScenicSpot scenicSpot, String error) {
                endRoteBar();
            }

            @Override
            public void onProccess(MediaPlayer mp, MScenicSpot scenicSpot) {
            }
        });

        locationControl.registerLocationListener(new LocationVRListener<MScenicSpot>() {
            @Override
            public void onReceive(Location location) {
            }

            @Override
            public void onFailed(int errorCode, String msg) {
            }

            @Override
            public void onAutoPlay(final MScenicSpot scenicSpot) {
                /*if (scenicSpot != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fab.postEventionText(scenicSpot.getName());
                        }
                    });
                }*/
            }
        });
    }

    RelativeLayout.LayoutParams lps;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initControlBar() {
        fab = new DragFloatActionButton(MainActivity.this);
        lps = new RelativeLayout.LayoutParams(ViewUtils.dipToPixel(85, this), ViewUtils.dipToPixel(85, this));
        lps.alignWithParent = true;
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        fab.setLayoutParams(lps);
        fab.setPadding(ViewUtils.dipToPixel(5, this), ViewUtils.dipToPixel(5, this), ViewUtils.dipToPixel(5, this), ViewUtils.dipToPixel(5, this));
        fab.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        view = (ViewGroup) findViewById(R.id.layout);
        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewUtils.releaseBackgroundDrawable(view);
                view.setBackgroundColor(getResources().getColor(R.color.translate));
                root.addView(fab, lps);
            }
        }, 20000);*/

        fab.setOnClickListener(new View.OnClickListener() {
            byte[] lock = new byte[1];
            long lastClickMills = 0;
            @Override
            public void onClick(View view) {
                if (!fab.isClick()) {
                    return;
                }
                //Snackbar.make(view, "action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                synchronized (lock) {
                    LogUtils.logError("点击响应了");
                    long currentMills = System.currentTimeMillis();
                    if (currentMills - lastClickMills < 1000) {
                        LogUtils.logError("上次点击小于1秒，不处理");
                        return;
                    }
                    lastClickMills = currentMills;

                    if (GlobManager.get().getMConfig() != null) {
                        ActivityUtils.startActivity(MainActivity.this, TravelActivity.class);
                    }
                }
            }
        });
    }

    private void startLocation() {
        if (LocationsManager.isOpenGPS(this)) {
            locationControl.startAutoGPS();
        } else {
            boolean openSucessd = LocationsManager.openGPS(this);
            if (!openSucessd || (openSucessd && !LocationsManager.isOpenGPS(this))) {
                LocationsManager.requestLocationPerrissmen(this);
            } else {
                locationControl.startAutoGPS();
            }
        }
    }

    private void startRoteBar() {
        if (roteAnim == null) {
            LinearInterpolator lin = new LinearInterpolator();
            roteAnim = ObjectAnimator.ofFloat(fab.getImageView(), "rotation", 0.0F, 359.0F);
            roteAnim.setRepeatCount(-1);
            roteAnim.setDuration(5000);
            roteAnim.setInterpolator(lin);
        }
        roteAnim.start();
    }

    private void updateIcon(final MScenicSpot spot) {
        if (spot != null) {
            Bitmap bitmap = ConfigManager.get(MainActivity.this.getBaseContext()).getIconBitmapFile(spot.getIconFileName(), 8);
            final BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.setImageDrawable(bitmapDrawable);
                    fab.postEventionText(spot.getName());
                }
            });
        }
    }

    private void endRoteBar() {
        if (roteAnim != null) {
            roteAnim.end();
            roteAnim.cancel();
        }
        fab.postStopEvention();
    }

    Animation animation;
    @Override
    public void onSwitchBar(boolean isShowBar, boolean isAnimate) {
        if (fab != null && lps != null && root != null) {
            if (isShowBar) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fab.getParent() == null) {
                            if (animation == null) {
                                animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_left);
                            }
                            root.addView(fab, lps);
                            fab.setAnimation(animation);
                            animation.start();
                            fab.bringToFront();
                        }
                        fab.initScreenWH();
                    }
                }, 1000);
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fab.getParent() != null) {
                            root.removeView(fab);
                        }
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onReady(boolean isReady) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewUtils.releaseBackgroundDrawable(view);
                view.setBackgroundColor(getResources().getColor(R.color.translate));
            }
        });
    }

    @Override
    public void onPlayVideo(String videoName) {
        if (!TextUtils.isEmpty(videoName)) {
            //videoName = videoName.substring(0, videoName.indexOf("."));
//            int resId = getResources().getIdentifier(videoName, "raw", this.getPackageName());
            if (videoName != "") {
//                playVedio(resId);
                Intent intent = new Intent(this, PlayVideoActivity.class);
                intent.putExtra(PlayVideoActivity.RESNAME_TAG, videoName);
                startActivity(intent);
                //overridePendingTransition(R.anim.activity_scale_in, R.anim.activity_scale_out);
            } else {
                UnityBridgeHandler.postUnityPlayVideoEnd();
                LogUtils.logVedio("找不到资源：" + videoName);
            }
        }
    }

    @Override
    public void onARScan() {
        ActivityUtils.startActivity(this, ImageTargets.class);
    }

    /*来电事件处理*/
    private class VrPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING://来电，应当停止音乐
                    if(mediaControl.isPlaying()){
                        mediaControl.pause(false);//暂停
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE://无电话状态
                    if (mediaControl.isFightPlayerAutoPaused()) {
                        mediaControl.resume();
                    }
                    break;
            }
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override protected void onDestroy () {
        try {
            if (mUnityPlayer != null) {
                mUnityPlayer.quit();
            }
        } catch (Exception e) {
        }

        super.onDestroy();
        handler.removeCallbacks(null);
        if (hintDialog != null) {
            hintDialog.cancel();
            hintDialog = null;
        }

        TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(null, 0);
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (mUnityPlayer != null) {
            mUnityPlayer.lowMemory();
        }
    }

    @Override public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            if (mUnityPlayer != null) {
                mUnityPlayer.lowMemory();
            }
        }
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mUnityPlayer != null) {
            mUnityPlayer.configurationChanged(newConfig);
        }
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mUnityPlayer != null) {
            mUnityPlayer.windowFocusChanged(hasFocus);
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            if (mUnityPlayer != null) {
                return mUnityPlayer.injectEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     {
        if (mUnityPlayer != null) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   {
        if (mUnityPlayer != null) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override public boolean onTouchEvent(MotionEvent event)          {
        if (mUnityPlayer != null) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onTouchEvent(event);
    }
    /*API12*/@Override public boolean onGenericMotionEvent(MotionEvent event)  {
        if (mUnityPlayer != null) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && grantResults.length != 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 授权成功
                if (LocationsManager.isOpenGPS(this)) {
                    locationControl.startAutoGPS();
                } else {
                    Toast.makeText(this, "获取定位失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 授权失败
                Toast.makeText(this, "获取定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void cutImageAndSave() {
        datas =  ConfigManager.get(AppDelegate.getApplication().getBaseContext()).getConfig();
        if (datas != null && !datas.isEmpty()) {
            GlobManager.get().setMConfig(datas.get(0));
            VrFactory.get().getLocationControl().initData(GlobManager.get().getMConfig().getScenicSpotList());
        }
        for (int i=1; i<2; i++) {
            int scale = 2 << i;
            if (i==-1) {
                scale = 1;
            }

//            String fileName = GlobManager.get().getMConfig().getMapFileName();
//            Bitmap bitmap = ConfigManager.get(getBaseContext()).getMapBitmapFile(fileName, 4/scale, (int) SCREEN_WIDTH);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.zhinengdaoyouditu);

            InputStream is = this.getResources().openRawResource(R.raw.zhinengdaoyouditu);
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap =BitmapFactory.decodeStream(is,null,options);

            final int WH = 256;
            // 获得要切割的图片的宽高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // 设置想要的大小
//            float newWidth = width/scale;
//            float newHeight = height/scale;
            // 取得想要缩放的matrix参数
//            Matrix matrix = new Matrix();
//            matrix.postScale(scale, scale);
            // 得到新的图片
//            Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, (int)width, (int)height, matrix, true);

            int rows = (int)Math.ceil((float)height / WH);
            int cols = (int)Math.ceil((float)width / WH);
            Log.i("frand", "cut images scale:"+scale+", rows:"+rows+", cols:"+cols);
            for (int y=0; y<rows; ++y) {
                for (int x=0; x<cols; ++x) {
                    // &#x5207;&#x7247;Bitmap&#x5bf9;&#x5e94;&#x7684;x&#xff0c;y&#x5750;&#x6807;&#xff0c;x&#x7531;&#x5217;&#x51b3;&#x5b9a;&#xff0c;y&#x5219;&#x884c;&#x51b3;&#x5b9a;
                    int xPoint = x * WH;
                    int yPoint = y * WH;
                    int bitWidth = WH;
                    int bitHeight = WH;
                    if (x == cols-1) {
                        bitWidth = width % 256 == 0 ? WH : width % WH;
                    }
                    if (y == rows-1) {
                        bitHeight = height % 256 == 0 ? WH : height % WH;
                    }
                    Log.i("frand", "width :"+width+",height="+height+",xPoint="+xPoint+",yPoint="+yPoint+",WH="+WH);
                    Bitmap sliceBitmap = Bitmap.createBitmap(bitmap, xPoint, yPoint, bitWidth, bitHeight);
                    String path = String.format("yiheyuan_%dx_%d_%d.png", scale, x, y);
                    saveImage(sliceBitmap, path);
                    sliceBitmap.recycle();
                    sliceBitmap = null;
                }
            }
            Log.i("frand", "save finish");
        }
    }

    private void saveImage(Bitmap bitmap, String path) {
        //将bitmap保存为本地文件
        File PHOTO_DIR = new File(Environment.getExternalStorageDirectory(), "yiheyuan");//设置保存路径
        if (PHOTO_DIR.exists()) {
            PHOTO_DIR.delete();
        }
        PHOTO_DIR.mkdir();
        File avaterFile = new File(PHOTO_DIR, path);//设置文件名称
        if(avaterFile.exists()) {
            avaterFile.delete();
        }
        try {
            Log.i("frand", "try save path: "+avaterFile.getPath());
            avaterFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(avaterFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.i("frand", "save path: "+avaterFile.getPath());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("frand", "exception path: "+e.getMessage());
        }
    }
}
