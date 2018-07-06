package com.jhqc.vr.travel.control;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.jhqc.vr.travel.algorithm.LocationAler;
import com.jhqc.vr.travel.bluetooth.BluetoothHandler;
import com.jhqc.vr.travel.bluetooth.IBeacon;
import com.jhqc.vr.travel.bluetooth.ScannedDevice;
import com.jhqc.vr.travel.location.LocationTimer;
import com.jhqc.vr.travel.location.LocationVRListener;
import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.struct.Entry;
import com.jhqc.vr.travel.util.LoctionConvertUtils;
import com.jhqc.vr.travel.util.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Solomon on 2017/10/19 0019.
 */

public class LocationControl {

    static final int MILLS = 5 * 1000;
    static final int AUTO_MILLS = 2 * 1000;
    static final int DISTANCE = 3;

    //单位km
    static final float MIN_DISTANCE = 10; //地图定位时使用：0.02f;
    static final boolean isAutoPlay = true;

    Context mContext;

    double latitude;

    double longitude;

    LocationTimer timer;

    LocationManager locationManager;

    BluetoothHandler bluetoothHandler;

    ArrayList<LocationVRListener<MScenicSpot>> mLocationVRListeners = new ArrayList<>();

    private ArrayList<MScenicSpot> dataSpotList = new ArrayList<>();

    LocationControl(Context context) {
        this.mContext = context;
    }

    LocationControl(Context context, LocationVRListener<MScenicSpot> locationVRListener) {
        this.mContext = context;
        this.mLocationVRListeners.add(locationVRListener);
    }

    public void initData(ArrayList<MScenicSpot> datas) {
        this.dataSpotList.addAll(datas);
    }

    public void registerLocationListener(LocationVRListener<MScenicSpot> locationVRListener) {
        if (!mLocationVRListeners.contains(locationVRListener)) {
            this.mLocationVRListeners.add(locationVRListener);
        }
    }

    public void unregisterLocationListener(LocationVRListener<MScenicSpot> locationVRListener) {
        this.mLocationVRListeners.remove(locationVRListener);
    }

    /**
     * 对比最近景区，并自动播放
     *
     * @param location
     * @return
     */
    private boolean autoPlayNear(Location location) {
        MScenicSpot nearSpot = checkNearPoint(location);
        boolean isPlay = startAutoPlayNear(nearSpot);

        if (isPlay) {
            ArrayList<LocationVRListener> list = new ArrayList(mLocationVRListeners.size());
            list.addAll(mLocationVRListeners);
            for (LocationVRListener<MScenicSpot> mLocationVRListener : list) {
                mLocationVRListener.onAutoPlay(nearSpot);
            }
        }
        return isPlay;
    }

    /**
     * 检查附近景区
     * 坐标最近的
     * @param location
     */
    private MScenicSpot checkNearPoint(Location location) {
        double bestDistance = 0;
        MScenicSpot bestSpot = null;

        float[] loc = new float[]{(float) location.getLatitude(), (float) location.getLongitude()};
        float[] cSpotLoc = new float[2];
        for (MScenicSpot spot : dataSpotList) {
            if (spot == null) {
                continue;
            }
            cSpotLoc[0] = spot.getLatitude();
            cSpotLoc[1] = spot.getLongitude();
            double distance = LoctionConvertUtils.distanceBettwenLoc(loc, cSpotLoc);
            //LogUtils.logBle("定位与点距离：" + distance);
            if (distance < MIN_DISTANCE) {
                spot.isNear = true;
                if (bestDistance > distance || bestDistance == 0) {
                    bestDistance = distance;
                    bestSpot = spot;
                }
            } else {
                spot.isNear = false;
                spot.isPlayed = false;
            }
            spot.isNearest = false;
        }

        if (bestSpot != null && bestSpot.isNear == true) {
            bestSpot.isNearest = true;
        }

        return bestSpot;
    }

