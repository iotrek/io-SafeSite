package com.iosite.io_safesite.Util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.iosite.io_safesite.interfaces.OnLocationFinderListener;

public class LocationUtil {

    public static void getCurrentLatLongGPS(final int type, LocationManager mLocationManager, final Context context, final OnLocationFinderListener listener) {

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (checkForLocationPermission(context)) {
                listener.onPermissionGranted(type);

                LocationRequest currentLocationRequest = new LocationRequest();
                currentLocationRequest.setInterval(500)
                        .setFastestInterval(0)
                        .setMaxWaitTime(0)
                        .setSmallestDisplacement(0)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                        .setExpirationDuration(60000);
                LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(currentLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        listener.onLocationCallBackResult(locationResult, type);
                        if (this != null)
                            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(this);
                        else
                            Toast.makeText(context, "location req expired", Toast.LENGTH_LONG).show();
                    }
                }, Looper.myLooper());
            } else {
                listener.onPermissionDenied(type);
            }
        } else {
            listener.onProviderDisabled(type);
        }

    }

    public static boolean checkForLocationPermission(Context context) {
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public static void getUserLastLocation(Context context, GoogleApiClient mGoogleApiClient, final OnLocationFinderListener listener, int type) {
        try {
            if (checkForLocationPermission(context)) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                listener.onLastLocationUpdate(location);
            } else {
                listener.onPermissionDenied(type);
            }
        } catch (Exception ex) {
            listener.onLastLocationUpdate(null);
            listener.onPermissionDenied(type);
        }
    }

}
