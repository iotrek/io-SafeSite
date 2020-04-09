package com.iosite.io_safesite.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.iosite.io_safesite.MyApplication;
import com.iosite.io_safesite.Pojo.ExposureData;
import com.iosite.io_safesite.Pojo.ProximityData;
import com.iosite.io_safesite.Services.MyFirebaseMessagingService;
import com.iosite.io_safesite.database.ExposureORM;
import com.iosite.io_safesite.database.ProximityORM;

import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Util {

    private static final int RECORDING_RATE = 44100;
    private static String TAG = "Util";
    private static AlertDialog.Builder builder;

    /**
     * method to check whether internet connection is available or not
     *
     * @param context
     * @return
     */
    public static boolean isInternetConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivity != null) {
                networkInfo = connectivity.getActiveNetworkInfo();
            }
            if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                Log.e("Kamran", "Internet State: Connected");
                return true;
            } else {
                Log.e("Kamran", "Internet State: NoT Connected");
                return false;
            }
        }
        Log.e("Kamran", "Internet State: NoT Connected");
        return false;
    }


    //public static

    public static void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "mp3"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, RECORDING_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    static byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getTimetoShow(long updatedAt) {

        updatedAt = updatedAt * 1000;
        Date eventDate = new Date(updatedAt);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(updatedAt);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        if (System.currentTimeMillis() - updatedAt <= 30000) {
            return "just now";
        } else {
            return formatter.format(eventDate);
        }
    }

    public static String getTimeDifference(Date receivedDate) {
        String timeDiffText = "";
        Date currentDate = new Date();
        double expTimeLeft = Math.abs(receivedDate.getTime() - currentDate.getTime());
        double expiryTimeSeconds = expTimeLeft / 1000;
        double expiryTimeMinutes = expiryTimeSeconds / 60;
        double expiryTimeHours = expiryTimeMinutes / 60;
        double expiryTimeDays = expiryTimeHours / 24;
        double expiryTimeMonths = expiryTimeDays / 30;
        double expiryTimeYears = expiryTimeMonths / 12;

        String unitOne = "";
        String unitTwo = "";

        if (expiryTimeYears > 1) {
            if ((int) expiryTimeYears > 1) unitOne = " yrs ";
            else unitOne = " yr ";
            if ((int) ((expiryTimeYears - (int) expiryTimeYears) * 12) > 1) unitTwo = " mos";
            else unitTwo = " mo";
            timeDiffText = String.valueOf((int) expiryTimeYears) + unitOne;
//                    + String.valueOf((int)((expiryTimeYears - (int)expiryTimeYears)*12)) + unitTwo;
//            Log.i(TAG, "timeDiffText" + timeDiffText);

        } else if (expiryTimeMonths > 1) {
            if ((int) expiryTimeMonths > 1) unitOne = " mos ";
            else unitOne = " mo ";
            if ((int) ((expiryTimeMonths - (int) expiryTimeMonths) * 30) > 1) unitTwo = " d";
            else unitTwo = " d";
            timeDiffText = String.valueOf((int) expiryTimeMonths) + unitOne;
//                    + String.valueOf((int)((expiryTimeMonths - (int)expiryTimeMonths)*30)) + unitTwo;
//            Log.i(TAG, "timeDiffText" + timeDiffText);
        } else if (expiryTimeDays > 1) {
            if ((int) expiryTimeDays > 1) unitOne = " d ";
            else unitOne = " d ";
            if ((int) ((expiryTimeDays - (int) expiryTimeDays) * 24) > 1) unitTwo = " hrs";
            else unitTwo = " hr";
            timeDiffText = String.valueOf((int) expiryTimeDays) + unitOne;
//                    + String.valueOf((int)((expiryTimeDays - (int)expiryTimeDays)*24)) + unitTwo;
//            Log.i(TAG, "timeDiffText" + timeDiffText);
        } else if (expiryTimeHours > 1) {
            if ((int) expiryTimeHours > 1) unitOne = " hrs ";
            else unitOne = " hr ";
            if ((int) ((expiryTimeHours - (int) expiryTimeHours) * 60) > 1) unitTwo = " mins";
            else unitTwo = " min";
            timeDiffText = String.valueOf((int) expiryTimeHours) + unitOne;
//                    + String.valueOf((int)((expiryTimeHours - (int)expiryTimeHours)*60)) + unitTwo;
//            Log.i(TAG, "timeDiffText" + timeDiffText);
        } else if (expiryTimeMinutes > 1) {
            if ((int) expiryTimeMinutes > 1) unitOne = " mins ";
            else unitOne = " min ";
            if ((int) ((expiryTimeMinutes - (int) expiryTimeMinutes) * 60) > 1) unitTwo = " secs";
            else unitTwo = " sec";
            timeDiffText = String.valueOf((int) expiryTimeMinutes) + unitOne;
//                    + String.valueOf((int)((expiryTimeMinutes - (int)expiryTimeMinutes)*60)) + unitTwo;
//            Log.i(TAG, "timeDiffText" + timeDiffText);
        } else if (expiryTimeSeconds > 1) {
            timeDiffText = String.valueOf((int) expiryTimeSeconds) + " secs";
//            Log.i(TAG, "timeDiffText" + timeDiffText);
        }

        return timeDiffText;
    }

    public static String changeToLocalTimeZone(String receivedTime) {
        Date date;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        String formattedDate = "";
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = df.parse(receivedTime);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public static String initialToUpperCase(String string) {
        if (string != null && !string.isEmpty()) {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        } else {
            return "";
        }

    }

    public static String getTimeFromTimestamp(String timestampString) {
        String timeString = "";
        try {
            Date timestampDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(timestampString);
            timeString = new SimpleDateFormat("hh:mm aa").format(timestampDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeString;
    }

    public static void clearCache(Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.getCache().clear();
    }


    public static Map<String, String> getRequestHeaders(Context mContext) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
        requestHeaders.put("Content-Type", "application/json");
        return requestHeaders;
    }

    private static String getJsonPayload(Context mContext) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_MSG_TYPE, PrefUtil.getString(mContext, Constants.PREF_MSG_TYPE, ""));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();

    }

    public static Gson getGson() {
        Gson gson = new Gson();
        return gson;
    }

    public static void showToastMsg(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static boolean isInternetConnectedOnChange(VolleyError error, String customMsg) {
        if (error != null && error.networkResponse == null) {
            Log.i(TAG, "Kamran error.ToString: " + error.toString());
            Log.i(TAG, "Kamran customMsg: " + customMsg);
            if(error.toString().equalsIgnoreCase("com.android.volley.VolleyError: No internet connection.")
                    && customMsg.equalsIgnoreCase("No internet connection.")) {
                Log.e("Kamran", "isInternetConnectedOnChange: Unknown State");
                return false;
            } else {
                Log.e("Kamran", "isInternetConnectedOnChange: NOT Connected");
                return true;
            }
        } else {
            Log.e("Kamran", "isInternetConnectedOnChange: condition not met");
            return true;
        }
    }


    public static void openFCMPort(Context mContext){
        if(isInternetConnected(mContext)) {
//            Log.i(TAG, "Kamran: called openFCMPort");
            try {
                DatagramSocket ds1 = new DatagramSocket(5228);
                DatagramSocket ds2 = new DatagramSocket(5229);
                DatagramSocket ds3 = new DatagramSocket(5230);
            } catch (Exception ex) {
                Log.e("Kamran", "Inside openFCMPort Exception");
                ex.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void sendSensorData(Context mContext) {
        JSONObject jsonObject = new JSONObject();
        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Log.i(TAG, "SendSensor IMEI: " + PrefUtil.getString(mContext, Constants.PARAM_IMEI, "999999999999999"));
        try {
            jsonObject.put(Constants.PARAM_IMEI, PrefUtil.getString(mContext, Constants.PARAM_IMEI, "999999999999999"));
            jsonObject.put(Constants.PARAM_TIMESTAMP, Instant.now());
            jsonObject.put(Constants.PARAM_BATTERY, PrefUtil.getString(mContext, Constants.PREF_BATTERY_PERCENTAGE, "00"));
            jsonObject.put(Constants.PARAM_ONBODY, PrefUtil.getString(mContext, Constants.PREF_ON_OFF_BODY_VALUE, "false"));
            jsonObject.put(Constants.PARAM_IS_CHARGING, PrefUtil.getString(mContext, Constants.PREF_IS_CHARGING, "false"));
            jsonObject.put(Constants.PARAM_EVENT_UPLINK, PrefUtil.getString(mContext, Constants.PREF_EVENT_UPLINK, "status"));
            jsonObject.put(Constants.PARAM_GPS_STATUS, String.valueOf(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
            Log.i(TAG, "Kamran: " + jsonObject.toString());   // addedd by kamran
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        SendSensor sendSensor = new SendSensor(mContext);
//        sendSensor.sendUplink(jsonObject);

    }

    public static void sendFCMToIosite(Context mContext) {
        if(PrefUtil.isKeyExistInPref(mContext, Constants.PREF_USER_ACCESS_TOKEN)
                && !PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, "").isEmpty()) {

            if (PrefUtil.isKeyExistInPref(mContext, Constants.PREF_FCM_TOKEN)
                    && !PrefUtil.getString(mContext, Constants.PREF_FCM_TOKEN, "").isEmpty()) {
                Log.i(TAG, "FCM Token already Updated on Server is: " + PrefUtil.getString(mContext, Constants.PREF_FCM_TOKEN, ""));
            } else {
                Log.i(TAG, "Registering for FCM Notif from IOSITE");
                MyFirebaseMessagingService myFirebaseMessagingService = new MyFirebaseMessagingService(mContext);
                myFirebaseMessagingService.fcmTokenRetrieve();
            }

//            if(!PrefUtil.isKeyExistInPref(mContext, Constants.PREF_FCM_TOKEN)) {
//                Log.i("Kamran NetworkReceiver", "FCM Token already sent");
//                MyFirebaseMessagingService myFirebaseMessagingService = new MyFirebaseMessagingService(mContext);
//                myFirebaseMessagingService.fcmTokenRetrieve();
//            }
        }
    }



    public static void printScanRecord (byte[] scanRecord) {

        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord,"UTF-8");
            Log.d("DEBUG","decoded String : " + ByteArrayToString(scanRecord));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);


        // Print individual records
        if (records.size() == 0) {
            Log.i("DEBUG", "Scan Record Empty");
        } else {
            Log.i("DEBUG", "Scan Record: " + TextUtils.join(",", records));
        }

    }


    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }


    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data,"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }

            return records;
        }
    }


    public static boolean isBeaconSupported() {
        Context mContext = MyApplication.getInstance().getApplicationContext();
        int result = BeaconTransmitter.checkTransmissionSupported(mContext);
        if(result == BeaconTransmitter.SUPPORTED) {
            Log.i(TAG, "beaconsupported");
            return true;
        }
        else {
            Log.i(TAG, "not beacon supported");
            return false;
        }
    }

    public static Map<String, String> setContentType() {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");
        Log.e(TAG, requestHeaders.toString());
        return requestHeaders;
    }

    public static Map<String, String> getAuthRequestHeaders(Context context) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");
        requestHeaders.put("Authorization", PrefUtil.getString(context, Constants.PREF_USER_ACCESS_TOKEN, ""));
        Log.e(TAG, requestHeaders.toString());
        return requestHeaders;
    }

    public static String getIBeaconId1(Context context) {
        if(PrefUtil.isKeyExistInPref(context, Constants.PREF_USER_BEACON_ID)
                && !PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").isEmpty()) {
//            2f234454-cf6d-4a0f-adf2-f4911ba9ffa6
            String iBeaconId1 = PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(0, 8) + "-"
                    + PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(8, 12) + "-"
                    + PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(12, 16) + "-"
                    + PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(16, 20) + "-"
                    + PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(20, 32);
            return iBeaconId1;
        }
        else {
            return Constants.iositeBeaconID  + "AAAAAAAAAAAA";
        }
    }

    public static String getEddyBeaconID1(Context context) {
        if(PrefUtil.isKeyExistInPref(context, Constants.PREF_USER_BEACON_ID)
                && !PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").isEmpty()) {
            return PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(0, 20);
        }
        else {
            return "1234ABCDAAAAAAAAAAAA";
        }
    }

    public static String getEddyBeaconID2(Context context) {
        if(PrefUtil.isKeyExistInPref(context, Constants.PREF_USER_BEACON_ID)
                && !PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").isEmpty()) {
            return PrefUtil.getString(context, Constants.PREF_USER_BEACON_ID, "").substring(20, 32);
        }
        else {
            return "AAAAAAAAAAAA";
        }
    }

    public static void saveProximityDataToDB(String ID1, String ID2, double distance, int rssi, int tx_power) {
//        String received_user_beacon_id = (ID1.substring(2, 22) + ID2.substring(2, 14)).substring(8, 32);
        String received_user_beacon_id = ID1.substring(8, ID1.length());
        received_user_beacon_id = received_user_beacon_id.replace("-", "");
        String[] received_distance_array = String.valueOf(distance).split("(?<=\\.\\d{2})");
        String received_distance = received_distance_array[0];
        String received_rssi = String.valueOf(rssi);
        String received_tx_power = String.valueOf(tx_power);
        Log.i("BLE Beacon:",
                "  received_user_beacon_id: " + received_user_beacon_id +
                        "  received_distance: " + received_distance +
                        "  received_rssi: " + received_rssi +
                        "  received_tx_power: " + received_tx_power
        );
        ProximityORM.insertProximityData(received_user_beacon_id, received_distance, received_rssi, received_tx_power);
//        logProximityData();
    }

    public static LinkedList<ProximityData> logProximityData() {
        LinkedList<ProximityData> proximityList = new LinkedList<>();
        proximityList = ProximityORM.getProximityData();
//        for(ProximityData proximityData: proximityList) {
//            Log.i(TAG, "Proximity data. BeaconID: " + proximityData.beaconId
//                    + " Distance: " + proximityData.distance
//                    + " RSSI: " + proximityData.rssi
//                    + " TX Power: " + proximityData.txPower
//                    + "  Is Computed: " + proximityData.is_computed
//                    + " CreatedAt: " + getDateFromMillis(proximityData.createdAt));
//        }
        return proximityList;
    }

    public static void logProximityData_kamran() {
        ArrayList<ProximityData> proximityList = new ArrayList<>();
        proximityList = ProximityORM.getProximityData_kamran();
        for(ProximityData proximityData: proximityList) {
            Log.i(TAG, "Proximity data. BeaconID: " + proximityData.beaconId
                    + " Distance: " + proximityData.distance
                    + " RSSI: " + proximityData.rssi
                    + " TX Power: " + proximityData.txPower
                    + " CreatedAt: " + getDateFromMillis(proximityData.createdAt));
        }
    }

    public static void printDeviceSize(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ((Activity) context).getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        Log.d(TAG, " dpi: " + density + "");
        Log.d(TAG, " dpheight: " + dpHeight + "");
        Log.d(TAG, " dpiwidth: " + dpWidth + "");
    }

    public static void showInformationMessage(Context context, String title, String message, boolean cancelable) {
        showInformationMessage(context, title, message, cancelable, null);
    }

    public static void showInformationMessage(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", listener);
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void showErrorMessage(Context context, String message) {
        showErrorMessage(context, message, false);
    }


    public static void showErrorMessage(Context context, String message, boolean cancelable) {
        showErrorMessage(context, message, cancelable, null);
    }


    public static void showErrorMessage(Context context, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton("OK", listener);
        builder.setCancelable(cancelable);
        builder.show();
    }


    public static String getDateFromMillis(long milliSeconds)
    {
        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        Date result = new Date(milliSeconds);
        String text = simple.format(result);
        return text;
    }

    public static long getMillisFromStringTS(String dateTimeinISO){
        String strDate = "2013-05-15T10:00:00-0700";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        Date date = null;
        try {
            date = dateFormat.parse(dateTimeinISO);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static int getArrayListAverage(ArrayList<Integer> integerArrayList) {
        Integer sum = 0;
        if(!integerArrayList.isEmpty()) {
            for (Integer integerValue : integerArrayList) {
                sum += integerValue;
            }
            return (sum / integerArrayList.size());
        }
        return 0;
    }

    public static double getDoubleArrayListAverage(ArrayList<Double> doubleArrayList) {
        double sum = 0;
        if(!doubleArrayList.isEmpty()) {
            for (double doubleValue : doubleArrayList) {
                sum += doubleValue;
            }
            return (sum / doubleArrayList.size());
        }
        return 0;
    }

    public static double restrictDecimaltoTwo(double value) {
        double valueInTo100 = value*100;
        double valueToTwoDec = (int)valueInTo100/100;
        String[] value_array = String.valueOf(value).split("(?<=\\.\\d{2})");
//        return valueToTwoDec;
        return Double.valueOf(value_array[0]);
    }

    public static double getMinValue(ArrayList<Double> doubleArrayList) {

        if (doubleArrayList.size() == 0) {
            Log.i(TAG, "Array Size Zero");
            return 0;
        }
        return restrictDecimaltoTwo(Collections.min(doubleArrayList));
    }

    public static double getMaxValue(ArrayList<Double> doubleArrayList) {
        if (doubleArrayList.size() == 0) {
            return 0;
        }
        return restrictDecimaltoTwo(Collections.max(doubleArrayList));
    }

    public static double getDoubleArrayListMedian(ArrayList<Double> doubleArrayList) {
        Collections.sort(doubleArrayList);
        double arrayMean = 0;   // = doubleArrayList.size()/2;
        if (doubleArrayList.size() == 0) {
            return 0;
        }
        if (doubleArrayList.size() == 1) {
            return doubleArrayList.get(0);
        }
        if (doubleArrayList.size() == 2) {
            return (doubleArrayList.get(0) + doubleArrayList.get(1))/2;
        }
        if (doubleArrayList.size()%2 == 1) {
//            arrayMean = (doubleArrayList.get(doubleArrayList.size()/2) + doubleArrayList.get(doubleArrayList.size()/2 - 1)/2);
            arrayMean = doubleArrayList.get((doubleArrayList.size()-1)/2 + 1);
        } else {
//            arrayMean = doubleArrayList.get(doubleArrayList.size() / 2);
            arrayMean = (doubleArrayList.get(doubleArrayList.size() / 2) + doubleArrayList.get(doubleArrayList.size() / 2 + 1)) / 2;
        }
        return arrayMean;
    }

    public static void logAllExposureData() {
        for (ExposureData exposureData: ExposureORM.getExposureData()) {
            Log.i(TAG, "Exp data number: " + exposureData.numberOf_data);
            Log.i(TAG, "Exp data start time: " + exposureData.exposure_start);
            Log.i(TAG, "Exp data id: " + exposureData.unique_id);
        }
    }

    public static void logExposureData(ArrayList<ExposureData> exposureDataArrayList) {
        for (ExposureData exposureData: exposureDataArrayList) {
            Log.i(TAG, " Exposure Beacon: " + exposureData.beaconId +
                    "  Exposure Mean Distance: " + exposureData.mean_distance +
                    "  Exposure Median Dist: " + exposureData.median_distance +
                    "  Exposure Max Dist: " + exposureData.max_distance +
                    "  Exposure Min Dist: " + exposureData.min_distance +
                    "  Exposure Mean RSSI: " + exposureData.mean_rssi +
                    "  Exposure Start: " + exposureData.exposure_start +
                    "  Exposure End: " + exposureData.exposure_end +
                    "  Exposure Total Data: " + exposureData.numberOf_data +
                    "  Exposure Avg TxPower: " + exposureData.mean_distance
            );
        }
    }

    public static void setUpCrashlyticsParameters(Context context) {
//        Crashlytics.setString(key, "foo" /* string value */);
//
//        Crashlytics.setBool(key, true /* boolean value */);
//
//        Crashlytics.setDouble(key, 1.0 /* double value */);
//
//        Crashlytics.setFloat(key, 1.0f /* float value */);
//
//        Crashlytics.setInt(key, 1 /* int value */);
        if (PrefUtil.isKeyExistInPref(context, Constants.PREF_USER_ID) && !PrefUtil.getString(context, Constants.PREF_USER_ID, "").isEmpty()) {
            Crashlytics.setUserIdentifier(PrefUtil.getString(context, Constants.PREF_USER_ID, ""));
        } else {
            Crashlytics.setUserIdentifier("UserID Not Present");
        }
    }

}
