/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jhqc.vr.travel.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.hardware.SensorEvent;
import android.util.Log;

import com.jhqc.vr.travel.bluetooth.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/** LeScanned Bluetooth Device */
public class ScannedDevice {
    private static final String UNKNOWN = "Unknown";
    /** BluetoothDevice */
    private BluetoothDevice mDevice;

    /** RSSI */
    private int mRssi;

    /** Display Name */
    private String mDisplayName;

    /** Advertise Scan Record */
    private byte[] mScanRecord;

    /** parsed iBeacon Data */
    private IBeacon mIBeacon;

    /** last updated (Advertise scanned) */
    private long mLastUpdatedMs;

    private float[] mapLocs;

    public ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long now) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        mLastUpdatedMs = now;
        mDevice = device;
        mDisplayName = device.getName();
        if ((mDisplayName == null) || (mDisplayName.length() == 0)) {
            mDisplayName = UNKNOWN;
        }
        mRssi = rssi;
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    private void checkIBeacon() {
        if (mScanRecord != null) {
            mIBeacon = IBeacon.fromScanData(mScanRecord, mRssi);
        }
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public long getLastUpdatedMs() {
        return mLastUpdatedMs;
    }

    public void setLastUpdatedMs(long lastUpdatedMs) {
        mLastUpdatedMs = lastUpdatedMs;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public String getScanRecordHexString() {
        return ScannedDevice.asHex(mScanRecord);
    }

    static final int COUNT = 5;
    List<Float> disList;
    public void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
        checkIBeacon();
        if (disList == null) {
            disList = new ArrayList<>(COUNT);
        }
        disList.add(0, digstDistance());
        if (disList.size() > COUNT) {
            try {
                disList.remove(COUNT);
            } catch (Exception e) {
            }
        }
    }

    private float digstDistance() {
        if (mIBeacon != null) {
//            return mIBeacon.getDistance(mIBeacon.getRssi(), mIBeacon.getTxPower());
            return mIBeacon.getAccuracy();
        }
        return -3.0f;
    }

    public float getDistance(boolean isMean) {
        if (mIBeacon != null && disList != null && disList.size() != 0) {
            disList.remove(-3.0f);
            if (isMean && disList.size() > 2) {
                float max = 0.0f, min = 0.0f;
                ArrayList<Float> list = new ArrayList<>(disList.size());
                list.addAll(disList);
                for (float dist : list) {
                    if (max == 0 || dist > max) {
                        max = dist;
                    }
                    if (min == 0 || dist < min) {
                        min = dist;
                    }
                }
                disList.remove(max);
                disList.remove(min);
            }

            float count = 0;
            for (float dis : disList) {
                count += dis;
            }
            Log.e("Distance", count +"/" + disList.size() +" = " + count / disList.size());
            return count / disList.size();
        } else {
            return digstDistance();
        }
    }

    public IBeacon getIBeacon() {
        return mIBeacon;
    }

    public boolean isIbeacon() {
        return mIBeacon != null;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public float[] getMapLocs() {
        return mapLocs;
    }

    public ScannedDevice setMapLocs(float[] mapLocs) {
        this.mapLocs = mapLocs;
        return this;
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        // DisplayName,MAC Addr,RSSI,Last Updated,iBeacon flag,Proximity UUID,major,minor,TxPower
        sb.append(mDisplayName).append(",");
        sb.append(mDevice.getAddress()).append(",");
        sb.append(mRssi).append(",");
        sb.append(DateUtil.get_yyyyMMddHHmmssSSS(mLastUpdatedMs)).append(",");
        if (mIBeacon == null) {
            sb.append("false,,0,0,0");
        } else {
            sb.append("true").append(",");
            sb.append(mIBeacon.toCsv());
        }
        return sb.toString();
    }

    /**
     * バイト配列を16進数の文字列に変換する。 http://d.hatena.ne.jp/winebarrel/20041012/p1
     * 
     * @param bytes バイト配列
     * @return 16進数の文字列
     */
    @SuppressLint("DefaultLocale")
    public static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        // バイト配列の要素数分、処理を繰り返す。
        for (int index = 0; index < bytes.length; index++) {
            // バイト値を自然数に変換。
            int bt = bytes[index] & 0xff;

            // バイト値が0x10以下か判定。
            if (bt < 0x10) {
                // 0x10以下の場合、文字列バッファに0を追加。
                sb.append("0");
            }

            // バイト値を16進数の文字列に変換して、文字列バッファに追加。
            sb.append(Integer.toHexString(bt).toUpperCase());
        }

        /// 16進数の文字列を返す。
        return sb.toString();
    }
}
