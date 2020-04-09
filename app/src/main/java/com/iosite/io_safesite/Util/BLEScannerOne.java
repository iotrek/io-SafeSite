package com.iosite.io_safesite.Util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import com.iosite.io_safesite.MyApplication;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BLEScannerOne{
    private static BeaconManager mBeaconManager;
    private Context mContext;
    private static BluetoothAdapter btAdapter;
    private static BluetoothManager btManager;
    private static String TAG = "BLEScannerOne";

    public static void initScanner(Context context) {
        Log.i(TAG, "initScanner");
        mBeaconManager = BeaconManager.getInstanceForApplication(context);
        btManager = (BluetoothManager) MyApplication.getInstance().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
//        setUpScanTimes();
        startBLEScan(context);

    }

    public static boolean isBLEScanning() {
        return mBeaconManager.isAnyConsumerBound();
    }

    private static void setUpScanTimes() {
//        mBeaconManager.setBackgroundScanPeriod(10*1000);
//        mBeaconManager.setBackgroundBetweenScanPeriod(300*1000);
//        mBeaconManager.setForegroundScanPeriod(20*1000);
//        mBeaconManager.setForegroundBetweenScanPeriod(10*1000);

//        mBeaconManager.setBackgroundBetweenScanPeriod(10000l);
        mBeaconManager.setForegroundBetweenScanPeriod(0);
//        mBeaconManager.setBackgroundScanPeriod(5000l);
        mBeaconManager.setForegroundScanPeriod(4100l);
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


//        try {
//            mBeaconManager.updateScanPeriods();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    public static boolean getBlueToothOn() {
        return btAdapter != null && btAdapter.isEnabled();
    }

    public static boolean isBLEOn(Context context) {
        if (getBlueToothOn()) {
            Log.i(TAG, "BlueTooth is ON");
            return true;
        } else {
            Log.i(TAG, "BlueTooth is OFF");
            return false;
        }
    }

    public static void startBLEScan(Context context) {
        if (isBLEOn(context)) {
            Log.i(TAG, "startBLEScan");
            mBeaconManager.getBeaconParsers().add(new BeaconParser()
                    .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
            Log.i(TAG, "mBeaconManager.getBackgroundScanPeriod(): " + mBeaconManager.getBackgroundScanPeriod() +
                    "  getBackgroundBetweenScanPeriod:" + mBeaconManager.getBackgroundBetweenScanPeriod() +
                    "  getForegroundScanPeriod: " + mBeaconManager.getForegroundScanPeriod() +
                    "  getForegroundBetweenScanPeriod: " + mBeaconManager.getForegroundBetweenScanPeriod());


//            Context mContext = MyApplication.getInstance().getApplicationContext();
            mBeaconManager.bind(new BeaconConsumer() {
                @Override
                public void onBeaconServiceConnect() {
                    Log.i(TAG, "onBeaconServiceConnect new");
                    mBeaconManager.removeAllRangeNotifiers();
                    mBeaconManager.addRangeNotifier(new RangeNotifier() {
                        @Override
                        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                            if (beacons.size() > 0) {
                                String companyIdInBeacondata = beacons.iterator().next().getId1().toString().substring(0, 8);
                                if(Constants.iositeBeaconID.equalsIgnoreCase(companyIdInBeacondata)) {
                                    Util.saveProximityDataToDB(beacons.iterator().next().getId1().toString(),     // string
                                            beacons.iterator().next().getId2().toString(),      // string
                                            beacons.iterator().next().getDistance(),            // double
                                            beacons.iterator().next().getRssi(),                // int
                                            beacons.iterator().next().getTxPower()              // int
                                    );
                                }
//                            Log.i(TAG, "BLE Beacon. ID1: " + beacons.iterator().next().getId1().toString());
//                            Log.i(TAG, "BLE Beacon. ID2" + beacons.iterator().next().getId2().toString());
//                            Log.i(TAG, "BLE Beacon. RSSI: " + beacons.iterator().next().getRssi());
//                            Log.i(TAG, "BLE Beacon. Distance: " + beacons.iterator().next().getDistance());
//                            Log.i(TAG, "BLE Beacon. TX Power: " + beacons.iterator().next().getTxPower());
                            }
                        }
                    });

                    try {
                        mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
                    } catch (RemoteException e) {    }

                }

                @Override
                public Context getApplicationContext() {
                    return null;
                }

                @Override
                public void unbindService(ServiceConnection serviceConnection) {

                }

                @Override
                public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
                    return false;
                }
            });
        }

    }

    public static void stopBLEScan(Context context) {
        if (isBLEOn(context)) {

            if(mBeaconManager.checkAvailability()) {
                Log.i(TAG, "stopBLEScanning");
                mBeaconManager.unbind(new BeaconConsumer() {
                    @Override
                    public void onBeaconServiceConnect() {

                    }

                    @Override
                    public Context getApplicationContext() {
                        return null;
                    }

                    @Override
                    public void unbindService(ServiceConnection serviceConnection) {
                        Log.i(TAG, "Unbind Done");

                    }

                    @Override
                    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
                        return false;
                    }
                });
            }
        }
    }

}
