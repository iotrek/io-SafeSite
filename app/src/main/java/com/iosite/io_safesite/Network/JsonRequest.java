package com.iosite.io_safesite.Network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.iosite.io_safesite.Util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonRequest<T> extends Request<T> {

    /**
     * Charset for request.
     */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    public final String mRequestBody;
    private String mUrl;
    private String mPostBody;

    private String contentType;
    /**
     * Request headers.
     */
    protected Map<String, String> mRequestHeaders;

    private Priority mPriority;

    protected NetworkResponse mResponse;
    private Map<String, String> params;

    public JsonRequest(int method, String url, String jsonPayload, Response.ErrorListener errorListener) {
        this(method, url, Collections.<String, String>emptyMap(), jsonPayload, errorListener);
    }

    public JsonRequest(int method, String url, Map<String, String> mRequestHeaders, String jsonPayload, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mRequestBody = jsonPayload;
        this.mUrl = url;
        if (mRequestHeaders == null) {
            mRequestHeaders = new HashMap<>();
        }

        this.mRequestHeaders = mRequestHeaders;
    }

    public void setPriority(Priority mPriority) { this.mPriority = mPriority; }

    @Override
    public Priority getPriority() {
        return this.mPriority;
    }


    @Override
    public String getBodyContentType() {
        if (StringUtil.isNullOrEmpty(contentType)) {
            return PROTOCOL_CONTENT_TYPE;
        }
        return contentType;
    }

    public void setBodyContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }


    @Override
    public byte[] getBody() {
        try {
            if (mPostBody != null) {
                return mPostBody.getBytes(PROTOCOL_CHARSET);
            } else if (params == null) {
                return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
            } else {
                StringBuilder encodedParams = new StringBuilder();
                try {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        encodedParams.append(URLEncoder.encode(entry.getKey(), PROTOCOL_CHARSET));
                        encodedParams.append('=');
                        encodedParams.append(URLEncoder.encode(entry.getValue(), PROTOCOL_CHARSET));
                        encodedParams.append('&');
                    }
                    return encodedParams.toString().getBytes(PROTOCOL_CHARSET);
                } catch (UnsupportedEncodingException uee) {
                    throw new RuntimeException("Encoding not supported: " + PROTOCOL_CHARSET, uee);
                }
            }
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    public void setParams(Map<String, String> mparams) {
        this.params = mparams;
    }

    public void setPostBody(String body) {
        this.mPostBody = body;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {

        return mRequestHeaders;
    }

    /*@Override
    public String getPostBodyContentType() {
        return "application/x-www-form-urlencoded";
    }*/
}
