package com.jhqc.vr.travel.control;

import com.jhqc.vr.travel.app.AppDelegate;

/**
 * Created by Solomon on 2017/10/30 0030.
 */

public class VrFactory {

    byte[] lock = new byte[1];

    static VrFactory factory;

    MediaControl mediaControl;

    LocationControl locationControl;

    public static VrFactory get() {
        if (factory == null) {
            factory = new VrFactory();
        }

        return factory;
    }

    public LocationControl getLocationControl() {
        if (locationControl == null) {
            synchronized (lock) {
                if (locationControl == null) {
                    locationControl = new LocationControl(AppDelegate.getApplication().getBaseContext());
                }
            }
        }
        return locationControl;
    }

    public MediaControl getMediaControl() {
        if (mediaControl == null) {
            synchronized (lock) {
                if (mediaControl == null) {
                    mediaControl = new MediaControl(AppDelegate.getApplication().getBaseContext());
                }
            }
        }
        return mediaControl;
    }

}
