package com.iosite.io_safesite.BroadCastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class BroadcastReceiverClass extends BroadcastReceiver {
    private String TAG = "BroadcastReceiverClass";

    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("Action",action);

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.i(TAG, "Scan Finished");
            bluetoothDevices.clear();
            addresses.clear();
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = device.getName();
            String address = device.getAddress();
            String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE));

            if (!addresses.contains(address)) {
                addresses.add(address);
                String deviceString = "";
                if (name == null || name.equals("")) {
                    deviceString = address + " - RSSI " + rssi + "dBm";
                } else {
                    deviceString = name + "  Address:- " + address + " - RSSI " + rssi + "dBm";
                }
                Log.i("BLE 4", deviceString);

                bluetoothDevices.add(deviceString);
//                arrayAdapter.notifyDataSetChanged();
            }
        }
    }
}
