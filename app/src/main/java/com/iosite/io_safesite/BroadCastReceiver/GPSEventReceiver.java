package com.iosite.io_safesite.BroadCastReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.iosite.io_safesite.Activity.SplashActivity;
import com.iosite.io_safesite.R;

public class GPSEventReceiver extends BroadcastReceiver {
    private String TAG = "GPSEventReceiver";
    private String notificationId = "11111111";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, Intent intent) {

        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGpsEnabled) {
                Log.i(TAG, "GPS Enabled");
            } else {
                Log.i(TAG, "GPS Disabled");
                createGPSOffNotification(intent, context, notificationId);
            }

            if (isNetworkEnabled) {
                Log.i(TAG, "Network Enabled");
                cancelNotif(context);
            } else {
                Log.i(TAG, "Network Disabled");
                createGPSOffNotification(intent, context, notificationId);
//                askUserToChangeSettings(context);
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
                "GPS Event",
                "GPS is switched OFF. Pleas switch it ON.",
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
            channel = new NotificationChannel("GPSChannel",
                    "GPS OFF Notification",
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
                new NotificationCompat.Builder(context, "GPSChannel")
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
