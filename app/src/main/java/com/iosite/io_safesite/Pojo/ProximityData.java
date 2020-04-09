package com.iosite.io_safesite.Pojo;

import java.io.Serializable;

public class ProximityData implements Serializable {
    public long unique_id;
    public String beaconId;
    public String distance;
    public String rssi;
    public String txPower;
    public long createdAt;
    public int is_computed;
}
