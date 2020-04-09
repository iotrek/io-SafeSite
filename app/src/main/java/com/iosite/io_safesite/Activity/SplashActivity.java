package com.iosite.io_safesite.Activity;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iosite.io_safesite.Activity.LoginActivity.LoginActivity;
import com.iosite.io_safesite.Activity.NavigationActivity.NavigationActivity;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Services.SendForeGroundService;
import com.iosite.io_safesite.Util.BLETransmitter;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "SplashActivity";
    private Button beginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        setContentView(R.layout.activity_splash);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
//        getPermissions();
//        setSendDataService();
        init();
    }

    private void getPermissions() {
        // check if the user has gps permission. if not, then ask for permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSION_CODE);
//        } else
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
        } else {
            init();
        }
    }

    public void init() {
        Util.setUpCrashlyticsParameters(this);
//        Crashlytics.getInstance().crash(); // Force a crash
        beginButton = findViewById(R.id.begin_button);
        if(isUserValidated()) {
            bleInitialCheck(isUserValidated());
//            gpsOnCheck();
//            showNextActivity(isUserValidated());
        } else {
            beginButton.setOnClickListener(this);
        }
    }

    private boolean isUserValidated() {
        if(PrefUtil.isKeyExistInPref(this, Constants.PREF_USER_ACCESS_TOKEN)
                && !PrefUtil.getString(this, Constants.PREF_USER_ACCESS_TOKEN, "").isEmpty())
        {
            return true;
        } else {
            return false;
        }
    }


    private void setSendDataService() {
        SendForeGroundService mSensorService = new SendForeGroundService(SplashActivity.this);
        Intent mServiceIntent = new Intent(this, mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.e(TAG, "Service running from if");
                startForegroundService(mServiceIntent);
            } else {
                Log.e(TAG, "Service running from else");
                startService(mServiceIntent);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e(TAG, "Service running");
                return true;
            }
        }
        Log.e(TAG, "Service NOT running");
        return false;
    }

    public void showNextActivity(boolean isValidated) {
        Intent myIntent = null;
        PrefUtil.putBoolean(this, Constants.PREF_IS_FIRST_LOGIN, !isValidated);
        if (isValidated) {
            setSendDataService();
            Util.sendFCMToIosite(this);
//            PrefUtil.putBoolean(this, Constants.PREF_IS_FIRST_LOGIN, false);
            myIntent = new Intent(SplashActivity.this, NavigationActivity.class);
        } else {
            myIntent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(myIntent);
        finish();
    }

    public void showGpsOnDialog() {
        Log.i(TAG, "GPS is OFF");
        Util.showInformationMessage(this, "Enable GPS", "Please enable GPS. It is needed for the app to function properly.",
                false, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 2);
                        }
                    }
                });
    }


    public void bleInitialCheck(boolean isUserValidated) {
        BLETransmitter.setupBeacon(this);
        if (BLETransmitter.getBlueToothOn()) {
            Log.i(TAG, "isBlueToothOn");
            if (gpsOnCheck()) {
                showNextActivity(isUserValidated);
            } else {
                showGpsOnDialog();
            }
        } else if (!BLETransmitter.isBluetoothLEAvailable(this)) {
            Util.showErrorMessage(this, "Bluetooth not available on your device");
        } else {
            Log.i(TAG, "BlueTooth is off");
            Util.showInformationMessage(this, "Enable Bluetooth", "Please enable Bluetooth. It is needed for the app to function properly.",
                    false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == DialogInterface.BUTTON_POSITIVE) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, 1);
                            }
                        }
                    });
        }
    }

    public boolean gpsOnCheck() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check if enabled and if not send user to the GPS settings
//        if (!isGpsenabled || !isNetworkEnabled) {
        if (!isNetworkEnabled) {
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            transmitIBeacon();
//            gpsOnCheck();
            if (gpsOnCheck()) {
                showNextActivity(true);
            } else {
                showGpsOnDialog();
            }
        } else {
            Log.e(TAG, "result not ok");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                } else {
                    init();
                }

            }
        } else if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.begin_button:
                showNextActivity(false);
                break;
            default:
                break;
        }
    }
}
