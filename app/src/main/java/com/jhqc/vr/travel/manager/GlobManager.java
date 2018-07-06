package com.jhqc.vr.travel.manager;

import android.location.Location;

import com.jhqc.vr.travel.model.MConfig;
import com.jhqc.vr.travel.model.MScenicSpot;

/**
 * Created by Solomon on 2017/10/19 0019.
 */

public class GlobManager {

    public static boolean isRelease = false;

    static byte[] lock = new byte[1];

    static GlobManager INSTANCE;

    private MConfig CURRENT_MCONFIG;

    private MScenicSpot CURRENT_SPOT;

    private Location curLocation;

    private GlobManager() {}

    public static GlobManager get() {
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    INSTANCE = new GlobManager();
                }
            }
        }
        return INSTANCE;
    }

    public MConfig getMConfig() {
        return CURRENT_MCONFIG;
    }

    public GlobManager setMConfig(MConfig mConfig) {
        this.CURRENT_MCONFIG = mConfig;
        return this;
    }

    public MScenicSpot getCurSpot() {
        return CURRENT_SPOT;
    }

    public GlobManager setCurSpot(MScenicSpot mCurSpot) {
        this.CURRENT_SPOT = mCurSpot;
        return this;
    }

    public Location getCurLocation() {
        return curLocation;
    }

    public GlobManager setCurLocation(Location curLocation) {
        this.curLocation = curLocation;
        return this;
    }
}
