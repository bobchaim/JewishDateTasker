package com.chaimchaikin.jewishdatetasker;

import com.google.android.gms.maps.model.LatLng;

import java.util.TimeZone;

/**
 * Created by Chaim on 2015-02-18.
 */
public class LocationPoint {

    public LatLng location = new LatLng(0,0);
    public String locationName = "unknown";
    public String timezone = TimeZone.getDefault().getID();

    public LocationPoint(LatLng location, String locationName, String timezone) {
        location = location;
        locationName = locationName;
        timezone = timezone;
    }

    public LocationPoint() {

    }
}
