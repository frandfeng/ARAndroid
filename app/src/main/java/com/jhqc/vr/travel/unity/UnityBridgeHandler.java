package com.jhqc.vr.travel.unity;

import android.location.Location;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.jhqc.vr.travel.app.AppDelegate;
import com.jhqc.vr.travel.control.MediaControl;
import com.jhqc.vr.travel.control.VrFactory;
import com.jhqc.vr.travel.location.LocationVRListener;
import com.jhqc.vr.travel.manager.DataLoader;
import com.jhqc.vr.travel.manager.GlobManager;
import com.jhqc.vr.travel.manager.LocationsManager;
import com.jhqc.vr.travel.unity.model.UARScan;
import com.jhqc.vr.travel.unity.model.UBModel;
import com.jhqc.vr.travel.unity.model.UCall;
import com.jhqc.vr.travel.unity.model.UGpsLocationInfo;
import com.jhqc.vr.travel.unity.model.UGpsState;
import com.jhqc.vr.travel.unity.model.UIntelligentState;
import com.jhqc.vr.travel.unity.model.ULog;
import com.jhqc.vr.travel.unity.model.UPlayBotton;
import com.jhqc.vr.travel.unity.model.UPlayContinue;
import com.jhqc.vr.travel.unity.model.UPlayVideo;
import com.jhqc.vr.travel.unity.model.UShutDownVideo;
import com.jhqc.vr.travel.unity.model.UTypeToken;
import com.jhqc.vr.travel.util.LogUtils;
import com.jhqc.vr.travel.util.OtherUtils;
import com.unity3d.player.UnityPlayer;

import java.lang.reflect.Type;

/**
 * Created by Solomon on 2017/10/29 0029.
 */

public class UnityBridgeHandler {

    LocationHandler handler;

    IUnity iUnity;

    public static UnityBridgeHandler currentActivity = new UnityBridgeHandler();

