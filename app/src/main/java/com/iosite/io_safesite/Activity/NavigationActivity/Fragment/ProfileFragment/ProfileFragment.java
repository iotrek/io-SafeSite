package com.iosite.io_safesite.Activity.NavigationActivity.Fragment.ProfileFragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.iosite.io_safesite.Activity.NavigationActivity.NavigationActivity;
import com.iosite.io_safesite.Network.GsonObjectRequest;
import com.iosite.io_safesite.Network.NetworkUpdateListener;
import com.iosite.io_safesite.Network.OnResponseReceived;
import com.iosite.io_safesite.Network.RequestManager;
import com.iosite.io_safesite.Pojo.BaseResponseModel;
import com.iosite.io_safesite.Pojo.UserProfile;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private String TAG = "ProfileFragment";
    private EditText citySearchBar;
    private LinearLayout cityNamesLayout;
    private Spinner spinnerGenderDropdown;
    private Button submitProfileButton;
    private TextView contactNumber;
    private EditText userFirstName;
    private EditText userLastName;
    private EditText userAge;
    private EditText addressOne;
    private EditText addressTwo;
    private EditText addressCity;
    private EditText addressState;
    private EditText addressPinCode;
    private String userGender="";
    private EditText quarantineCalenderInput;
    private CalendarView calendarView;
    private NestedScrollView scrollViewLayout;
    private Switch quarantineSwitch;
    private LinearLayout quarantineInputLayout;
    private int totalNumberOfQuarantinedays = 14;
    private int millisInADay = 60*60*24*1000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
//        citySearchBar = view.findViewById(R.id.city_search_bar);
//        cityNamesLayout = view.findViewById(R.id.city_names_layout);
        spinnerGenderDropdown = view.findViewById(R.id.spinner_gender_dropdown);
        submitProfileButton = view.findViewById(R.id.submit_profile_button);
        submitProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserProfileToLocal();
                saveUserProfileToServer();
            }
        });
        contactNumber = view.findViewById(R.id.contact_number);
        userFirstName = view.findViewById(R.id.user_first_name);
        userLastName = view.findViewById(R.id.user_last_name);
        userAge = view.findViewById(R.id.user_age);
        addressOne = view.findViewById(R.id.address_1);
        addressTwo = view.findViewById(R.id.address_2);
        addressCity = view.findViewById(R.id.address_city);
        addressState = view.findViewById(R.id.address_state);
        addressPinCode = view.findViewById(R.id.address_pin_code);
        quarantineCalenderInput = view.findViewById(R.id.quarantine_calender_input);
        calendarView = view.findViewById(R.id.calendar_layout);
        calendarView.setVisibility(View.GONE);
        calendarView.setMaxDate(System.currentTimeMillis());
        calendarView.setMinDate(System.currentTimeMillis() - totalNumberOfQuarantinedays*millisInADay);
        calendarView.setVisibility(View.GONE);
        scrollViewLayout = view.findViewById(R.id.scroll_view_layout);
        quarantineSwitch = view.findViewById(R.id.quarantine_switch);
        quarantineInputLayout = view.findViewById(R.id.quarantine_input_layout);
        quarantineInputLayout.setVisibility(View.GONE);
        quarantineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                enableSaveButton();
                PrefUtil.putBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, b);
                if(b) {
                    quarantineInputLayout.setVisibility(View.VISIBLE);
                    PrefUtil.putString(getActivity(), Constants.PREF_COVID19_STATUS, "Quarantine");
                    scrollScreenToBottom();
                } else {
                    quarantineInputLayout.setVisibility(View.GONE);
                    PrefUtil.putString(getActivity(), Constants.PREF_COVID19_STATUS, "Safe");
                    calendarView.setVisibility(View.GONE);
                }
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
//                Toast.makeText(getActivity(), "" + i2, Toast.LENGTH_LONG).show();// TODO Auto-generated method stub
                Log.i(TAG, "Date selected: " +  i );
                Log.i(TAG, "Date selected: " +  i1 );
                Log.i(TAG, "Date selected: " +  i2 );
                showSelectedDate(i, i1, i2);

            }
        });

        setUpGenderSpinner();
        addEditTextChangeListeners();
        disableSaveButton();
        setUpCalenderIconClick();
        showProfileDataInUIFromPref();
    }

    private void setQuarantineStatusCheckBox() {
        if (PrefUtil.isKeyExistInPref(getActivity(), Constants.PREF_QUARANTINE_STATUS) &&
                PrefUtil.getBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, false)) {
            String qurantineSince = PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, "");
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
                    if (numberOfDaysSinceQuarantine > 14) {
                        PrefUtil.putBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, false);
                        PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, "");
                        quarantineSwitch.setEnabled(true);
                        quarantineSwitch.setChecked(false);
                    } else {
                        quarantineSwitch.setChecked(true);
                        quarantineSwitch.setEnabled(false);
                        calendarView.setVisibility(View.GONE);
                        quarantineInputLayout.setVisibility(View.GONE);
                    }
