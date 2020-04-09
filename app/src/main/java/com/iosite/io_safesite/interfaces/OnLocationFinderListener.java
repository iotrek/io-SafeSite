package com.iosite.io_safesite.interfaces;

import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.iosite.io_safesite.Pojo.IpLocationModel;

public interface OnLocationFinderListener {

    void onPermissionGranted(int type);

    void onPermissionDenied(int type);

    void onProviderDisabled(int type);

    void onLocationCallBackResult(LocationResult locationResult, int type);

    void onLastLocationUpdate(Location location);

    void onLocationUpdateViaIp(IpLocationModel.LocationModel locationModel);

    void onLocationError();

}