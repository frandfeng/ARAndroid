package com.jhqc.vr.travel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.jhqc.vr.travel.bluetooth.util.BleUtil;
import com.jhqc.vr.travel.struct.Entry;
import com.jhqc.vr.travel.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

/**
 * Created by Solomon on 2017/11/10 0010.
 */

public class BluetoothHandler {

    public static final String Provider = "Bluetooth";

    static final int major = 16160;
    static final int[] minors = {6839, 6845, 6851, 6854};

    Context mContext;

    private List<Entry<String, ScannedDevice>> mDataList;

    private BluetoothAdapter mBTAdapter;

    public BluetoothHandler(Context context) {
        this.mContext = context;
        this.mDataList = new ArrayList<>();
    }

    public void startScan(List<String> uuids) {
        if (mBTAdapter == null) {
            BluetoothManager manager = BleUtil.getManager(mContext);
            if (manager != null) {
                mBTAdapter = manager.getAdapter();
                if (!mBTAdapter.isEnabled()) {
                    mBTAdapter.enable();
                }
            }
        }

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<ScanFilter> bleScanFilters = new ArrayList<>();
            if (uuids != null) {
                for (String uuid : uuids) {
                    if (!TextUtils.isEmpty(uuid)) {
                        bleScanFilters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(uuid)).build());
                    }
                    LogUtils.logOther("if: uuid:" + uuid);
                }
            }
            mBTAdapter.getBluetoothLeScanner().startScan(bleScanFilters, new ScanSettings.Builder().build(), new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    update(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            });
        } else {*/
            /*if (uuids != null && uuids.size() != 0) {
                UUID[] list = new UUID[uuids.size()];
                for (int i = 0; i < uuids.size(); i++) {
                    String uuid = uuids.get(i);
                    if (!TextUtils.isEmpty(uuid)) {
                        list[i] = UUID.fromString(uuid);
                    }
                    LogUtils.logOther("else: uuid:" + list[i]);
                }
                mBTAdapter.startLeScan(list, callback);
            } else {*/
        mBTAdapter.startLeScan(callback);
//            }
//        }
    }

    public List<Entry<String, ScannedDevice>> getDataList() {
        return mDataList;
    }

    /**
     * add or update BluetoothDevice List
     *
     * @param newDevice  Scanned Bluetooth Device
     * @param rssi       RSSI
     * @param scanRecord advertise data
     * @return summary ex. "iBeacon:3 (Total:10)"
     */
    void update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        LogUtils.logBle("扫描到设备：" + newDevice.getAddress() + "   " + newDevice.getUuids());
        if ((newDevice == null) || (newDevice.getAddress() == null)) {
            return;
        }
        BlueData blueData = new BlueData(newDevice, rssi, scanRecord);

        /////方案一////////////////////////////
        long now = System.currentTimeMillis();
        boolean contains = false;
        for (Entry<String, ScannedDevice> entry : mDataList) {
            ScannedDevice device = entry.getValue();
            if (blueData.newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                device.setRssi(blueData.rssi);
                device.setLastUpdatedMs(now);
                device.setScanRecord(blueData.scanRecord);
                //break;
                return;
            }
        }
        ScannedDevice scannedDevice = new ScannedDevice(blueData.newDevice, blueData.rssi, blueData.scanRecord, now);
        if (!contains && scannedDevice.isIbeacon() && isCurrentDevice(scannedDevice.getIBeacon())) {
            IBeacon beacon = scannedDevice.getIBeacon();
            mDataList.add(new Entry(getKeyByBeacon(beacon), scannedDevice));
        } else {
            return;
        }

        /////方案二////////////////////////////
        /*stack.add(0, blueData);
        if (task == null || !task.isAlive()) {
            isRceiving = true;
            task = new Thread(receiveTask);
            task.start();
        }*/
    }

    boolean isRceiving = false;
    ArrayList<BlueData> stack = new ArrayList<>();
    Thread task;

    Runnable receiveTask = new Runnable() {
        @Override
        public void run() {
            isRceiving = true;
            int mills = 5;
            while (isRceiving) {
                if (!stack.isEmpty()) {
                    BlueData blueData = stack.remove(0);
                    long now = System.currentTimeMillis();

                    boolean contains = false;
                    for (Entry<String, ScannedDevice> entry : mDataList) {
                        ScannedDevice device = entry.getValue();
                        if (blueData != null && blueData.newDevice != null && device != null && device.getDevice()!= null &&
                                blueData.newDevice.getAddress().equals(device.getDevice().getAddress())) {
                            contains = true;
                            // update
                            device.setRssi(blueData.rssi);
                            device.setLastUpdatedMs(now);
                            device.setScanRecord(blueData.scanRecord);
                            break;
                        }
                    }
                    if (!contains) {
                        ScannedDevice scannedDevice = new ScannedDevice(blueData.newDevice, blueData.rssi, blueData.scanRecord, now);
                        if (scannedDevice.isIbeacon() && isCurrentDevice(scannedDevice.getIBeacon())){
                            IBeacon beacon = scannedDevice.getIBeacon();
                            mDataList.add(new Entry(getKeyByBeacon(beacon), scannedDevice));
                        }
                    }
                    mills = 5;
                } else {
                    mills = 100;
                }
                try {
                    Thread.sleep(mills);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    class BlueData {
        public BluetoothDevice newDevice;
        public int rssi;
        public byte[] scanRecord;

        public BlueData(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
            this.newDevice = newDevice;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
    }

    public String getKeyByBeacon(IBeacon beacon) {
        return String.valueOf(beacon.getMajor()) + "-" + String.valueOf(beacon.getMinor());
    }

    boolean isCurrentDevice(IBeacon iBeacon) {
        if (iBeacon.getMajor() == major) {
            for (int minor : minors) {
                if (iBeacon.getMinor() == minor) {
                    return true;
                }
                continue;
            }
        }

        return false;
    }

    BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                             final byte[] newScanRecord) {
            update(newDeivce, newRssi, newScanRecord);
        }
    };

    public void release() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(callback);
        }
        isRceiving = false;
        mBTAdapter.stopLeScan(null);
    }
}
