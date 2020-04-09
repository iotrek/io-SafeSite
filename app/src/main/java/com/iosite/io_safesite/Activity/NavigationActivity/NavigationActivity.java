package com.iosite.io_safesite.Activity.NavigationActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iosite.io_safesite.Activity.NavigationActivity.Fragment.HomeFragment.HomeFragment;
import com.iosite.io_safesite.Activity.NavigationActivity.Fragment.MessagesFragment.MessagesFragment;
import com.iosite.io_safesite.Activity.NavigationActivity.Fragment.ProfileFragment.ProfileFragment;
import com.iosite.io_safesite.Network.GsonObjectRequest;
import com.iosite.io_safesite.Network.NetworkUpdateListener;
import com.iosite.io_safesite.Network.OnResponseReceived;
import com.iosite.io_safesite.Network.RequestManager;
import com.iosite.io_safesite.Pojo.UserProfile;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NavigationActivity extends AppCompatActivity {
    private BottomNavigationView mBottomNavigationView;
    private String TAG = "NavigationActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        if (savedInstanceState == null) {
//            TODO: load the default fragment
            loadHomeFragment();
        }
        init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    private void init() {
        Util.setUpCrashlyticsParameters(this);
        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();
        getUserProfileFromServer();
    }

    private void setupBottomNavigation() {

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // TODO: load relevant fragment
                        loadHomeFragment();
                        return true;
//                    case R.id.navigation_messages:
//                        // TODO: load relevant fragment
//                        loadMessagesFragment();
//                        return true;
                    case R.id.navigation_profile:
                        // TODO: load relevant fragment
                        loadProfileFragment();
                        return true;
                }
                return false;
            }
        });
    }

    private void loadHomeFragment() {
        HomeFragment homeFragment = new HomeFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, homeFragment,
                "homeFragment").disallowAddToBackStack().commit();
    }

    private void loadProfileFragment() {
        ProfileFragment profileFragment = new ProfileFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, profileFragment,
                "profileFragment").disallowAddToBackStack().commit();
    }

    private void loadMessagesFragment() {
        MessagesFragment messagesFragment = new MessagesFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, messagesFragment,
                "profileFragment").disallowAddToBackStack().commit();
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

    public boolean isQuarantineOver() {
        String qurantineSince = PrefUtil.getString(this, Constants.PREF_QUARANTINE_SINCE, "");
        if (!qurantineSince.isEmpty()) {
            Date quarantineDate = new Date();
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.format(currentDate);
            try {
                quarantineDate = dateFormat.parse(qurantineSince);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (quarantineDate != null) {
                long diff = currentDate.getTime() - quarantineDate.getTime();
                long numberOfDaysSinceQuarantine = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (numberOfDaysSinceQuarantine < 1 || numberOfDaysSinceQuarantine > 14) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void getUserProfileFromServer() {
        try {
            Util.clearCache(this);
            GsonObjectRequest saveUserProfileRequest = new GsonObjectRequest<>(this, Request.Method.GET,
                    Constants.REQUEST_GET_PROFILE,
                    Constants.REQUEST_GET_PROFILE_URL,
                    Util.getAuthRequestHeaders(this), null, UserProfile.class,
                    new NetworkUpdateListener(new OnResponseReceived() {
                        @Override
                        public void onRecieve(Object object) {
                            UserProfile response = (UserProfile) object;
                            if (response != null) {
                                saveProfileDataToPref(response);
                                Util.showToastMsg(NavigationActivity.this, "Profile fetched successfully.");
                            } else {
                                Util.showToastMsg(NavigationActivity.this, "Some error in fetching details");
                            }
                        }

                        @Override
                        public void onErrorRecive(VolleyError error, String customMsg, String header) {
                            if (error != null && error.networkResponse != null) {
                                Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                            } else {
                                Util.showToastMsg(NavigationActivity.this, "No response from server. Try again.");
                            }
                        }
                    }));
            RequestManager.addRequest(saveUserProfileRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProfileDataToPref(UserProfile userProfile) {
        if(userProfile.mobile != null) {
            PrefUtil.putString(this, Constants.PREF_CONTACT_NUMBER, userProfile.mobile);
        }
        if(userProfile.first_name != null) {
            PrefUtil.putString(this, Constants.PREF_FIRST_NAME, userProfile.first_name);
        }
        if(userProfile.last_name != null) {
            PrefUtil.putString(this, Constants.PREF_LAST_NAME, userProfile.last_name);
        }
        if(userProfile.age_group != null) {
            PrefUtil.putString(this, Constants.PREF_USER_AGE, userProfile.age_group);
        }

        if(userProfile.address.address1 != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_ONE, userProfile.address.address1);
        }
        if(userProfile.address.address2 != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_TWO, userProfile.address.address2);
        }
        if(userProfile.address.city != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_CITY, userProfile.address.city);
        }
        if(userProfile.address.state != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_STATE, userProfile.address.state);
        }

        if(userProfile.address.country != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_COUNTRY, userProfile.address.country);
        }

        if(String.valueOf(userProfile.address.pincode) != null) {
            PrefUtil.putString(this, Constants.PREF_ADDRESS_PINCODE, String.valueOf(userProfile.address.pincode));
        }

        if (userProfile.covid19_status != null) {
            PrefUtil.putString(this, Constants.PREF_COVID19_STATUS, userProfile.covid19_status);
        }
        if ("Quarantine".equalsIgnoreCase(userProfile.covid19_status) || "Infected".equalsIgnoreCase(userProfile.covid19_status)) {
            PrefUtil.putBoolean(this, Constants.PREF_QUARANTINE_STATUS, true);
        } else {
            PrefUtil.putBoolean(this, Constants.PREF_QUARANTINE_STATUS, false);
        }
        if(userProfile.quarantine_date != null) {
            String qurantineDate = userProfile.quarantine_date;
            Log.i(TAG, "qurantineDate: " + qurantineDate);
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Log.i(TAG, "PREF_QUARANTINE_SINCE: " + outputDateFormat.format((inputDateFormat.parse(qurantineDate))));
                PrefUtil.putString(this, Constants.PREF_QUARANTINE_SINCE, outputDateFormat.format((inputDateFormat.parse(qurantineDate))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        logProfileSavedToPref();

//        loadHomeFragment();
    }

    public void logProfileSavedToPref() {
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_CONTACT_NUMBER, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_FIRST_NAME, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_LAST_NAME, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_USER_AGE, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_ONE, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_TWO, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_CITY, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_STATE, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_COUNTRY, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_ADDRESS_PINCODE, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_COVID19_STATUS, ""));
        Log.i(TAG, PrefUtil.getString(this, Constants.PREF_QUARANTINE_SINCE, ""));
        Log.i(TAG, PrefUtil.getBoolean(this, Constants.PREF_QUARANTINE_STATUS, false) + "");
    }
}
