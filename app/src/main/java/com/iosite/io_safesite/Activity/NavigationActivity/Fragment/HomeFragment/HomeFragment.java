package com.iosite.io_safesite.Activity.NavigationActivity.Fragment.HomeFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.iosite.io_safesite.Activity.NavigationActivity.NavigationActivity;
import com.iosite.io_safesite.MyApplication;
import com.iosite.io_safesite.Pojo.ExposureData;
import com.iosite.io_safesite.Pojo.ProximityData;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;
import com.iosite.io_safesite.database.ExposureORM;
import com.iosite.io_safesite.database.ProximityORM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private ImageView washYourHands;
    private TextView pageText;
    private Timer timer;
    private TimerTask timerTask;
    private int timeToCollapseWashHand = 4; // seconds
    private String TAG = "HomeFragment";
    private TextView lastUpdateTime;
    private TextView riskValue;
    private TextView numberOfDays;
    private TextView remainingDaysValue;
    private ConstraintLayout appFunctionalLayout;
    private ConstraintLayout newPossibleCasesLayout;
    private ConstraintLayout homePageLayoutOne;
    private TextView functionalUnderstoodText;
    private TextView newUnderstoodText;
    private ConstraintLayout washHandsLayout;
    private ConstraintLayout userSafeLayout;
    private LinearLayout numberOfQuarantineDaysLayout;
    private ImageView homeCinemaImage;
    private TextView quarantineText;
    private LinearLayout quarantineRemainingLayout;
    private TextView numberOfDaysText;

    private ProgressDialog loading;
    private String authToken;
    private TextView uploadFileIcon;
    private Context mContext;
    private Activity mActivity;
    private int startCount = 0, endCount = 1000, totalCount;
    private int exposureDataNotSentTotalCount;
    private int entriesInOneReq = 1000;
    private int count = 1;
    int status = 0;
    Handler handler = new Handler();
    Dialog dialog;
    ProgressBar text;
    TextView text2;
    private int exposureTimeDiff;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initUI(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        totalCount = Util.logProximityData().size();
        totalCount = ExposureORM.getNotSentExposureData().size();
        Log.i(TAG, "totalCount: " + totalCount);
//        throw new RuntimeException("Test Crash"); // Force a crash

    }

    private void initUI(View view) {
        washYourHands = view.findViewById(R.id.wash_your_hands);
        Glide.with(this).asGif().load(R.raw.wash_your_hands).into(washYourHands);
        pageText = view.findViewById(R.id.page_text);
        String sourceString = "For caring about" + "<b>" + " India " + "</b>" + "and her safety Feel proud to be an Indian";
        pageText.setText(Html.fromHtml(sourceString));
        lastUpdateTime = view.findViewById(R.id.last_update_time);
        riskValue = view.findViewById(R.id.risk_value);
        numberOfDays = view.findViewById(R.id.number_of_days);
        remainingDaysValue = view.findViewById(R.id.remaining_days_value);
        appFunctionalLayout = view.findViewById(R.id.app_functional_layout);
        newPossibleCasesLayout = view.findViewById(R.id.new_possible_cases_layout);
        homePageLayoutOne = view.findViewById(R.id.home_page_layout_1);
        functionalUnderstoodText = view.findViewById(R.id.functional_understood_text);
        functionalUnderstoodText.setOnClickListener(this);
        newUnderstoodText = view.findViewById(R.id.new_understood_text);
        newUnderstoodText.setOnClickListener(this);
        washHandsLayout = view.findViewById(R.id.wash_hand_layout);
        userSafeLayout = view.findViewById(R.id.user_safe_laout);
        numberOfQuarantineDaysLayout = view.findViewById(R.id.number_of_quarantine_days_layout);
        homeCinemaImage = view.findViewById(R.id.home_cinema_image);
        quarantineText  = view.findViewById(R.id.qurantine_text_1);
        quarantineRemainingLayout = view.findViewById(R.id.qurantine_remaining_layout);
        numberOfDaysText = view.findViewById(R.id.number_of_days_text);
        uploadFileIcon = view.findViewById(R.id.uploadFileIcon_ids);
        uploadFileIcon.setOnClickListener(this);
        numberOfDaysText.setText(" days in Home Quarantine");
        if (!PrefUtil.isKeyExistInPref(mContext, Constants.PREF_EXPOSURE_TIME_DIFF) ||
                PrefUtil.getInt(mContext, Constants.PREF_EXPOSURE_TIME_DIFF, 10) < 2) {
            PrefUtil.putInt(mContext, Constants.PREF_EXPOSURE_TIME_DIFF, 10);
        }
        showFirstView();

    }

    private void showFirstView() {
        Log.i(TAG, PrefUtil.getBoolean(getActivity(), Constants.PREF_IS_FIRST_LOGIN, false) + "");
        if ( PrefUtil.getBoolean(getActivity(), Constants.PREF_IS_FIRST_LOGIN, false)) {
            showWashHandView();
            timerToCollapseWashHand();
        } else {
            showHomeView();
        }
    }

    private void timerToCollapseWashHand() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Wash Hand timer Over");
