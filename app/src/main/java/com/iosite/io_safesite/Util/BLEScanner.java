package com.iosite.io_safesite.Util;

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
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.iosite.io_safesite.MyApplication;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class BLEScanner {


    private static final String TAG = "BLEScanner";

    private static final long SCAN_PERIOD = 30*1000;
    private static BluetoothLeScanner mLEScanner;
    private static BluetoothManager btManager;
    private static BluetoothAdapter btAdapter;
    private static Handler scanHandler;
    private static Handler mHandler;
    private Button checkInBtn;
    private TextView tvEmpID;
    private ImageView ivRandomEmp;
    private static boolean isShowDialog;

    private static ScanSettings settings;
    private static ArrayList<ScanFilter> filters;
    private static String employeeID;
//    private GPSTracker gps;

    private static GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private static List<String> beaconDeviceList = new ArrayList<>();

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int REQUEST_CHECK_SETTINGS = 14;

    public static void initScanner(Context context) {
        Log.i(TAG, "initScanner");
        isShowDialog = false;
        scanHandler = new Handler();
        mHandler = new Handler();
        employeeID = getRandomID();

        settingBlueTooth(context);
//        googleApiClient.connect();
//        settingLocationRequest();
//        checkLocationPermission();
        getBeaconDevice();

//        if (isLocationEnabled()) {
//            gps = new GPSTracker(getContext());
//            Log.i("Location_Lat", getLat() + " " + getLon());
//        } else {
//            displayLocationSettingsRequest();
//        }
    }

    public static void settingBlueTooth(Context context) {
        // init BLE
//        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btManager = (BluetoothManager) MyApplication.getInstance().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
//            ScanFilter scanFilter = new ScanFilter.Builder()
//                    .setServiceUuid(ParcelUuid.fromString(OWDevice.OnewheelServiceUUID))
//                    .build();
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setManufacturerData(0xabcd, new byte[] {});
            ScanFilter filter = builder.build();
            filters.add(filter);

        }
    }

    public static boolean getBlueToothOn() {
        return btAdapter != null && btAdapter.isEnabled();
    }

//    public static boolean isBluetoothLEAvailable(Context context) {
//        return btAdapter != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
//    }

    public static boolean isBLEOn(Context context) {
        if (getBlueToothOn()) {
            Log.i(TAG, "BlueTooth is ON");
            return true;
//            transmitIBeacon(context);
//        } else if (!isBluetoothLEAvailable(context)) {
//            Log.i(TAG, "BlueTooth is NOT Available on thi device");
//            return false;
        } else {
            Log.i(TAG, "BlueTooth is OFF");
            return false;
        }
    }

    public static void startScan(Context context) {
//        checkInBtn.setClickable(false);
        if (isBLEOn(context)) {
            scanLeDevice(true, context);
        } else {
            Log.i(TAG, "BLE Not available on startScan");
        }
    }

    public static void stopScan(Context context) {
//        checkInBtn.setClickable(true);
        if (isBLEOn(context)) {
            scanLeDevice(false, context);
        } else {
            Log.i(TAG, "BLE Not available on stopScan");
        }
    }

    public static void scanLeDevice(final boolean enable, final Context context) {
        if (enable) {
            Log.i(TAG, "BLE start scan");
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(TAG, "start SDK_INT < 21");
                btAdapter.startLeScan(leScanCallback);
            } else {
                Log.i(TAG, "start SDK_INT >= 21");
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            Log.i(TAG, "BLE stop scan");
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(TAG, "stop SDK_INT < 21");
                btAdapter.stopLeScan(leScanCallback);
            } else {
                Log.i(TAG, "stop SDK_INT >= 21");
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private static ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte[] scanRecord = result.getScanRecord().getBytes();
            findBeaconPattern(scanRecord);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i(TAG, "ScanResult - Results" + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed Error Code: " + errorCode);
        }
    };

    private static BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            Log.i(TAG, "leScanCallback ");
            findBeaconPattern(scanRecord);
        }
    };

    private static void findBeaconPattern(byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //UUID detection
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);

            // major
            final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            // minor
            final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            Log.i(TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);
//            foundBeacon(uuid, major, minor);
        }
    }

//    private void foundBeacon(String uuid, int major, int minor) {
//
////        final LocationModel locationModel = new LocationModel(getLat(), getLon());
//
////        final CheckInModel data = new CheckInModel("amonratk", getDateString(), getTimeString(), uuid, String.valueOf(minor), String.valueOf(major), locationModel);
//
//        for (String device : beaconDeviceList) {
//            if (uuid.equalsIgnoreCase(device)) {
//                Log.e(TAG, "isShowDialog : " + isShowDialog);
//                if (!isShowDialog) {
//                    UiHelper.showConfirmDialog(getContext(), "Time Attendant Confirmation", "Check In at  " + getCurrentDateTime(), false, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            if (i == DialogInterface.BUTTON_POSITIVE) {
//                                saveToFirebase(data);
//                            } else {
//                                isShowDialog = false;
//                            }
//                        }
//                    });
//
//                    isShowDialog = true;
//                }
//                stopScan();
//            } else {
//                Log.i(TAG, "Its not TISCO Beacon");
//            }
//        }
//    }
//


    private String getCurrentDateTime() {
        return getTimeString() + " (" + getDateString() + ")";
    }

    private Date getDate() {
        return Calendar.getInstance().getTime();
    }

    private String getDateString() {
        return DateFormat.getDateInstance().format(getDate());
    }

    private String getTimeString() {
        return DateFormat.getTimeInstance().format(getDate());
    }

    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // to be called on destroy
    public static void removeScanCallBack() {
        scanHandler.removeCallbacksAndMessages(null);
    }

    public static String getRandomID() {
        Random rn = new Random();
        String department = String.valueOf(rn.nextInt(15) + 1);
        String id = String.valueOf(rn.nextInt(999) + 1);

        String departmentPadding = String.format("%02d", Integer.parseInt(department));
        String idPadding = String.format("%03d", Integer.parseInt(id));
        return departmentPadding + "-" + idPadding;
    }

    public static void getBeaconDevice() {
        //Get form your service
        beaconDeviceList.add("954e6dac-5612-4642-b2d1-d253429db36b");
        beaconDeviceList.add("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6");
    }


}
