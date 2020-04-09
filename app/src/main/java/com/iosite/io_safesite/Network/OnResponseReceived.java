package com.iosite.io_safesite.Network;

import com.android.volley.VolleyError;

public interface OnResponseReceived {

    void onRecieve(Object object);

    void onErrorRecive(VolleyError error, String customMsg, String header);

}