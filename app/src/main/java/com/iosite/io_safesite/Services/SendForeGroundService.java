package com.iosite.io_safesite.Services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.iosite.io_safesite.Activity.SplashActivity;
import com.iosite.io_safesite.BroadCastReceiver.GPSEventReceiver;
import com.iosite.io_safesite.BroadCastReceiver.NetworkReceiver;
import com.iosite.io_safesite.Pojo.IpLocationModel;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.BLEScannerOne;
import com.iosite.io_safesite.Util.BLETransmitter;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.LocationUtil;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.interfaces.OnLocationFinderListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SendForeGroundService extends Service implements
        OnLocationFinderListener {

    private int DSMFreq = 1;  // in minutes
    private int openFCMFreq = 3;  // in minutes

    private String TAG = "SendForeGround"; // separate tags in different classes for error identification.
    public Context mContext;
    public static final String CHANNEL_ID_FOREGROUND = "ForegroundServiceChannel";
    private LocationManager mLocationManager;
    public GoogleApiClient mGoogleApiClient;

    private Timer timer;
    private TimerTask timerTask;
    private Timer timerBLE;
    private TimerTask timerTaskBLE;
    private Timer openFCMTimer;
    private Timer timerStartTransmission;
    private Timer timerStopTransmission;
    private TimerTask timerStartTransmissionTask;
    private TimerTask timerStopTransmissionTask;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;


    private boolean is_scanning;
    private BeaconParser beaconParser;
    private BeaconTransmitter beaconTransmitter;

    private int BLE_duration = 50;   // in seconds
    int BLE_duration_MAX_ONE = 70;  // in seconds
    int BLE_duration_MIN_ONE = 30;   // in seconds
    int BLE_duration_MAX_TWO = 20;  // in seconds
    int BLE_duration_MIN_TWO = 0;   // in seconds

    private Beacon beacon;

    /* variables for fetchig location */
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Location location;

    public SendForeGroundService(Context context) {
        super();
        mContext = context;
    }


    public SendForeGroundService() {

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "sensorActivity onCreate");
        //TODO :: NEED TO RUN SERVICE PERIODICALLY
        super.onCreate();
        init();
    }


    private void init() {

//        BLEScanner.initScanner(mContext);
        BLEScannerOne.initScanner(this);

        setUpTransmissionTimers();
        BLETransmitter.setupBeacon(this);

        IntentFilter gpsIntentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        gpsIntentFilter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        GPSEventReceiver gpsEventReceiver = new GPSEventReceiver();
        this.registerReceiver(gpsEventReceiver, gpsIntentFilter);

        IntentFilter connectivityFilter = new IntentFilter();
        connectivityFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        NetworkReceiver receiver = new NetworkReceiver();
        this.registerReceiver(receiver, connectivityFilter);
    }


    private boolean isBeaconSupported() {
        int result = BeaconTransmitter.checkTransmissionSupported(this);
        if(result == BeaconTransmitter.SUPPORTED) {
            Log.i(TAG, "beaconsupported");
            return true;
        }
        else {
            Log.i(TAG, "not beacon supported");
            return false;
        }
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "sensorActivity onDestroy");
        stoptimertask();

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "sensorActivity onStartCommand");
        super.onStartCommand(intent, flags, startId);
        Intent notificationIntent;
        notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
                        .setSmallIcon(R.drawable.common_google_signin_btn_text_light)
                        .setContentTitle("CoShield")
                        .setContentText("Be Safe !.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setColor(Color.BLUE)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(Integer.parseInt("9999"));  // clear out previous notifications

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_FOREGROUND,
                    "App Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(Integer.parseInt("9999"),
                notificationBuilder.build());


        startForeground(1, notificationBuilder.build());
        //setLocationListeners();
        setSensor();
        return START_STICKY;
    }

    public void setSensor() {
        Log.i(TAG, "SensorActivity setSensor called.");
    }

    @SuppressLint("NewApi")
    private void sendLocationData(Location location, Context context) {
        JSONObject jsonObject = new JSONObject();
        String userInOutState = "check_in";
        if (PrefUtil.getBoolean(context, Constants.PREF_IS_CHECKED_IN, true)) {
            userInOutState = "check_in";
        } else {
            userInOutState = "check_out";
        }
        try {
            jsonObject.put(Constants.PARAM_TYPE, userInOutState);
            jsonObject.put(Constants.PARAM_LAT, location.getLatitude());
            jsonObject.put(Constants.PARAM_LONG, location.getLongitude());
            Log.i(TAG, "Kamran: " + jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        SendSensor sendSensor = new SendSensor(context);
//        sendSensor.sendLocationUplink(jsonObject);
    }


    public void setUpTransmissionTimers() {
        timerStartTransmission = new Timer();
        timerStopTransmission = new Timer();
//        timerToStartTransmission();
        timerToStopTransmission();   // start with scanning first
    }

    public int getNewBLEDuration() {
        return new Random().nextInt((BLE_duration_MAX_ONE - BLE_duration_MIN_ONE) + 1) + BLE_duration_MIN_ONE;
    }

    public void timerToStartTransmission() {
        taskTimerStartTransmission();
        timerStartTransmission.schedule(timerStartTransmissionTask, 1000*getNewBLEDuration());
    }

    public void timerToStopTransmission() {
        taskTimerStopTransmission();
        timerStopTransmission.schedule(timerStopTransmissionTask, 1000*getNewBLEDuration());
    }

    public void taskTimerStartTransmission() {
        timerStartTransmissionTask = new TimerTask() {
            @Override
            public void run() {
//                startBeaconTransmission();
//                transmitIBeacon();
//                Log.e(TAG, "Start Transmit. Stop Scan");
                Log.e(TAG, "Transmit. Stop Scan");
                if (!BLETransmitter.isBLETransmitting()) {
                    Log.i(TAG, "BLE NOT Transmitting");
                    BLETransmitter.setupBeacon(mContext);
                }
//                BLEScanner.stopScan(mContext);
//                BLEScannerOne.stopBLEScan(mContext);
//                BLETransmitter.transmitIBeacon(mContext, true);
                timerToStopTransmission();

            }
        };
    }


    public void taskTimerStopTransmission() {
        timerStopTransmissionTask = new TimerTask() {
            @Override
            public void run() {
//                Log.e(TAG, "Stop Transmit. Start Scan");
                Log.e(TAG, "Transmit. Start Scan");
                if (!BLEScannerOne.isBLEScanning()) {
                    Log.i(TAG, "BLE NOT Scanning");
                    BLEScannerOne.initScanner(mContext);
                }
//                BLETransmitter.transmitIBeacon(mContext, false);
//                BLETransmitter.transmitIBeacon(mContext, true);
//                BLEScannerOne.startBLEScan(mContext);
//                BLEScanner.startScan(mContext);
                timerToStartTransmission();
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if(openFCMTimer != null) {
            openFCMTimer.cancel();
            openFCMTimer = null;
        }

        if(timerBLE != null) {
            timerBLE.cancel();
            timerBLE = null;
        }
    }

    public void sendUserLocation() {
        if (LocationUtil.checkForLocationPermission(mContext)) {
            Log.d(TAG, "Location permission exists");
            LocationUtil.getCurrentLatLongGPS(1, mLocationManager, mContext, this);
        } else {
            Log.d(TAG, "Location permission NOT exists");
        }
    }


    @Override
    public void onPermissionGranted(int type) {
    }

    @Override
    public void onPermissionDenied(int type) {
        //show error message or re require message
    }

    @Override
    public void onProviderDisabled(int type) {
        //get user last location
        // TODO: call getUserLastLocation
        Log.i(TAG, "provider disabled");
        LocationUtil.getUserLastLocation(mContext, mGoogleApiClient, this, 1);
    }

    @Override
    public void onLocationCallBackResult(LocationResult locationResult, int type) {
        if (locationResult != null && locationResult.getLastLocation() != null) {
            //get current lat long here
            Log.i(TAG, "location fetched");
//            sendLocationData(locationResult.getLastLocation(), mContext);   // testing
            Log.i(TAG, "latitude: " + locationResult.getLastLocation().getLatitude() + "Logitude: " + locationResult.getLastLocation().getLongitude());

        }
    }

    @Override
    public void onLastLocationUpdate(Location location) {
        if (location != null) {
            // TODO: send location with checkIn ID
            Log.i(TAG, "got last location");
        } else {
            //Show error unable to find location
            Log.i(TAG, "location  not found ");
            //TODO: show error on locaiton not found
        }
    }

    @Override
    public void onLocationUpdateViaIp(IpLocationModel.LocationModel locationModel) {

    }


    @Override
    public void onLocationError() {

    }

}