//                    numberOfDays.setText(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
                }

//                remainingDaysValue.setText(String.valueOf(14 - Integer.parseInt(numberOfDays.getText().toString())));
            }
        }
    }

    private void saveUserProfileToLocal() {
        PrefUtil.putString(getActivity(), Constants.PREF_FIRST_NAME, userFirstName.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_LAST_NAME, userLastName.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_USER_AGE, userAge.getText().toString());
//        PrefUtil.putString(getActivity(), Constants.PREF_USER_GENDER, spinnerGenderDropdown.getT);
        PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_ONE, addressOne.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_TWO, addressTwo.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_CITY, addressCity.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_STATE, addressState.getText().toString());
        PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_PINCODE, addressPinCode.getText().toString());
        if (quarantineSwitch.isEnabled()) {
            Log.i(TAG, "quarantineSwitch is isEnabled");
            PrefUtil.putString(getActivity(), Constants.PREF_QUARANTINE_SINCE, quarantineCalenderInput.getText().toString());
        }
        Log.i(TAG, "PREF_QUARANTINE_SINCE 11: " + PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, ""));
    }

    public void showProfileDataInUIFromPref() {
        contactNumber.setText(PrefUtil.getString(getActivity(), Constants.PREF_CONTACT_NUMBER, ""));
        userFirstName.setText(PrefUtil.getString(getActivity(), Constants.PREF_FIRST_NAME, ""));
        userLastName.setText(PrefUtil.getString(getActivity(), Constants.PREF_LAST_NAME, ""));
        userAge.setText(PrefUtil.getString(getActivity(), Constants.PREF_USER_AGE, ""));
        addressOne.setText(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_ONE, ""));
        addressTwo.setText(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_TWO, ""));
        addressCity.setText(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_CITY, ""));
        addressState.setText(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_STATE, ""));
        addressPinCode.setText(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_PINCODE, ""));
        if (((NavigationActivity)getActivity()).isQuarantineOver()) {
            PrefUtil.putBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, false);
            PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, "");
            quarantineSwitch.setEnabled(true);
            quarantineSwitch.setChecked(false);
        } else {
            quarantineSwitch.setChecked(true);
            quarantineSwitch.setEnabled(false);
            calendarView.setVisibility(View.GONE);
            quarantineInputLayout.setVisibility(View.GONE);
            disableSaveButton();
        }
//        setQuarantineStatusCheckBox();
    }

    private void showSelectedDate(int year, int month, int dayOfMonth) {
        month = month+1;
        String dayOfMonthString = "" + dayOfMonth;
        String monthString = "" + month;
        if (dayOfMonth < 10) {
            dayOfMonthString = "0" + dayOfMonth;
        }
        if(month < 10) {
            monthString = "0" + month;
        }

        calendarView.setVisibility(View.GONE);
        quarantineCalenderInput.setText(dayOfMonthString + "/" + monthString + "/" + year);
    }

    private void setUpGenderSpinner() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gender_options_array, android.R.layout.simple_spinner_dropdown_item);

//        String[] items = new String[] { "Male", "Female" };
//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_spinner_dropdown_item, items);
        spinnerGenderDropdown.setAdapter(adapter);
        spinnerGenderDropdown.setOnItemSelectedListener(this);
