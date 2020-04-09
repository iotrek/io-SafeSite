package com.iosite.io_safesite.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iosite.io_safesite.Activity.SplashActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_BOOT_COMPLETED");
            Intent activityIntent = new Intent(context, SplashActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
