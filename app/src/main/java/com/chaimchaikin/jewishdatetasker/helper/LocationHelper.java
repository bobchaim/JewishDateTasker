package com.chaimchaikin.jewishdatetasker.helper;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.chaimchaikin.jewishdatetasker.TimeZoneMapper;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chaim on 2015-01-02.
 */
public class LocationHelper implements LocationListener {

    private LocationManager locationManager;
    private String provider;

    public String locationName, timezone;
    public Double lat, lng;

    private Location globalLocation;

    public Context mContext;

    public LocationHelper (Context mContext){
        this.mContext = mContext;

        // Get the location manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);


        /*Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            //System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            locationName = "Location not available";
        }*/
    }


    public void updateLocation() {
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public void requestLocationUpdates() {
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    public void removeUpdates() {
        locationManager.removeUpdates(this);
    }

    public void getTimezone() {
        TimeZoneMapper timeZoneMapper = new TimeZoneMapper();
        timezone = timeZoneMapper.latLngToTimezoneString(lat, lng);

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = (double) (location.getLatitude());
        lng = (double) (location.getLongitude());


        getTimezone();
        locationName = getLocationName(lat, lng);
    }

    public String getLocationName(double lat, double lng) {
        String locationNameText = "unknown";

        Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(lat, lng, 1);
            if(null!=listAddresses&&listAddresses.size()>0){

                locationNameText = listAddresses.get(0).getSubLocality() + ", " + listAddresses.get(0).getLocality() + ", " + listAddresses.get(0).getSubAdminArea() + ", " + listAddresses.get(0).getAdminArea() + ", " + listAddresses.get(0).getCountryName();
                locationNameText = locationNameText.replace("null, ", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationNameText;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(mContext, "Enabled new provider " + provider,
        //        Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(mContext, "Disabled provider " + provider,
        //        Toast.LENGTH_SHORT).show();
    }


}
