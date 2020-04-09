package com.iosite.io_safesite.Network;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class RequestManager {
    /**
     * TODO:
     * 1: Cache Images should be cleaned once application is uninstalled.
     * 2: Do, I have to start the queue on queue() and loader() functions
     * 3: DiskCache in RequestQueue Vs LRUCache in ImageLoader
     * 4: Request Priority order
     * 5: can we use only one requestQueue.
     */
    /**
     * The default socket timeout in milliseconds
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    public static final int DEFAULT_MAX_RETRIES = 1;
    private static RequestManager instance;
    private static ImageLoader mImageLoader;
    private RequestQueue mDataRequestQueue;
    private RequestQueue mImageQueue;
    private Context mContext;
    private Config mConfig;

    private static CopyOnWriteArrayList<Request> queuedRequestWhenAuthFailed;


    //	private static String mDefaultRequestTag;
    public static class Config {
        private String mImageCachePath;
        private int mDefaultDiskUsageBytes;
        private int mThreadPoolSize;

        public Config(final String imageCachePath, final int defaultDiskUsageBytes, final int threadPoolSize) {
            this.mDefaultDiskUsageBytes = defaultDiskUsageBytes;
            this.mImageCachePath = imageCachePath;
            this.mThreadPoolSize = threadPoolSize;

        }
    }


    private RequestManager(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
    }

    //TODO: Initialize this on application onCreate()
    public static synchronized RequestManager initializeWith(Context context, Config config) {
        if (instance == null) {
            instance = new RequestManager(context, config);
        }
        return instance;
    }

    public synchronized RequestQueue getDataRequestQueue() {
        if (mDataRequestQueue == null) {
            mDataRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
            mDataRequestQueue.start();
        }
        return mDataRequestQueue;
    }


    public static <T> void addRequest(Request<T> pRequest) {
        try {
            if (instance == null) {
                throw new IllegalStateException(RequestManager.class.getSimpleName() + " is not initialized, call initializeWith(..) method first.");
            }
            if (pRequest.getTag() == null) {
                new IllegalArgumentException("Request Object Tag is not specified.");
            }
            pRequest.setRetryPolicy(new MyRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue queue = instance.getDataRequestQueue();
            queue.add(pRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add request in queue when request is auth is failed
     *
     * @param pRequest
     * @param <T>
     */
    public static <T> void addRequestInQueueWhenAuthFailed(Request<T> pRequest) {
        if (queuedRequestWhenAuthFailed == null)
            queuedRequestWhenAuthFailed = new CopyOnWriteArrayList<>();

        queuedRequestWhenAuthFailed.add(pRequest);
    }

    /**
     * Add all queued request in request manager
     */


    public static void releasePendingQueue(Context context) {
        if (queuedRequestWhenAuthFailed != null) {
            try {
                ListIterator<Request> mListIterator = queuedRequestWhenAuthFailed.listIterator();
                while (mListIterator.hasNext()) {
                    Request request = mListIterator.next();
                    RequestManager.addRequest(request);
//                    mListIterator.remove();
                    queuedRequestWhenAuthFailed.remove(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Clear all pending request
     */
    public static void clearAllPendingQueue() {
        if (queuedRequestWhenAuthFailed != null) {
            queuedRequestWhenAuthFailed.clear();
        }
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important to
     * specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param pRequestTag
     */
    public static void cancelPendingRequests(Object pRequestTag) {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call initializeWith(..) method first.");
        }
        if (instance.getDataRequestQueue() != null) {
            Log.d("=====", "======== pRequestTag : " + (String) pRequestTag);
            instance.getDataRequestQueue().cancelAll(pRequestTag);
        }
    }
}