//                TODO: show next view
                        showFuncionalView();
                    }
                });
            }
        };
        timer.schedule(timerTask, 1000*timeToCollapseWashHand);

    }

    private void controlQuarantineView() {
        if (PrefUtil.isKeyExistInPref(getActivity(), Constants.PREF_QUARANTINE_STATUS) &&
                PrefUtil.getBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, false)) {
            if (!((NavigationActivity)getActivity()).isQuarantineOver()) {
                showQuarantineView();
            } else {
                showSafeView();
            }
        } else {
            showSafeView();
        }
    }

    private void showQuarantineView() {
        numberOfQuarantineDaysLayout.setVisibility(View.VISIBLE);
        homeCinemaImage.setVisibility(View.VISIBLE);
        quarantineRemainingLayout.setVisibility(View.VISIBLE);
        userSafeLayout.setVisibility(View.GONE);
        quarantineText.setVisibility(View.VISIBLE);
        getQuarantineDynamicValues();
    }

    private void getQuarantineDynamicValues() {
        if (PrefUtil.isKeyExistInPref(getActivity(), Constants.PREF_QUARANTINE_STATUS) &&
                PrefUtil.getBoolean(getActivity(), Constants.PREF_QUARANTINE_STATUS, false)) {
            riskValue.setText("MEDIUM");
            riskValue.setTextColor(Color.RED);
        } else {
            riskValue.setText("LOW");
        }
        String qurantineSince = PrefUtil.getString(getActivity(), Constants.PREF_QUARANTINE_SINCE, "");
        Date quarantineDate = new Date();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.format(currentDate);
        try {
            quarantineDate = dateFormat.parse(qurantineSince);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = currentDate.getTime() - quarantineDate.getTime();
        numberOfDays.setText(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
        remainingDaysValue.setText(String.valueOf(14 - Integer.parseInt(numberOfDays.getText().toString())));
    }

    private void showSafeView() {
        numberOfQuarantineDaysLayout.setVisibility(View.GONE);
        homeCinemaImage.setVisibility(View.GONE);
        quarantineText.setVisibility(View.GONE);
        quarantineRemainingLayout.setVisibility(View.GONE);
        userSafeLayout.setVisibility(View.VISIBLE);
    }

    private void showHomeView() {
//        homePageLayoutOne.setAlpha(1);
        homePageLayoutOne.setVisibility(View.VISIBLE);
        controlQuarantineView();
        appFunctionalLayout.setVisibility(View.GONE);
        newPossibleCasesLayout.setVisibility(View.GONE);
        washHandsLayout.setVisibility(View.GONE);
    }

    private void showFuncionalView() {
//        homePageLayoutOne.setAlpha((float) 0.2);
        appFunctionalLayout.setVisibility(View.VISIBLE);
        newPossibleCasesLayout.setVisibility(View.GONE);
        washHandsLayout.setVisibility(View.GONE);
    }

    private void showNewCasesView() {
//        homePageLayoutOne.setAlpha((float) 0.2);
        appFunctionalLayout.setVisibility(View.GONE);
        newPossibleCasesLayout.setVisibility(View.VISIBLE);
        washHandsLayout.setVisibility(View.GONE);
    }

    private void showWashHandView() {
        homePageLayoutOne.setVisibility(View.GONE);
        appFunctionalLayout.setVisibility(View.GONE);
        newPossibleCasesLayout.setVisibility(View.GONE);
        washHandsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.functional_understood_text:
                showNewCasesView();
                break;

            case R.id.new_understood_text:
                showHomeView();
                break;

            case R.id.uploadFileIcon_ids:
                Log.i(TAG, "Upload button clicked");
//                sendProximityAPI();
                initExposureDBCreationOnlyUnComputed();
                break;
        }
    }

    // send Proximity DB data in server using Volley
    public void sendProximityAPI() {
        //new AsyncTaskRunner().execute("fs");
        Log.e("APICount", String.valueOf(count));
        loading = ProgressDialog.show(getActivity(), "Processing", "Please wait...", false, false);
        Log.e("startCount", String.valueOf(startCount));
        Log.e("endCount", String.valueOf(endCount));
        JSONArray mArray = ProximityORM.getProximityDataJsonArray(startCount, endCount);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_PROXIMITY_ARRAY_KEY, mArray);
            Log.e("jsonObject", String.valueOf(jsonObject));
            Log.e("mArraySizeInitial", String.valueOf(mArray.length()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest proximityReq = new JsonObjectRequest(Request.Method.POST, Constants.REQUEST_PROXIMITY_BULK, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), response.optString("msg"), Toast.LENGTH_SHORT).show();
                        Log.e("proximityBulkResponse", String.valueOf(response));

                        sendProximityAPIMore();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "ConnectionError!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof AuthFailureError)
                            Toast.makeText(getActivity(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof ServerError)
                            Toast.makeText(getActivity(), "No Data Found !!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof NetworkError)
                            Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof ParseError)
                            Toast.makeText(getActivity(), "Parse Error!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
//                headers.put("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNWU3YTZjZGE1M2QzMDBiZDI4YzBiNDdkIiwidGltZSI6IjIwMjAtMDQtMDRUMTM6NTc6MjMrMDA6MDAiLCJyZWZyZXNoIjpmYWxzZX0.DNp98hujIZvCCS2gTUEdWLU47nF4axnCW8CvkYjgoL4");
                //headers.put("Authorization", authToken);

                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(proximityReq);
        proximityReq.setRetryPolicy(new DefaultRetryPolicy(36000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.e("ProximityUrl", proximityReq.getUrl());
    }
    // send Proximity DB data in server using Volley
    public void sendProximityAPIMore() {
        count++;
        Log.e("APICount", String.valueOf(count));
        startCount = startCount + entriesInOneReq;
        endCount = endCount + entriesInOneReq;
        Log.e("startCountM", String.valueOf(startCount));
        Log.e("endCountM", String.valueOf(endCount));
        JSONArray jsonArray = ProximityORM.getProximityDataJsonArray(startCount, endCount);
        if (jsonArray.length() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("proximity_data", jsonArray);
                Log.e("jsonObject", String.valueOf(jsonObject));
                Log.e("mArraySizeM", String.valueOf(jsonArray.length()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest proximityReq = new JsonObjectRequest(Request.Method.POST, Constants.REQUEST_PROXIMITY_BULK, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (startCount > totalCount || endCount > totalCount)
                                loading.dismiss();
                            else
                                sendProximityAPIMore();

                            Toast.makeText(mContext, response.optString("msg"), Toast.LENGTH_SHORT).show();
                            Log.e("proximityBulkResponse", String.valueOf(response));
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading.dismiss();
                            if (error instanceof TimeoutError || error instanceof NoConnectionError)
                                Toast.makeText(getActivity(), "ConnectionError!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof AuthFailureError)
                                Toast.makeText(getActivity(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof ServerError)
                                Toast.makeText(getActivity(), "No Data Found !!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof NetworkError)
                                Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof ParseError)
                                Toast.makeText(getActivity(), "Parse Error!", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
//                    headers.put("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNWU3YTZjZGE1M2QzMDBiZDI4YzBiNDdkIiwidGltZSI6IjIwMjAtMDQtMDRUMTM6NTc6MjMrMDA6MDAiLCJyZWZyZXNoIjpmYWxzZX0.DNp98hujIZvCCS2gTUEdWLU47nF4axnCW8CvkYjgoL4");
                    //headers.put("Authorization", authToken);

                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(proximityReq);
            proximityReq.setRetryPolicy(new DefaultRetryPolicy(36000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Log.e("ProximityUrl", proximityReq.getUrl());
        }
        else
            loading.dismiss();
    }


    private void initExposureDBCreation() {

        Log.i(TAG, "total distinct users: " + ProximityORM.getAllUniqueUserIDs().size() + "");
        for (String str: ProximityORM.getAllUniqueUserIDs()) {
            Log.i(TAG, "Size: " + ProximityORM.getProximityDataForUser(str).size());
            exposureDbCreationForUser(ProximityORM.getProximityDataForUser(str));
        }
    }
    // method to fetch un-computed data from proximity table and create data for exposure table.
    private void initExposureDBCreationOnlyUnComputed() {
        Log.i(TAG, "total distinct users: " + ProximityORM.getAllUniqueUserIDs().size() + "");

        for (String str: ProximityORM.getAllUniqueUserIDs()) {
            Log.i(TAG, "Size unComputed: " + ProximityORM.getIsComputedProximityDataForUser(str, 0).size());
            exposureDbCreationForUser(ProximityORM.getIsComputedProximityDataForUser(str, 0));
        }
        Log.i(TAG, "Size Exposure DB: " + ExposureORM.getExposureData().size());
//        Util.logAllExposureData();
        Log.i(TAG, "Size NOTSENT Exposure DB: " + ExposureORM.getNotSentExposureData().size());
        if (ExposureORM.getNotSentExposureData().size() > 0) {
            Log.i(TAG, "Sending data to server");
            sendExposureDataToServer();
        }

    }
    private void sendExposureDataToServer() {
        sendExposureDataAPI();
    }
    private void exposureDbCreationForUser(LinkedList<ProximityData> userProximityData)
    {
        if (userProximityData.size() > 0) {
            ArrayList<ExposureData> exposureDataList = new ArrayList<ExposureData>();
            String prximity_user = userProximityData.getFirst().beaconId;
            String exposure_start = Util.getDateFromMillis(userProximityData.getFirst().createdAt);

            ArrayList<Double> rssiArrayList = new ArrayList<>();
            ArrayList<Double> distanceArrayList = new ArrayList<>();
            ArrayList<Double> txPowerArrayList = new ArrayList<>();
            int totalDataInExposure = 0;

            for (int k = 0; k < userProximityData.size(); k++) {
                totalDataInExposure += 1;

                if (k == 0) {
                    rssiArrayList.add(Double.parseDouble(userProximityData.get(k).rssi));
                    distanceArrayList.add(Double.parseDouble(userProximityData.get(k).distance));
                    txPowerArrayList.add(Double.parseDouble(userProximityData.get(k).txPower));
                } else {
                    if (userProximityData.get(k).createdAt - userProximityData.get(k-1).createdAt > PrefUtil.getInt(mContext, Constants.PREF_EXPOSURE_TIME_DIFF, 10)*60*1000
                            || k == userProximityData.size()-1)
                    {
                        ExposureData oneExposureData = new ExposureData();
                        oneExposureData.beaconId = prximity_user;
                        oneExposureData.mean_distance = String.valueOf(Util.restrictDecimaltoTwo(Util.getDoubleArrayListAverage(distanceArrayList)));
                        oneExposureData.median_distance = String.valueOf(Util.restrictDecimaltoTwo(Util.getDoubleArrayListMedian(distanceArrayList)));
                        oneExposureData.min_distance = String.valueOf(Util.getMinValue(distanceArrayList));
                        oneExposureData.max_distance = String.valueOf(Util.getMaxValue(distanceArrayList));
                        oneExposureData.mean_rssi = String.valueOf(Util.restrictDecimaltoTwo(Util.getDoubleArrayListAverage(rssiArrayList)));
                        oneExposureData.mean_txPower = String.valueOf(Util.restrictDecimaltoTwo(Util.getDoubleArrayListAverage(txPowerArrayList)));
                        oneExposureData.exposure_start = exposure_start;
                        oneExposureData.exposure_end = Util.getDateFromMillis(userProximityData.get(k-1).createdAt);
                        oneExposureData.numberOf_data = String.valueOf(totalDataInExposure);

                        exposureDataList.add(oneExposureData);
                        ExposureORM.insertExposureData(oneExposureData);

                        distanceArrayList.clear();
                        rssiArrayList.clear();
                        txPowerArrayList.clear();
                        totalDataInExposure = 0;

                        exposure_start = Util.getDateFromMillis(userProximityData.get(k).createdAt);
                        rssiArrayList.add(Double.parseDouble(userProximityData.get(k).rssi));
                        distanceArrayList.add(Double.parseDouble(userProximityData.get(k).distance));
                        txPowerArrayList.add(Double.parseDouble(userProximityData.get(k).txPower));


                    } else
                    {
                        rssiArrayList.add(Double.parseDouble(userProximityData.get(k).rssi));
                        distanceArrayList.add(Double.parseDouble(userProximityData.get(k).distance));
                        txPowerArrayList.add(Double.parseDouble(userProximityData.get(k).txPower));
                    }
                }
            }

            if (ProximityORM.updateIsComputedColumnForUser(prximity_user, 1)) {
                Log.i(TAG, "marked computed");
            } else {
                Log.i(TAG, "not marked");
            }
            Util.logExposureData(exposureDataList);
        } else {
            Log.i(TAG, "Empty List");
        }


    }
    // send Proximity DB data in server using Volley
    public void sendExposureDataAPI() {
        startCount = 0;
        endCount = 1000;
        //new AsyncTaskRunner().execute("fs");
        Log.e("APICount", String.valueOf(count));
        loading = ProgressDialog.show(getActivity(), "Processing", "Please wait...", false, false);
        Log.e("startCount", String.valueOf(startCount));
        Log.e("endCount", String.valueOf(endCount));
//        JSONArray mArray = ProximityORM.getProximityDataJsonArray(startCount, endCount);
        JSONArray mArray = ExposureORM.getNotSentExposureDataJsonArray(startCount, endCount);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_EXPOSURE_ARRAY_KEY, mArray);
            Log.e("jsonObject", String.valueOf(jsonObject));
            Log.e("mArraySizeInitial", String.valueOf(mArray.length()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest exposureReq = new JsonObjectRequest(Request.Method.POST, Constants.REQUEST_EXPOSURE_BULK, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), response.optString("msg"), Toast.LENGTH_SHORT).show();
                        Log.e("proximityBulkResponse", String.valueOf(response));

                        sendExposureDataAPIMore();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "ConnectionError!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof AuthFailureError)
                            Toast.makeText(getActivity(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof ServerError)
                            Toast.makeText(getActivity(), "No Data Found !!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof NetworkError)
                            Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                        else if (error instanceof ParseError)
                            Toast.makeText(getActivity(), "Parse Error!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNWU3YTZjZGE1M2QzMDBiZDI4YzBiNDdkIiwidGltZSI6IjIwMjAtMDQtMDRUMTM6NTc6MjMrMDA6MDAiLCJyZWZyZXNoIjpmYWxzZX0.DNp98hujIZvCCS2gTUEdWLU47nF4axnCW8CvkYjgoL4");
                headers.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
                //headers.put("Authorization", authToken);

                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(exposureReq);
        exposureReq.setRetryPolicy(new DefaultRetryPolicy(36000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.e("ProximityUrl", exposureReq.getUrl());
    }
    // send Proximity DB data in server using Volley
    public void sendExposureDataAPIMore() {
        count++;
        Log.e("APICount", String.valueOf(count));
        startCount = startCount + entriesInOneReq;
        endCount = endCount + entriesInOneReq;
        Log.e("startCountM", String.valueOf(startCount));
        Log.e("endCountM", String.valueOf(endCount));
//        JSONArray jsonArray = ProximityORM.getProximityDataJsonArray(startCount, endCount);
        JSONArray jsonArray = ExposureORM.getNotSentExposureDataJsonArray(startCount, endCount);
        if (jsonArray.length() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("proximity_data", jsonArray);
//                Log.e("jsonObject", String.valueOf(jsonObject));
                Log.e("mArraySizeM", String.valueOf(jsonArray.length()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest proximityReq = new JsonObjectRequest(Request.Method.POST, Constants.REQUEST_EXPOSURE_BULK, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (startCount > totalCount || endCount > totalCount) {
                                loading.dismiss();
                                ExposureORM.updateIsSentColumn(1);
                            }
                            else
                                sendExposureDataAPIMore();

                            Toast.makeText(mContext, response.optString("msg"), Toast.LENGTH_SHORT).show();
                            Log.e("proximityBulkResponse", String.valueOf(response));
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading.dismiss();
                            if (error instanceof TimeoutError || error instanceof NoConnectionError)
                                Toast.makeText(getActivity(), "ConnectionError!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof AuthFailureError)
                                Toast.makeText(getActivity(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof ServerError)
                                Toast.makeText(getActivity(), "No Data Found !!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof NetworkError)
                                Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                            else if (error instanceof ParseError)
                                Toast.makeText(getActivity(), "Parse Error!", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
//                    headers.put("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNWU3YTZjZGE1M2QzMDBiZDI4YzBiNDdkIiwidGltZSI6IjIwMjAtMDQtMDRUMTM6NTc6MjMrMDA6MDAiLCJyZWZyZXNoIjpmYWxzZX0.DNp98hujIZvCCS2gTUEdWLU47nF4axnCW8CvkYjgoL4");
                    //headers.put("Authorization", authToken);

                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(proximityReq);
            proximityReq.setRetryPolicy(new DefaultRetryPolicy(36000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Log.e("ProximityUrl", proximityReq.getUrl());
        }
        else {
            loading.dismiss();
            ExposureORM.updateIsSentColumn(1);
        }
    }

    public void showDialog(Activity activity, String msg) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_percentage_design);

        text = dialog.findViewById(R.id.progress_horizontal);
        text2 = dialog.findViewById(R.id.value123);

        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (status < totalCount) {
                    status += 50;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            text.setProgress(status);
                            text2.setText(String.valueOf(status));
                            if (status == totalCount) {
                                status = 0;
                            }
                        }
                    });
                }
            }
        }).start();
    }
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "resp";
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            showDialog(mActivity,"");
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}
