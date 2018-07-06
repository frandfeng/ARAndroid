/*===============================================================================
Copyright (c) 2016-2017 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.jhqc.vr.travel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jhqc.AR_3D.R;
import com.jhqc.vr.travel.ar.ImageTargetRenderer;
import com.jhqc.vr.travel.ar.SampleApplication.SampleApplicationControl;
import com.jhqc.vr.travel.ar.SampleApplication.SampleApplicationException;
import com.jhqc.vr.travel.ar.SampleApplication.SampleApplicationSession;
import com.jhqc.vr.travel.ar.SampleApplication.utils.LoadingDialogHandler;
import com.jhqc.vr.travel.ar.SampleApplication.utils.SampleApplicationGLView;
import com.jhqc.vr.travel.ar.SampleApplication.utils.Texture;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.weight.AnimationsContainer;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.Vector;


public class ImageTargets extends Activity  implements SampleApplicationControl {
    private static final String LOGTAG = "ImageTargets";

    static SampleApplicationSession vuforiaAppSession;

    private  Vector<DataSet> mDataSetList = new Vector<>();
    private  ArrayList<String> mDataFileList = new ArrayList<>();

    // Our OpenGL view:
    private  SampleApplicationGLView mGlView;

    // Our renderer:
    private ImageTargetRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private boolean mSwitchDatasetAsap = false;
    private boolean mContAutofocus = true;
    private boolean mExtendedTracking = false;

    private RelativeLayout mUILayout;

    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.scan_layout);

        if (vuforiaAppSession == null) {
            vuforiaAppSession = new SampleApplicationSession(this);
        }

        startLoadingAnimation();
        if (mDataFileList.size() == 0) {
            mDataFileList.add("AR_Card.xml");
            mDataFileList.add("AR_Sand.xml");
            mDataFileList.add("AR_Ticket.xml");
        }

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loadTextures();
    }

    private void loadTextures() {
        mTextures = new Vector<>();
        mTextures.add(Texture.loadTextureFromApk("Buildings.jpeg", getAssets()));
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        showProgressIndicator(true);

        // This is needed for some Droid devices to force portrait
//        if (mIsDroidDevice) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

        if (vuforiaAppSession != null)
            vuforiaAppSession.onResume();
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        if (vuforiaAppSession != null)
            vuforiaAppSession.onConfigurationChanged();
    }

    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try {
            if (vuforiaAppSession != null)
                vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            if (vuforiaAppSession != null)
                vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        if (mErrorDialog != null) {
            mErrorDialog.cancel();
            mErrorDialog = null;
        }
        if (mGlView != null) {
            mGlView.removeCallbacks(null);
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;
        System.gc();
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        if (mRenderer ==null ) {
            mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
            if (mRenderer != null)
                mRenderer.setTextures(mTextures);
        }

        if (mGlView == null) {
            mGlView = new SampleApplicationGLView(this);

            if (mGlView != null) {
                mGlView.init(translucent, depthSize, stencilSize);
                mGlView.setRenderer(mRenderer);
            }
        }
    }

    private void startLoadingAnimation() {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        final ObjectTracker objectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                DataSet dataSet;
                if (mDataSetList.size() == 0)
                    for (int index = 0; index < mDataFileList.size(); index++) {
                        dataSet = objectTracker.createDataSet();

                        if (!dataSet.load(mDataFileList.get(index), STORAGE_TYPE.STORAGE_APPRESOURCE))
                            continue;
                        if (!objectTracker.activateDataSet(dataSet))
                            continue;

                        int numTrackables = dataSet.getNumTrackables();
                        for (int count = 0; count < numTrackables; count++) {
                            Trackable trackable = dataSet.getTrackable(count);
                            if (isExtendedTrackingActive()) {
                                trackable.startExtendedTracking();
                            }
                            trackable.setUserData(trackable.getName());
                            Log.d(LOGTAG, "注册图片对象： " + trackable.getUserData());
                        }
                        mDataSetList.add(dataSet);
                    }
            }
        }).start();

        return true;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        for (DataSet dataSet : mDataSetList) {
            if (dataSet != null && dataSet.isActive()) {
                if (objectTracker.getActiveDataSet(0).equals(dataSet) && !objectTracker.deactivateDataSet(dataSet)) {
                    result = false;
                } else if (!objectTracker.destroyDataSet(dataSet)) {
                    result = false;
                }
            }
        }
        return result;
    }

    @Override
    public void onVuforiaResumed() {
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted() {
        if (mRenderer != null)
            mRenderer.updateConfiguration();

        if (mContAutofocus) {
            // Set camera focus mode
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
                // If continuous autofocus mode fails, attempt to set to a different mode
                if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
                    CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
                }
            }
        }

        showProgressIndicator(false);
    }

    public void showProgressIndicator(boolean show) {
        if (loadingDialogHandler != null) {
            if (show) {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            } else {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            if (mRenderer != null)
                mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
//            LayoutInflater inflater = LayoutInflater.from(ImageTargets.this);
//            View view = inflater.inflate(R.layout.scan_layout, null);
            FrameLayout glGroup = (FrameLayout) findViewById(R.id.scan_gl_layout);
            View flagView = findViewById(R.id.flag_view);
            if (mGlView != null) {
                glGroup.addView(mGlView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            ImageView imageView = (ImageView) findViewById(R.id.img_gl_view);

            AnimationsContainer.FramesSequenceAnimation animation = AnimationsContainer.getInstance(this.getApplicationContext(), R.array.conner_anim, 8).createProgressDialogAnim(imageView);
            animation.start();

//            addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            if (vuforiaAppSession != null)
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
        } else {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ImageTargets.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle("错误")
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap) {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mDataSetList == null
                    || ot.getActiveDataSet(0) == null) {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    public boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }

    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;

    String text;
    //TextView textView;
    public void updateResult(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if (textView == null) {
                    textView = new TextView(ImageTargets.this);
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(layoutParams);
                    textView.setTextColor(Color.RED);
                    addContentView(textView, layoutParams);
                    textView.bringToFront();
                }
                if (str != null && !str.equals(text)) {
                    textView.setText(str);
                    text = str;
                    showToast(text);
                }*/
                if (!TextUtils.isEmpty(str)) {
                    UnityBridgeHandler.postUnityARScan(str, true);
                    finish();
                }
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
