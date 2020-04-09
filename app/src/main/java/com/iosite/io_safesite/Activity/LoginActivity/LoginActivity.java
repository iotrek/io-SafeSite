package com.iosite.io_safesite.Activity.LoginActivity;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.iosite.io_safesite.Activity.LoginActivity.ConsentFragment.ConsentFragment;
import com.iosite.io_safesite.Activity.LoginActivity.MobileFragment.MobileFragment;
import com.iosite.io_safesite.Activity.LoginActivity.OTPFragment.OTPFragment;
import com.iosite.io_safesite.Activity.LoginActivity.PermissionFragment.PermissionFragment;
import com.iosite.io_safesite.Activity.NavigationActivity.NavigationActivity;
import com.iosite.io_safesite.Network.GsonObjectRequest;
import com.iosite.io_safesite.Network.NetworkUpdateListener;
import com.iosite.io_safesite.Network.OnResponseReceived;
import com.iosite.io_safesite.Network.RequestManager;
import com.iosite.io_safesite.Pojo.BaseResponseModel;
import com.iosite.io_safesite.Pojo.ValidateOTPResponse;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Services.SendForeGroundService;
import com.iosite.io_safesite.Util.BLETransmitter;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";
    private String phoneNumber;
    private String otp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void init() {
        showMobileFragment();
    }

    public void savePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void saveOtp(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void generateOTP() {
        try {
            if (Util.isInternetConnected(this)) {
                Util.clearCache(this);
                GsonObjectRequest getOtpRequest = new GsonObjectRequest<>(this, Request.Method.POST, Constants.REQUEST_GENERATE_OTP,
                        Constants.REQUEST_GENERATE_OTP_URL, Util.setContentType(), getGenerateOTPBody(), BaseResponseModel.class,
                        new NetworkUpdateListener(new OnResponseReceived() {
                            @Override
                            public void onRecieve(Object object) {
                                Log.i(TAG, "get OTP suuccess");
                                BaseResponseModel response = (BaseResponseModel) object;
                                if (response != null && response.msg != null) {
                                    Log.i(TAG, "generate OTP success response: " + response.msg);
                                    Util.showToastMsg(LoginActivity.this, "OTP sent successfully.");
                                    showOTPFragment();

                                } else if (response != null && response.error != null) {
                                    Log.i(TAG, "generate OTP failure response: " + response.error);
                                    Util.showToastMsg(LoginActivity.this, "Some error in getting OTP. Try again.");
                                }
                            }

                            @Override
                            public void onErrorRecive(VolleyError error, String customMsg, String header) {
                                Util.isInternetConnectedOnChange(error, customMsg);
                                Log.i(TAG, "Error: " + error);
                                Log.i(TAG, "customMsg" + customMsg);
                                if (error != null && error.networkResponse != null) {
                                    Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                                } else {
                                    Util.showToastMsg(LoginActivity.this, "No response from server. Try again.");
                                }
                            }
                        }));
                RequestManager.addRequest(getOtpRequest);
            } else {
                Log.e(TAG, "Internet Not connected");
                Util.showToastMsg(LoginActivity.this, "Internet not connected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validateOTP() {
        try {
            Util.clearCache(this);
            GsonObjectRequest validateOTPRequest = new GsonObjectRequest<>(this, Request.Method.PUT, Constants.REQUEST_VALIDATE_OTP,
                    Constants.REQUEST_VALIDATE_OTP_URL, Util.setContentType(), getValidateOTPBody(), ValidateOTPResponse.class,
                    new NetworkUpdateListener(new OnResponseReceived() {
                        @Override
                        public void onRecieve(Object object) {
                            Log.i(TAG, "validate OTP suuccess");
                            ValidateOTPResponse response = (ValidateOTPResponse) object;
                            if (response != null && response.user_id != null && response.token != null) {
                                Log.i(TAG, "user_id: " + response.user_id);
                                Log.i(TAG, "token: " + response.token);
                                PrefUtil.putString(LoginActivity.this, Constants.PREF_USER_ACCESS_TOKEN, response.token);
                                PrefUtil.putString(LoginActivity.this, Constants.PREF_USER_ID, response.user_id);
                                PrefUtil.putString(LoginActivity.this, Constants.PREF_USER_BEACON_ID, Constants.iositeBeaconID + response.user_id);
                                Log.i(TAG, "PREF_USER_BEACON_ID: " + PrefUtil.getString(LoginActivity.this, Constants.PREF_USER_BEACON_ID,  ""));
                                Util.showToastMsg(LoginActivity.this, "OTP Validated");
//                                showNextActivity();
                                showPermissionFragment();
                            } else {
//                                setEnterMobileNumberView();
                                Util.showToastMsg(LoginActivity.this, "Some error in fetching details");
                            }
                        }

                        @Override
                        public void onErrorRecive(VolleyError error, String customMsg, String header) {
//                            setEnterOTPView();
                            if (error != null && error.networkResponse != null) {
                                Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                                sendResponseCodeToFragment(error.networkResponse.statusCode);
                                if(error.networkResponse.statusCode == 400) {
                                    // TODO: Invalid OTP. Try again
                                }
                                if(error.networkResponse.statusCode == 404) {
                                    //TODO: OTP Expired. Click Resend
                                }

                            } else {
                                Util.showToastMsg(LoginActivity.this, "No response from server. Try again.");
                            }
                        }
                    }));
            RequestManager.addRequest(validateOTPRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getGenerateOTPBody() {
        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put(Constants.PARAM_MOBILE, Integer.valueOf(mobileNumber.getText().toString()));
            jsonObject.put(Constants.PARAM_MOBILE, phoneNumber);
            Log.i(TAG, jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();

    }

    private String getValidateOTPBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_MOBILE, phoneNumber);
            jsonObject.put(Constants.PARAM_OTP, otp);
            Log.i(TAG, jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();
    }


    public void bleInitialCheck() {
        BLETransmitter.setupBeacon(this);
        if (BLETransmitter.getBlueToothOn()) {
            Log.i(TAG, "isBlueToothOn");
            showNextActivity();
        } else if (!BLETransmitter.isBluetoothLEAvailable(this)) {
            Util.showErrorMessage(this, "Bluetooth not available on your device");
        } else {
            Log.i(TAG, "BlueTooth is off");
            Util.showInformationMessage(this, "Enable Bluetooth", "Please enable Bluetooth. It is needed for the app to function properly..",
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            transmitIBeacon();
            if (gpsOnCheck()) {
                showNextActivity();
            } else {
                showGpsOnDialog();
            }
//            showNextActivity();
        } else {
            Log.e(TAG, "result not ok");
        }
    }

    public void showNextActivity() {
        setSendDataService();
        Util.sendFCMToIosite(this);
        Intent myIntent = null;

        PrefUtil.putBoolean(this, Constants.PREF_IS_FIRST_LOGIN, true);
        myIntent = new Intent(LoginActivity.this, NavigationActivity.class);
        startActivity(myIntent);
        finish();
    }



    public void showMobileFragment() {
        removeOneFragment();
        MobileFragment mobileFragment = new MobileFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, mobileFragment,
                "mobileFragment").addToBackStack("mobileFragment").commit();
    }

    private void showOTPFragment() {
        removeOneFragment();
        OTPFragment otpFragment = new OTPFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, otpFragment,
                "otpFragment").addToBackStack("otpFragment").commit();

    }
    private void showPermissionFragment() {
        removeOneFragment();
        PermissionFragment permissionFragment = new PermissionFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, permissionFragment,
                "permissionFragment").addToBackStack("permissionFragment").commit();
    }

    private void showConsentFragment() {
        removeOneFragment();
        ConsentFragment consentFragment = new ConsentFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, consentFragment,
                "consentFragment").addToBackStack("consentFragment").commit();
    }

    public void removeOneFragment() {
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    public void removeFragments() {
        do {
            getFragmentManager().popBackStackImmediate();
        }
        while (getFragmentManager().getBackStackEntryCount() > 0);

    }

    private void sendResponseCodeToFragment(int responseCode) {
        OTPFragment otpFragment = (OTPFragment) getFragmentManager().findFragmentByTag("otpFragment");
        otpFragment.getValidateOtpResponse(responseCode);
//        OnetoOneChatFragment fragment = (OnetoOneChatFragment) getFragmentManager().findFragmentByTag("ChatWindow");
    }

    public void getPermissions() {
        // check if the user has gps permission. if not, then ask for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSION_CODE);
        } else
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
        } else {
            Log.i(TAG, "All permisisons exist");
//            showNextActivity();
            showConsentFragment();
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
                    Log.i(TAG, "All permisisons granted");
//                    showNextActivity();
                    showConsentFragment();
                }

            }
        } else if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "All permisisons granted");
//                showNextActivity();
                showConsentFragment();
            }
        }
    }

    private void setSendDataService() {
        SendForeGroundService mSensorService = new SendForeGroundService(LoginActivity.this);
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
}
