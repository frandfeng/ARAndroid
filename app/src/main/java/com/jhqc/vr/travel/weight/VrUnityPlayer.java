package com.jhqc.vr.travel.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.unity3d.player.UnityPlayer;

/**
 * Created by Solomon on 2017/10/30 0030.
 */

public class VrUnityPlayer extends UnityPlayer {

    public VrUnityPlayer(ContextWrapper context) {
        super(context);
    }

    @Override
    public void addView(View child) {
        if (child instanceof SurfaceView) {
            ((SurfaceView)child).setZOrderOnTop(false);
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (child instanceof SurfaceView) {
            ((SurfaceView)child).setZOrderOnTop(false);
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child instanceof SurfaceView) {
            ((SurfaceView)child).setZOrderOnTop(false);
        }
        super.addView(child, params);
    }
}
