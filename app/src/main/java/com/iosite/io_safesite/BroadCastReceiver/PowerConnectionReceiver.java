package com.iosite.io_safesite.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;

public class PowerConnectionReceiver extends BroadcastReceiver {

    public int batteryPercent;
    private String TAG = "PowerConnectionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isCharging = false;

        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int)(100.0f * ((float)level / (float) scale));
//            batteryPercent = level;
            PrefUtil.putString(context, Constants.PREF_BATTERY_PERCENTAGE, String.valueOf(batteryPercent));
        }

        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            isCharging = true;
            PrefUtil.putString(context, Constants.PREF_IS_CHARGING, String.valueOf(isCharging));
            PrefUtil.putString(context, Constants.PREF_EVENT_UPLINK, "charge");
        }
        else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            isCharging = false;
            PrefUtil.putString(context, Constants.PREF_IS_CHARGING, String.valueOf(isCharging));
            PrefUtil.putString(context, Constants.PREF_EVENT_UPLINK, "charge");
        }


    }
}
