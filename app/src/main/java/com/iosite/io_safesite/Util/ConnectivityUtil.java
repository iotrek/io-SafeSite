package com.iosite.io_safesite.Util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ConnectivityUtil {

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static WifiManager getWifiInfo(Context context) {
        WifiManager cm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return cm;
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isWifiOn(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true;
        } else return false;
    }

    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isOnBluetooth(Context context) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            return false;
        } else {
            mBluetoothAdapter.getState();
            // Bluetooth is enabled
            return true;
        }
    }

    public static boolean isConnectedBluetooth(Context context) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.getProfileConnectionState(BluetoothAdapter.STATE_CONNECTED) == BluetoothAdapter.STATE_CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isConnectedFast(Context context) {
        Log.i("Kamran", "isConnectedFast");
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        if (info != null) {
            Log.i("kamran", "infor type: " + info.getTypeName());
        }
        return (info != null && info.isConnected() && ConnectivityUtil.isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static String getNetworkType(Context context) {
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return info.getTypeName();
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return info.getSubtypeName();
            } else if (info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                return info.getTypeName();
            } else {
                return info.getTypeName();
            }
        } else {
            return "NullNetworkInfo";
        }
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static int mobileUpSpeed(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return nc.getLinkUpstreamBandwidthKbps();
    }

    public static int mobileDownSpeed(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return nc.getLinkDownstreamBandwidthKbps();
    }

    public static int getWifiLevel(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(linkSpeed, 5);
        return level;
    }

    public static boolean internetHandle(Context mContext) {
        int MIN_BANDWIDTH_KBPS = 5000;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();

        if (activeNetwork != null) {
            int bandwidth = connectivityManager.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps();
            if (bandwidth < MIN_BANDWIDTH_KBPS) {
                Log.i("Kamran Internet: ", "Req fast Internet. Current BW is: " + bandwidth);
//                requestFastInternet(mContext);
                return true;
            } else {
                // You already are on a high-bandwidth network, so start your network request
                Log.i("Kamran Internet: ", "Already using high speed internet. Speed is: " + bandwidth);
                return false;
            }
        } else {
            // No active network
            Log.i("Kamran Internet", "Req for fast internet. No Internet available.");
            return true;
        }

    }

    public static void logConnectivityStatus(Context mContext) {
        Log.i("Kamran", "WiFi connected: " + ConnectivityUtil.isConnectedWifi(mContext));
        Log.i("Kamran", "mobile: " + ConnectivityUtil.isConnectedMobile(mContext));
        Log.i("Kamran", "fast: " + ConnectivityUtil.isConnectedFast(mContext));
        if (ConnectivityUtil.isConnectedWifi(mContext)) {
            Log.i("Kamran", "wifilevel: " + ConnectivityUtil.getWifiLevel(mContext));
        }
        Log.i("Kamran", "bluetooth on: " + ConnectivityUtil.isOnBluetooth(mContext));
        Log.i("Kamran", "bluetooth connected: " + ConnectivityUtil.isConnectedBluetooth(mContext));
        Log.i("Kamran", "isWifi On: " + ConnectivityUtil.isWifiOn(mContext));
        Log.i("Kamran", "network type: " + ConnectivityUtil.getNetworkType(mContext));
        Log.i("Kamran Internet", "isInternetConnected " + Util.isInternetConnected(mContext) + "");
    }
}