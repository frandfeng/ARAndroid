package com.jhqc.vr.travel;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jhqc.vr.travel.manager.VrActivityManager;

/**
 * Created by Solomon on 2017/10/29 0029.
 */

public class BaseActivity extends AppCompatActivity {

    /** 屏幕宽高 */
    public float SCREEN_WIDTH, SCREEN_HEIGHT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
//        VrActivityManager.get().add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        VrActivityManager.get().remove(this);
    }
}
