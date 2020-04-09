package com.iosite.io_safesite.Network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class NetworkUpdateListener<T> implements Response.Listener<T>, Response.ErrorListener {

    private static final String TAG = "NetworkUpdateListener";

    private final OnResponseReceived screen;

    public NetworkUpdateListener(OnResponseReceived listener) {
        screen = listener;
    }

    @Override
    public void onResponse(T response) {

        if (response == null) {
            screen.onErrorRecive(new VolleyError(), "Something went wrong. Please try again.", "Alert");
        } else {
            screen.onRecieve(response);
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            screen.onErrorRecive(error, "Something went wrong. Please try again.", "Alert");
        } else if (error != null && error.networkResponse == null) {
//            screen.onErrorRecive(new VolleyError("error"), "Something went wrong. Please try again.", "Alert");
            screen.onErrorRecive(new VolleyError("No internet connection."), "No internet connection.", "Alert");
        } else if (error != null && error.getClass().equals(AuthFailureError.class)) {
        }

    }

}
