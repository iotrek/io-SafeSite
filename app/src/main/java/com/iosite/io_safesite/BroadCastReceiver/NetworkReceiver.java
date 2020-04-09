package com.iosite.io_safesite.BroadCastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.iosite.io_safesite.Activity.SplashActivity;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.BLEScanner;
import com.iosite.io_safesite.Util.BLEScannerOne;
import com.iosite.io_safesite.Util.BLETransmitter;
import com.iosite.io_safesite.Util.ConnectivityUtil;
import com.iosite.io_safesite.Util.Util;

public class NetworkReceiver extends BroadcastReceiver {
    private String notificationId = "22222222";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            if(!ConnectivityUtil.getNetworkType(context).equalsIgnoreCase("NullNetworkInfo")) {
                Util.sendFCMToIosite(context);
                Util.openFCMPort(context);
//                if(!ConnectivityUtil.getNetworkType(context).equalsIgnoreCase("PROXY")) {
//                    Log.i("Kamran NetworkReceiver", "NON BT Internet regained");
//                }
            }
        }
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state)
            {
                case BluetoothAdapter.STATE_OFF:
                    Log.i("Kamran NetworkReceiver", "Bluetooth off");
                    createGPSOffNotification(intent, context, notificationId);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.i("Kamran NetworkReceiver", "Turning Bluetooth off...");

                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i("Kamran NetworkReceiver", "Bluetooth ON.");
                    cancelNotif(context);
                    BLETransmitter.setupBeacon(context);
                    BLEScanner.initScanner(context);
                    BLEScannerOne.initScanner(context);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i("Kamran NetworkReceiver", "Turning ON...");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                {
                    break;
                }
                case BluetoothAdapter.STATE_DISCONNECTED:
                {
                    break;
                }
                default:
                    break;
            }

        }
    }

    private void cancelNotif(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(notificationId));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createGPSOffNotification(Intent intent, Context context, String notificationId) {
        intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Integer.parseInt(notificationId),
                intent,
                PendingIntent.FLAG_ONE_SHOT);
        createNotification(context,
                "Bluetooth Event",
                "Bluetooth is switched OFF. Pleas switch it ON.",
                notificationId,
                pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotification(Context context, String notificationTitle, String notificationContent,
                                    String notificationId, PendingIntent pendingIntent){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("BLEChannel",
                    "Bluetooth OFF Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
        }
        AudioAttributes att = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build();
        channel.setSound(defaultSoundUri, att);
        channel.setSound(defaultSoundUri, att);
        channel.enableLights(true);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "BLEChannel")
                        .setSmallIcon(R.drawable.common_google_signin_btn_text_dark)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(Color.BLUE)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL);

        notificationManager.notify(Integer.parseInt(notificationId),
                notificationBuilder.build());
    }
}
