package com.iosite.io_safesite.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iosite.io_safesite.Network.GsonObjectRequest;
import com.iosite.io_safesite.Network.NetworkUpdateListener;
import com.iosite.io_safesite.Network.OnResponseReceived;
import com.iosite.io_safesite.Network.RequestManager;
import com.iosite.io_safesite.Pojo.BaseResponseModel;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.NotificationUtil;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "MyFirebaseMessaging";
    private Context mContext;
    public static final String CHANNEL_ID = "NotifChannel";
    public static final String CHANNEL_ID_ANN = "AnnouncementChannel";
    public static final String CHANNEL_ID_MSG = "MsgChannel";
    private String fcm_token = "";
    Location location;
    private Vibrator audioVibe;
    private VibrationEffect audioVibeEffect;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public MyFirebaseMessagingService(Context context) {
        Log.i(TAG, "MyFirebaseMessagingService started");
        mContext = context;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived called: " + remoteMessage.getData());

        Map<String, String> messageReceivedData = remoteMessage.getData();
        if (messageReceivedData.size() > 0) {
            //TODO: handle new group added notif and subscribe for the group. Also add group to group list
//            sendMessageNotification(messageReceivedData);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "New token generated: " + token);
        fcm_token = token;
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(final String FCMToken) {
        try {
            if (Util.isInternetConnected(mContext)) {
                Util.clearCache(mContext);
                GsonObjectRequest sendFCMTokenRequest = new GsonObjectRequest<>(mContext, Request.Method.PUT, Constants.REQUEST_FCM_TOKEN,
                        Constants.REQUEST_FCM_TOKEN_URL, getRequestHeaders(), getJsonPayload(FCMToken), BaseResponseModel.class,
                        new NetworkUpdateListener(new OnResponseReceived() {
                            @Override
                            public void onRecieve(Object object) {
                                Log.d(TAG, "fcm_token sent to server is: " + FCMToken);
                                PrefUtil.putString(mContext, Constants.PREF_FCM_TOKEN, FCMToken);
                                /*opening port after subscribing to iosite push notif*/
                                Util.openFCMPort(mContext);
                            }

                            @Override
                            public void onErrorRecive(VolleyError error, String customMsg, String header) {
                                // Handle error here
                                Log.e(TAG, "sendRegistrationToServer error receive :" + customMsg);
                            }
                        }));
                RequestManager.addRequest(sendFCMTokenRequest);
            } else {
                Toast.makeText(mContext, "Internet is not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", PrefUtil.getString(mContext, Constants.PREF_USER_ACCESS_TOKEN, ""));
        requestHeaders.put("Content-Type", "application/json");
        Log.e(TAG, requestHeaders.toString());
        return requestHeaders;
    }

    private String getJsonPayload(String FCMToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PARAM_FCM_TOKEN, FCMToken);
            Log.i(TAG, jsonObject.toString());   // addedd by kamran
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();

    }

    private void sendMessageNotification(Map<String, String> msgNotification) {

        String message = msgNotification.get("message").toString();

        String messageContent = "";
        String senderOrGroupUID = "";
        String notificationTitle = "";
        String senderOrGroupName = "";
        Boolean isFromGruop = false;

        NotificationUtil notificationUtil = new NotificationUtil(message);

        if (notificationUtil.getMsgCategory().equals("message")) {
            // TODO: take action on receving notification
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createIositeMsgNotification(Map<String, String> msgNotification) {
        String notificationContent = "";
        String notificationTitle = "";
        String notificationId = "";
        PendingIntent pendingIntent = null;

        // TODO: fetch all the details from msg notification

        createNotification(notificationTitle, notificationContent, notificationId, pendingIntent);
    }

    public void fcmTokenRetrieve() {
        try {
            if (Util.isInternetConnected(mContext)) {
                Log.i(TAG, "fcmTokenRetrieve Internet Connected");
                if (isGooglePlayServicesAvailable(mContext)) {
                    Log.i(TAG, "GooglePlayServicesAvailable Available");
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                        return;
                                    }
                                    fcm_token = task.getResult().getToken().toString();
                                    if (!fcm_token.isEmpty()) {
                                        Log.d(TAG, "FCM beiing sent");
                                        sendRegistrationToServer(fcm_token);
                                    }

                                    Log.d(TAG, "FCM Token: " + fcm_token);
                                }
                            });
                } else {
                    Log.i(TAG, "GooglePlayServicesAvailable NOT Available");
                }

            } else {
                Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Some Error on FCM");
        }
    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification(String notificationTitle, String notificationContent,
                                    String notificationId, PendingIntent pendingIntent){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID_ANN,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
        }
        AudioAttributes att = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
        channel.setSound(defaultSoundUri, att);
        channel.enableLights(true);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID_ANN)
                        .setSmallIcon(R.drawable.common_google_signin_btn_text_dark)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(Color.BLUE)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL);

        notificationManager.notify((int) Long.parseLong(notificationId.substring(notificationId.length() - 4), 16),
                notificationBuilder.build());
    }

}