    public void handle(String methd, String json) {
        LogUtils.logUnity("handle()..methd = "+ methd +"  json=" + json);
        if (!TextUtils.isEmpty(methd)) {
            Type type;
            UBModel ubModel;
            switch (methd) {
                case UnityConstants.N_REQGPSSTATE:
                    boolean isOpenGPS = LocationsManager.isOpenGPS(AppDelegate.getApplication().getBaseContext());

                    type = new TypeToken<UBModel<UGpsState>>(){}.getType();
                    ubModel = DataLoader.loadDataByJson(json, type);
                    if (ubModel != null) {
                        UGpsState gpsState = new UGpsState();
                        gpsState.state = isOpenGPS ? UnityConstants.GPSSTATE_YES : UnityConstants.GPSSTATE_NONE;
                        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_GPSSTATE,
                                OtherUtils.packUnityJsonData(DataLoader.classToJson(gpsState)).toString());
                    }
                    break;
                case UnityConstants.N_REQGPSINFO:
                    isOpenGPS = LocationsManager.isOpenGPS(AppDelegate.getApplication().getBaseContext());
                    if (isOpenGPS) {
                        if (handler == null) {
                            handler = new LocationHandler();
                        }
                        handler.postLocation();
                    }
                    break;
                case UnityConstants.N_REQCALL:
                    type = new TypeToken<UBModel<UCall>>(){}.getType();
                    ubModel = DataLoader.loadDataByJson(json, type);
                    if (ubModel != null && ubModel.params != null) {
                        OtherUtils.call(AppDelegate.getApplication().getBaseContext(), ((UCall)ubModel.params).phoneNum);
                    }
                    break;
                case UnityConstants.N_REQLOG:
                    type = new TypeToken<UBModel<ULog>>(){}.getType();
                    ubModel = DataLoader.loadDataByJson(json, type);
                    if (ubModel != null && ubModel.params != null) {
                        LogUtils.logLoc(((ULog)ubModel.params).logString);
                    }
                    break;
                case UnityConstants.N_REQPLAYMUSIC:
                    type = new TypeToken<UBModel<UPlayContinue>>(){}.getType();
                    ubModel = DataLoader.loadDataByJson(json, type);
                    if (ubModel != null && ubModel.params != null) {
                        boolean isPlay = ((UPlayContinue)ubModel.params).play;
                        MediaControl mediaControl = VrFactory.get().getMediaControl();
                        if (isPlay && GlobManager.get().getCurSpot() != null) {
                            LogUtils.logUnity(isPlay + " = 继续播放");
                            if (mediaControl.isFightPlayerAutoPaused()) {
                                mediaControl.resume();
                            }
                        } else if (mediaControl.isPlaying()){
                            LogUtils.logUnity(isPlay + " = 暂停播放");
                            mediaControl.pause(false);
                        }
                    }
                    break;
                case UnityConstants.N_REQPLAYBUTTON:
                    type = new TypeToken<UBModel<UPlayBotton>>(){}.getType();
                    ubModel = DataLoader.loadDataByJson(json, type);
                    if (ubModel != null && ubModel.params != null) {
                        boolean isShow = ((UPlayBotton)ubModel.params).appear;
                        boolean isAnim = ((UPlayBotton)ubModel.params).animate;
                        if (iUnity != null) {
                            iUnity.onSwitchBar(isShow, isAnim);
                        }
                    }
                    break;
                case UnityConstants.N_REQREADY:
                    if (iUnity != null) {
                        iUnity.onReady(true);
                    }
                    break;
                case UnityConstants.N_REQPLAYVIDEO:
                    if (iUnity != null) {
                        type = new TypeToken<UBModel<UPlayVideo>>(){}.getType();
                        ubModel = DataLoader.loadDataByJson(json, type);
                        if (ubModel != null && ubModel.params != null) {
                            String videoName = ((UPlayVideo)ubModel.params).videoName;
                            int progress = ((UPlayVideo)ubModel.params).progress;
                            iUnity.onPlayVideo(videoName, progress);
                        }
                    }
                    break;
                case UnityConstants.N_REQARSCAN:
                    if (iUnity != null) {
//                        iUnity.onARScan();
                    }
                    break;
                case UnityConstants.N_SHUTDOWNVIDEO:
                    if (iUnity != null) {
                        type = new TypeToken<UBModel<UShutDownVideo>>(){}.getType();
                        ubModel = DataLoader.loadDataByJson(json, type);
                        if (ubModel != null && ubModel.params != null) {
                            boolean resetPosition = ((UShutDownVideo)ubModel.params).resetposition;
                            iUnity.onShutDownVideo(resetPosition);
                        }
                    }
                    break;
            }
            type = new UTypeToken<UBModel<UCall>>().getType();
            DataLoader.loadDataByJson(json, type);
        }
    }

    public static void sendUnityMessage(String str1, String str2) {
        UnityBridgeHandler.sendUnityMessage(UnityConstants.CLASS_ENTRANCE, str1, str2);
    }

    public static void sendUnityMessage(String str0, String str1, String str2) {
        LogUtils.logUnity("Android 回调 Unity:\n" + str0 + " " + str1 +" " + str2);
        UnityPlayer.UnitySendMessage(str0, str1, str2);
    }

    public static void postUnityPlayVideoEnd() {
        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_ONPLAYVIDEOEND, OtherUtils.packUnityJsonData("{\"errCode\":0}").toString());
    }

    public static void postUnityARScan(String value, boolean isSeccess) {
        UnityBridgeHandler.sendUnityMessage(UnityConstants.U_ONPLAYVIDEOEND, OtherUtils.packUnityJsonData(
                DataLoader.classToJson(new UARScan(isSeccess ? 0 : 1, value))).toString());
    }

    public IUnity getiUnity() {
        return iUnity;
    }

    public UnityBridgeHandler registerUnity(IUnity iUnity) {
        this.iUnity = iUnity;
        return this;
    }

    class LocationHandler {

        public void postLocation() {
            VrFactory.get().getLocationControl().registerLocationListener(locationVRListener);
        }

        LocationVRListener locationVRListener = new LocationVRListener() {
            @Override
            public void onReceive(Location location) {
                if (location == null) {
                    return;
                }
                UGpsLocationInfo info = new UGpsLocationInfo();
                info.latitude = (float) location.getLatitude();
                info.longitude = (float) location.getLongitude();
                UnityBridgeHandler.sendUnityMessage(UnityConstants.U_GPSINFO,
                        OtherUtils.packUnityJsonData(DataLoader.classToJson(info)).toString());
                VrFactory.get().getLocationControl().unregisterLocationListener(locationVRListener);
            }

            @Override
            public void onFailed(int errorCode, String msg) {
            }

            @Override
            public void onAutoPlay(Object o) {
            }
        };
    }

}
