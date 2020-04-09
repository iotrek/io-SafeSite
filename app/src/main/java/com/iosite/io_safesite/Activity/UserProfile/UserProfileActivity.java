package com.iosite.io_safesite.Activity.UserProfile;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.iosite.io_safesite.Network.GsonObjectRequest;
import com.iosite.io_safesite.Network.NetworkUpdateListener;
import com.iosite.io_safesite.Network.OnResponseReceived;
import com.iosite.io_safesite.Network.RequestManager;
import com.iosite.io_safesite.Pojo.BaseResponseModel;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserProfileActivity extends AppCompatActivity {

    private String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        init();
    }

    private void init() {
        saveUserProfile();
    }

    private void saveUserProfile() {
        try {
            Util.clearCache(this);
            GsonObjectRequest saveUserProfileRequest = new GsonObjectRequest<>(this, Request.Method.PUT, Constants.REQUEST_UPDATE_PROFILE,
                    Constants.REQUEST_UPDATE_PROFILE_URL + "/" + PrefUtil.getString(this, Constants.PREF_USER_ID, ""),
                        Util.getAuthRequestHeaders(this), getsaveUserProfileBody(), BaseResponseModel.class,
                    new NetworkUpdateListener(new OnResponseReceived() {
                        @Override
                        public void onRecieve(Object object) {
                            BaseResponseModel response = (BaseResponseModel) object;
                            if (response != null && response.msg != null) {
                                Log.i(TAG, "Profile Saved. Response: " + response.msg);
                            } else {
                                Util.showToastMsg(UserProfileActivity.this, "Some error in fetching details");
                            }
                        }

                        @Override
                        public void onErrorRecive(VolleyError error, String customMsg, String header) {
                            if (error != null && error.networkResponse != null) {
                                Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                            } else {
                                Util.showToastMsg(UserProfileActivity.this, "No response from server. Try again.");
                            }
                        }
                    }));
            RequestManager.addRequest(saveUserProfileRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getsaveUserProfileBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_FIRST_NAME, "Kamran");
            jsonObject.put(Constants.PARAM_LAST_NAME, "Alam");
            jsonObject.put(Constants.PARAM_USER_GENDER, "Male");
            jsonObject.put(Constants.PARAM_USER_AGE, "20-30");
            jsonObject.put(Constants.PARAM_COVID19_STATUS, "Safe");

            JSONObject jsonObjectAddress = new JSONObject();
            JSONObject jsonAddress = new JSONObject();
            jsonAddress.put(Constants.PARAM_ADDRESS_ONE, "Cleo County");
            jsonAddress.put(Constants.PARAM_ADDRESS_TWO, "GBN");
            jsonAddress.put(Constants.PARAM_ADDRESS_CITY, "Noida");
            jsonAddress.put(Constants.PARAM_ADDRESS_STATE, "UP");
            jsonAddress.put(Constants.PARAM_ADDRESS_COUNTRY, "IN");
            jsonAddress.put(Constants.PARAM_ADDRESS_PINCODE, "201307");
            jsonObjectAddress.put(Constants.PARAM_ADDRESS, jsonAddress);
            jsonObject.put(Constants.PARAM_ADDRESS, jsonObjectAddress);

            JSONArray jsonArrayTravelHistory = new JSONArray();
            JSONObject jsonTravelHistory = new JSONObject();
            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_CITY, "Gurgaon");
            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_STATE, "Haryana");
            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_COUNTRY, "IN");
            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_DATE, "4th March");
            jsonArrayTravelHistory.put(jsonTravelHistory);
            jsonObject.put(Constants.PARAM_TRAVEL_HISTORY, jsonArrayTravelHistory);
            Log.i(TAG, jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();
    }
}
