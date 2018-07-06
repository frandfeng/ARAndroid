package com.jhqc.vr.travel.unity;

/**
 * Created by Solomon on 2017/11/17 0017.
 */

public interface IUnity {

    void onSwitchBar(boolean isShowBar, boolean isAnimate);

    void onReady(boolean isReady);

    void onPlayVideo(String videoName);

    void onARScan();

}