//        spinnerGenderDropdown.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                Log.i(TAG, "setOnTouchListener event");
//                Log.i(TAG, "setOnTouchListener event: ");
//                return false;
//            }
//        });
//        spinnerGenderDropdown.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                Log.i(TAG, "setOnKeyListener event");
//                Log.i(TAG, "setOnKeyListener event: " + adapter.getItem(i));
//                return false;
//            }
//        });
//        spinnerGenderDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//                Log.v(TAG, "item: " + (String) parent.getItemAtPosition(position));
////                userGender = (String) parent.getItemAtPosition(position);
//                PrefUtil.putString(getActivity(), Constants.PREF_USER_GENDER, (String) parent.getItemAtPosition(position));
//                enableSaveButton();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//            }
//        });

        if (PrefUtil.isKeyExistInPref(getActivity(), Constants.PREF_USER_GENDER) && !PrefUtil.getString(getActivity(), Constants.PREF_USER_GENDER, "").isEmpty()) {
//            Arrays.asList(R.array.gender_options_array).indexOf(PrefUtil.getString(getActivity(), Constants.PREF_USER_GENDER, ""));
            spinnerGenderDropdown.setSelection(Arrays.asList(R.array.gender_options_array).indexOf(PrefUtil.getString(getActivity(), Constants.PREF_USER_GENDER, "")));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpCalenderIconClick() {
        quarantineCalenderInput.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (quarantineCalenderInput.getRight() - quarantineCalenderInput.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    Log.i(TAG, "Right Drawable");
                    calendarView.setVisibility(View.VISIBLE);
                    scrollScreenToBottom();
                    return true;
                }
            }
            return false;
        }
    });
    }

    private void scrollScreenToBottom() {
        scrollViewLayout.post(new Runnable() {
            public void run() {
                scrollViewLayout.fullScroll(scrollViewLayout.FOCUS_DOWN);
            }
        });
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private void editTextDrawable() {
//        citySearchBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_RIGHT = 2;
//
//                if(event.getAction() == MotionEvent.ACTION_UP) {
//                    if(event.getRawX() >= (citySearchBar.getRight() - citySearchBar.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        Log.i(TAG, "Right Drawable");
//                        saveEnteredCityName(citySearchBar.getText().toString());
//                        return true;
//                    }
//                }
//
//                if(event.getAction() == MotionEvent.ACTION_UP) {
//                    if(event.getRawX() <= (citySearchBar.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
//                        // your action here
//                        Log.i(TAG, "Left Drawable");
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//    }

//    private void addCityNamesToLayout(String cityName) {
//
////        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),"font/open_sans_semibold.ttf");
//        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.open_sans_semibold);
//        int paddingDp = 10;
//        float density = getActivity().getResources().getDisplayMetrics().density;
//        int paddingPixel = (int)(paddingDp * density);
//
//        TextView valueTV = new TextView(getActivity());
//        valueTV.setText(cityName);
//////        valueTV.setId(5);
////        valueTV.setLayoutParams(new LinearLayout.LayoutParams(
////                LinearLayout.LayoutParams.WRAP_CONTENT,
////                LinearLayout.LayoutParams.WRAP_CONTENT));
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(0, 8, 0, 0);
//        valueTV.setTextColor(Color.parseColor("#131415"));
//        valueTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//        valueTV.setTypeface(typeface);
//        valueTV.setGravity(Gravity.CENTER);
//        valueTV.setPadding(paddingPixel,paddingPixel-2,paddingPixel,paddingPixel-2);
//        valueTV.setBackgroundResource(R.drawable.button_with_round_corner_transparent);
//        ((LinearLayout) cityNamesLayout).addView(valueTV);
//    }

//    private void saveEnteredCityName(String cityName) {
//        addCityNamesToLayout(cityName);
//    }

    private void disableSaveButton() {

        submitProfileButton.setEnabled(false);
        submitProfileButton.setBackgroundResource(R.drawable.button_with_round_corner);
    }

    private void enableSaveButton() {
        submitProfileButton.setEnabled(true);
        submitProfileButton.setBackgroundResource(R.drawable.button_with_round_corner_enabled);
    }

    private void addEditTextChangeListeners() {
        userFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_FIRST_NAME, userFirstName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_LAST_NAME, userLastName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_USER_AGE, userAge.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_ONE, addressOne.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_TWO, addressTwo.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_CITY, addressCity.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_STATE, addressState.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressPinCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_ADDRESS_PINCODE, addressPinCode.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        quarantineCalenderInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableSaveButton();
//                PrefUtil.putString(getActivity(), Constants.PREF_QUARANTINE_SINCE, quarantineCalenderInput.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void saveUserProfileToServer() {
        Log.i(TAG, "saveUserProfileToServer");
        try {
            Util.clearCache(getActivity());
            GsonObjectRequest saveUserProfileRequest = new GsonObjectRequest<>(getActivity(), Request.Method.PUT, Constants.REQUEST_UPDATE_PROFILE,
                    Constants.REQUEST_UPDATE_PROFILE_URL + "/" + PrefUtil.getString(getActivity(), Constants.PREF_USER_ID, ""),
                    Util.getAuthRequestHeaders(getActivity()), getSaveUserProfileBody(), BaseResponseModel.class,
                    new NetworkUpdateListener(new OnResponseReceived() {
                        @Override
                        public void onRecieve(Object object) {
                            BaseResponseModel response = (BaseResponseModel) object;
                            if (response != null && response.msg != null) {
                                Log.i(TAG, "Profile Saved. Response: " + response.msg);
                                disableSaveButton();
                                Util.showToastMsg(getActivity(), "Profile Saved successfully.");
                            } else {
                                Util.showToastMsg(getActivity(), "Some error in fetching details");
                            }
                        }

                        @Override
                        public void onErrorRecive(VolleyError error, String customMsg, String header) {
                            if (error != null && error.networkResponse != null) {
                                Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                            } else {
                                Util.showToastMsg(getActivity(), "No response from server. Try again.");
                            }
                        }
                    }));
            RequestManager.addRequest(saveUserProfileRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserProfileFromServer() {
        try {
            Util.clearCache(getActivity());
            GsonObjectRequest saveUserProfileRequest = new GsonObjectRequest<>(getActivity(), Request.Method.GET,
                    Constants.REQUEST_GET_PROFILE,
                    Constants.REQUEST_GET_PROFILE_URL,
                    Util.getAuthRequestHeaders(getActivity()), null, UserProfile.class,
                    new NetworkUpdateListener(new OnResponseReceived() {
                        @Override
                        public void onRecieve(Object object) {
                            UserProfile response = (UserProfile) object;
                            if (response != null) {
                                showProfileDataInUIFromPref();
                                Util.showToastMsg(getActivity(), "Profile fetched successfully.");
                            } else {
                                Util.showToastMsg(getActivity(), "Some error in fetching details");
                            }
                        }

                        @Override
                        public void onErrorRecive(VolleyError error, String customMsg, String header) {
                            if (error != null && error.networkResponse != null) {
                                Log.e(TAG, "networkResponseCode; " + error.networkResponse.statusCode);
                            } else {
                                Util.showToastMsg(getActivity(), "No response from server. Try again.");
                            }
                        }
                    }));
            RequestManager.addRequest(saveUserProfileRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSaveUserProfileBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_FIRST_NAME, PrefUtil.getString(getActivity(), Constants.PREF_FIRST_NAME, ""));// userFirstName.getText().toString());
            jsonObject.put(Constants.PARAM_LAST_NAME, PrefUtil.getString(getActivity(), Constants.PARAM_LAST_NAME, ""));
            jsonObject.put(Constants.PARAM_USER_GENDER, PrefUtil.getString(getActivity(), Constants.PREF_USER_GENDER, ""));
            jsonObject.put(Constants.PARAM_USER_AGE, PrefUtil.getString(getActivity(), Constants.PREF_USER_AGE, ""));
//            jsonObject.put(Constants.PARAM_QUARANTINE_SINCE, PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, ""));
            jsonObject.put(Constants.PARAM_COVID19_STATUS, PrefUtil.getString(getActivity(), Constants.PREF_COVID19_STATUS, ""));
            jsonObject.put(Constants.PARAM_QUARANTINE_DATE, PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, ""));

            JSONObject jsonAddress = new JSONObject();
            jsonAddress.put(Constants.PARAM_ADDRESS_ONE, PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_ONE, ""));
            jsonAddress.put(Constants.PARAM_ADDRESS_TWO, PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_TWO, ""));
            jsonAddress.put(Constants.PARAM_ADDRESS_CITY, PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_CITY, ""));
            jsonAddress.put(Constants.PARAM_ADDRESS_STATE, PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_STATE, ""));
            jsonAddress.put(Constants.PARAM_ADDRESS_COUNTRY, "India");
            jsonAddress.put(Constants.PARAM_ADDRESS_PINCODE, Integer.parseInt(PrefUtil.getString(getActivity(), Constants.PREF_ADDRESS_PINCODE, "")));
            jsonObject.put(Constants.PARAM_ADDRESS, jsonAddress);
//
//            JSONArray jsonArrayTravelHistory = new JSONArray();
//            JSONObject jsonTravelHistory = new JSONObject();
//            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_CITY, "Gurgaon");
//            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_STATE, "Haryana");
//            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_COUNTRY, "IN");
//            jsonTravelHistory.put(Constants.PARAM_TRAVEL_HISTORY_DATE, "4th March");
//            jsonArrayTravelHistory.put(jsonTravelHistory);
//            jsonObject.put(Constants.PARAM_TRAVEL_HISTORY, jsonArrayTravelHistory);
            Log.i(TAG, jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.v(TAG, "item: " + (String) adapterView.getItemAtPosition(i));
//                userGender = (String) parent.getItemAtPosition(position);
        PrefUtil.putString(getActivity(), Constants.PREF_USER_GENDER, (String) adapterView.getItemAtPosition(i));
        enableSaveButton();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
