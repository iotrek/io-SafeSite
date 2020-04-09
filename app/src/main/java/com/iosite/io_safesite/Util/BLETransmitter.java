package com.iosite.io_safesite.Util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;


import com.iosite.io_safesite.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class BLETransmitter {

    private static Beacon beacon;
    private static BluetoothManager btManager;
    private static BluetoothAdapter btAdapter;
    private static String TAG = "BLETransmitter";
    private static BeaconTransmitter beaconTransmitter;
    private static boolean wasBLESwitchedOff;

    public static void setupBeacon(Context context) {
        Log.i(TAG, "setupBeacon");
        wasBLESwitchedOff = false;
        beacon = new Beacon.Builder()
                .setId1(Util.getIBeaconId1(context)) // UUID for beacon
                .setId2(context.getString(R.string.beacon_major_simulator)) // Major for beacon
                .setId3(context.getString(R.string.beacon_minor_simulator)) // Minor for beacon
                .setManufacturer(0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
                .setTxPower(-56) // Power in dB
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();

        btManager = (BluetoothManager) context.getSystemService (Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter ();

        beaconTransmitter = new BeaconTransmitter(context, new BeaconParser()
                .setBeaconLayout ("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconTransmitter.setConnectable(true);

        transmitIBeacon(context, true);
    }

    public static boolean isBLETransmitting() {
        return beaconTransmitter.isStarted();
    }

    @Nullable
    public static boolean isBluetoothLEAvailable(Context context) {
        return btAdapter != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean getBlueToothOn() {
        return btAdapter != null && btAdapter.isEnabled();
    }


    public static boolean isBLEOn(Context context) {
        if (getBlueToothOn()) {
            Log.i(TAG, "isBlueToothOn");
//            wasBLESwitchedOff = false;
            return true;
//            transmitIBeacon(context);
//        } else if (!isBluetoothLEAvailable(context)) {
//            Util.showToastMsg(context, "Bluetooth not available on your device");
//            return false;
        } else {
            Log.i(TAG, "BlueTooth is off");
            wasBLESwitchedOff = true;
            return false;
        }
    }

    public static void transmitIBeacon(Context context, boolean transmit) {
        if (isBLEOn(context)) {
//            if (wasBLESwitchedOff) {
//                setupBeacon(context);
//            }
            if (beacon == null || beaconTransmitter == null) {
                Log.i(TAG, "beacon or beaconTransmitter is null");
                setupBeacon(context);
            }
//        setupBeacon(context);
            boolean isSupported = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                isSupported = btAdapter.isMultipleAdvertisementSupported();
                if (isSupported) {

                    Log.v(TAG, "is support advertistment");
//                if (beaconTransmitter.isStarted()) {
                    if (!transmit) {
                        Log.v(TAG, "advertising stopped");
                        beaconTransmitter.stopAdvertising();
//                    beaconIv.setAnimation(null);

                    } else {
                        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                            @Override
                            public void onStartFailure(int errorCode) {
                                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
                            }

                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                                Log.i(TAG, "Advertisement start succeeded." + settingsInEffect.toString());
                            }
                        });
//                    beaconIv.startAnimation(anim);
                    }
                } else {
//                Util.showToastMsg(MyApplication.getInstance().getApplicationContext(), "Your device is not support leBluetooth.");
                    Log.i(TAG, "Your device is not support leBluetooth");
                }
            } else {
//            Util.showToastMsg(context, "Your device is not support leBluetooth.");
                Log.i(TAG, "Your device is not support leBluetooth");
            }
        }
    }
}
