package com.jhqc.vr.travel.location;

import android.location.Location;

/**
 * Created by Solomon on 2017/10/20 0020.
 */

public interface LocationVRListener<T> {

    public void onReceive(Location location);

    public void onFailed(int errorCode, String msg);

    public void onAutoPlay(T t);

}
