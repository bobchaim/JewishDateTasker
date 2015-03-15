package com.chaimchaikin.jewishdatetasker;

import com.google.android.gms.maps.model.LatLng;

import java.util.TimeZone;

/**
 * Abstract wrapper for a location
 * - Contains lat/lng, location name, timezone and altitude
 */
public class LocationPoint {

    public LatLng location = new LatLng(0,0);
    public String locationName = "unknown";
    public double altitude = 0;
    public String timezone = TimeZone.getDefault().getID();

    public LocationPoint(LatLng lc, String ln, String tz, double alt) {
        location = lc;
        locationName = ln;
        timezone = tz;
        altitude = alt;
    }

    public LocationPoint() {

    }
}