    /**
     * 播放最近景点
     *
     * @param spot
     */
    private boolean startAutoPlayNear(MScenicSpot spot) {
        if (!isAutoPlay) {
            return false;
        }
        if (spot != null && !VrFactory.get().getMediaControl().isClickPaused()
                && !VrFactory.get().getMediaControl().isFightPlayerAutoPaused()) {
            if (!spot.isPlayed) {
                if (VrFactory.get().getMediaControl().isPlaying() && !VrFactory.get().getMediaControl().isPlaying(spot)) {
                    VrFactory.get().getMediaControl().stop();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                VrFactory.get().getMediaControl().play(spot);
                return true;
            }
        }
        return false;
    }


    //////////////////////////////////////////定位//////////////////////////////////////////////
    public void startAutoGPS() {
        if (bluetoothHandler == null) {
            this.bluetoothHandler = new BluetoothHandler(mContext);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                /*for (MScenicSpot spot : dataSpotList) {
                    if (!TextUtils.isEmpty(spot.getUuid())) {
                        uuidList.add(spot.getUuid());
                    }
                }*/
                bluetoothHandler.startScan(null);
            }
        }).start();

        LogUtils.logLoc("startAutoGPS()");
        //TODO startLocation();
        if (timer == null) {
            timer = new LocationTimer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LogUtils.logLoc("定时定位");
                    //TODO getBetterLocation();
                   postBlueLocation();
                }
            }, AUTO_MILLS * 10, AUTO_MILLS);
        }
    }

    private void postBlueLocation() {
        List<Entry<String, ScannedDevice>> blueDatas = new ArrayList<>();
        blueDatas.addAll(bluetoothHandler.getDataList());

        if (blueDatas == null || blueDatas.isEmpty()) {
            return;
        }

        /** 填充蓝牙设备坐标数据 */
        A : for (Entry<String, ScannedDevice> entry : blueDatas) {
            ScannedDevice device = entry.getValue();
            if (device != null && device.getMapLocs() == null) {
                for (MScenicSpot spot : dataSpotList) {
                    if (spot != null && spot.getDeviceID().equals(entry.getKey())) {
                        if (device.getMapLocs() == null) {
                            float[] locs = new float[2];
                            locs[0] = spot.getLatitude();
                            locs[1] = spot.getLongitude();
                            device.setMapLocs(locs);
                            continue A;
                        }
                    }
                    continue;
                }
            }
        }

        ///////////////////////////////////////暂使用蓝牙定位新逻辑////////////////////////////////////////
        //TODO
        ScannedDevice nearstDevice = null;
        float dis = 0;
        for (Entry<String, ScannedDevice> entry : blueDatas) {
            ScannedDevice sDevice = entry.getValue();
            if (sDevice != null && sDevice.getMapLocs() != null && sDevice.getIBeacon() != null) {
                float distance = sDevice.getDistance(true);
                if (sDevice.getIBeacon().getProximity() == IBeacon.PROXIMITY_IMMEDIATE
                        && (dis == 0 || distance < dis)) {
                        dis = distance;
                        nearstDevice = sDevice;
                }

                /*if (distance > 1.5) {
                    for (MScenicSpot spot : dataSpotList) {
                        if (spot.getDeviceID().equals(entry.getKey())) {
                            spot.isPlayed = false;
                            break;
                        }
                    }
                }*/

                LogUtils.logBle(sDevice.getIBeacon().getMinor()+ "距离级别:" + sDevice.getIBeacon().getProximity() + "    距离:" + distance +"m");
            }
        }

        if (nearstDevice != null && nearstDevice.getMapLocs() != null) {
            float[] locs = new float[2];
            float[] dlocs = nearstDevice.getMapLocs();
            locs[0] = dlocs[0];
            locs[1] = dlocs[1];

            Location location = new Location(BluetoothHandler.Provider);
            location.setLatitude(locs[0]);
            location.setLongitude(locs[1]);

            LogUtils.logBle("蓝牙设备最近的是：" + nearstDevice.getIBeacon().getMinor() +"  相距：" + nearstDevice.getDistance(false) +"m");
            boolean isPlay = autoPlayNear(location);
//            if (isPlay) {
                ArrayList<LocationVRListener> list = new ArrayList(mLocationVRListeners.size());
                list.addAll(mLocationVRListeners);
                for (LocationVRListener mLocationVRListener : list) {
                    mLocationVRListener.onReceive(location);
                }
//            }
        }
        if (true) {
            return;
        }
        //////////////////////////////////////////END///////////////////////////////////////////

        if (blueDatas.size() >= 4) {
            LocationAler aler = new LocationAler();
            float point[] = new float[3];

            for (int i = 0; i < 4; i++) {
                Entry<String, ScannedDevice> entry = null;
                for (Entry<String, ScannedDevice> et : blueDatas) {
                    ScannedDevice device = et.getValue();
                    if (device != null && device.getMapLocs() != null) {
                        entry = et;
                        break;
                    }
                    continue;
                }
                blueDatas.remove(entry);
                ScannedDevice device = entry.getValue();
                //获得坐标
                point[0] = device.getMapLocs()[0];
                point[1] = device.getMapLocs()[1];
                point[2] = i > 1 ? (float) (i - 0.5) : (float)(i + 0.5);
                LogUtils.logBle("蓝牙：" + point[0] +"  " + point[1] + "  " + point[2]);
                LogUtils.logBle("距离 ："+ device.getDistance(false) * 100);
                aler.set_point(point,i);

                //distance
                aler.set_distance((float) device.getDistance(false), i);
            }
            try {
                float[] loc = aler.calc();
                if (loc != null && loc[0] != 0) {
                    LogUtils.logBle("点：" + loc[0] +"  " + loc[1] + "  " + loc[2]);
                    Location location = new Location(BluetoothHandler.Provider);
                    location.setLatitude(loc[0]);
                    location.setLongitude(loc[1]);

                    autoPlayNear(location);
                    ArrayList<LocationVRListener> list = new ArrayList(mLocationVRListeners.size());
                    list.addAll(mLocationVRListeners);
                    for (LocationVRListener mLocationVRListener : list) {
                        mLocationVRListener.onReceive(location);
                    }
                }
            } catch (Exception e) {
                LogUtils.logError("Digst location is Error!:" + e.getLocalizedMessage());
            }
        }
    }

    public void startLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String locationProvider = LocationManager.GPS_PROVIDER;
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }

        locationManager.requestLocationUpdates(locationProvider, MILLS, DISTANCE, locationListener);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MILLS, DISTANCE, locationListener);
        LogUtils.logLoc(locationProvider);
        LogUtils.logLoc(locationManager.getLastKnownLocation(locationProvider) + "");
        getBetterLocation();
    }

    private void getBetterLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            LogUtils.logLoc("getBetterLocation()使用GPS定位");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                LogUtils.logLoc("getBetterLocation()使用网络定位");
                // 纬度
                latitude = location.getLatitude();
                // 经度
                longitude = location.getLongitude();
            }
        }
        LogUtils.logLoc("getBetterLocation():" + latitude + "  " + longitude);

        if (location != null) {
            autoPlayNear(location);
        }

        ArrayList<LocationVRListener> list = new ArrayList(mLocationVRListeners.size());
        list.addAll(mLocationVRListeners);
        for (LocationVRListener mLocationVRListener : list) {
            if (location == null) {
                mLocationVRListener.onFailed(-1, "Location is error!");
            } else {
                mLocationVRListener.onReceive(location);
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    LogUtils.logLoc("onStatusChanged: 当前GPS状态为可见状态");
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MILLS, DISTANCE, locationListener);
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    LogUtils.logLoc("onStatusChanged: 当前GPS状态为服务区外状态");
                    //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    LogUtils.logLoc("onStatusChanged: 当前GPS状态为暂停服务状态");
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MILLS, DISTANCE, locationListener);
                    break;
            }
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            LogUtils.logLoc("onProviderEnabled()" + provider);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            LogUtils.logLoc("onProviderDisabled()" + provider);
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Toast.makeText(mContext, "位置：" + location.getLatitude() + "  " + location.getLongitude(), Toast.LENGTH_LONG).show();
                LogUtils.logLoc("Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                latitude = location.getLatitude(); // 经度
                longitude = location.getLongitude(); // 纬度
            }

            if (location != null) {
                autoPlayNear(location);
            }

            ArrayList<LocationVRListener> list = new ArrayList(mLocationVRListeners.size());
            list.addAll(mLocationVRListeners);
            for (LocationVRListener mLocationVRListener : list) {
                if (location == null) {
                    mLocationVRListener.onFailed(-1, "Location is error!");
                } else {
                    mLocationVRListener.onReceive(location);
                }
            }
        }
    };

    public void release() {
        mContext = null;
        timer.cancel();
        timer = null;
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        bluetoothHandler.release();
        bluetoothHandler = null;
    }
}
