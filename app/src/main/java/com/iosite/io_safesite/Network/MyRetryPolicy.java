package com.iosite.io_safesite.Network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;

class MyRetryPolicy extends DefaultRetryPolicy {

    private int mCurrentTimeoutMs;

    /**
     * The current retry count.
     */
    private int mCurrentRetryCount;

    /**
     * The maximum number of attempts.
     */
    private final int mMaxNumRetries;

    /**
     * The backoff multiplier for the policy.
     */
    private final float mBackoffMultiplier;

    /**
     * The default socket timeout in milliseconds
     */
    public static final int DEFAULT_TIMEOUT_MS = 25000;

    /**
     * The default number of retries
     */
    public static final int DEFAULT_MAX_RETRIES = 1;

    /**
     * The default backoff multiplier
     */
    public static final float DEFAULT_BACKOFF_MULT = 1f;


    public MyRetryPolicy() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }

    /**
     * Constructs a new retry policy.
     *
     * @param initialTimeoutMs  The initial timeout for the policy.
     * @param maxNumRetries     The maximum number of retries.
     * @param backoffMultiplier Backoff multiplier for the policy.
     */
    public MyRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }

    /**
     * Returns the current timeout.
     */
    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }

    /**
     * Returns the current retry count.
     */
    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    /**
     * Returns the backoff multiplier for the policy.
     */
    public float getBackoffMultiplier() {
        return mBackoffMultiplier;
    }

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     *
     * @param error The error code of the last attempt.
     */
    @Override
    public void retry(VolleyError error) throws VolleyError {

        if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {

            throw error;
        } else {
            mCurrentRetryCount++;
            mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
            if (!hasAttemptRemaining()) {
                throw error;
            }
        }
    }

    /**
     * Returns true if this policy has attempts remaining, false otherwise.
     */
    protected boolean hasAttemptRemaining() {
        return mCurrentRetryCount <= mMaxNumRetries;
    }

}
