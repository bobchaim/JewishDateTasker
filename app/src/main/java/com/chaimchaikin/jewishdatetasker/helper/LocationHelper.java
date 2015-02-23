package com.chaimchaikin.jewishdatetasker.helper;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.chaimchaikin.jewishdatetasker.R;
import com.chaimchaikin.jewishdatetasker.TimeZoneMapper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Chaim on 2015-01-02.
 *
 * LocationHelper
 *
 * Starts a location service and gets location information on update of current location
 *
 */
public class LocationHelper implements LocationListener {

    // Internal variables for finding location
    private LocationManager locationManager;
    private String provider;

    // Keep track of requests for update
    private boolean updatesRequested = false;

    // Stores context from parent
    private Context mContext;

    // Variables accessible from outside store current location and details
    public String locationName, timezone;
    public Double lat, lng;


    /**
     * Constructor for LocationHelper
     *
     * - Starts a new LocationManager
     * - Finds the best available provider
     *
     * @param mContext receives the context from the parent class for later use
     */

    public LocationHelper (Context mContext){
        // Store the context for later use
        this.mContext = mContext;

        // Get the location manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Find the best provider
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);

    }

    /**
     * Request the LocationManager to look for location updates
     */
    public void requestLocationUpdates() {
        // Only request new updates if they haven't been requested already
        if(!updatesRequested) {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(provider, 300, 10, LocationHelper.this);

            updatesRequested = true;
        }
    }

    /**
     * Remove all requested location update requests
     */
    public void removeUpdates() {

        locationManager.removeUpdates(this);
        updatesRequested = false;
    }

    /**
     * Get and store the timezone from the current lat/lng
     */
    public void getTimezone() {
        timezone = TimeZoneMapper.latLngToTimezoneString(lat, lng);
    }


    /**
     * When location is changed
     *
     * - Store the new location
     * - Get the timezone for this location
     * - Get the location name for this location
     *
     * @param location the new location
     */
    @Override
    public void onLocationChanged(Location location) {
        // Store new location
        lat = location.getLatitude();
        lng = location.getLongitude();

        // Get timezone for location
        getTimezone();

        // Get location name
        locationName = getLocationName(lat, lng);
    }

    /**
     * Gets the name of the location from lat/lng
     *
     * - Creates a new geocoder
     * - Finds the location name
     *
     * @param lat Latitude
     * @param lng Longitude
     * @return String containing location name
     */
    public String getLocationName(double lat, double lng) {
        // Start with location "unknown" in case we can't get location from geocoder
        String locationNameText = mContext.getString(R.string.unknown_location);

        // Create a new geocoder
        Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
        try {
            // Get address from location
            List<Address> listAddresses = geocoder.getFromLocation(lat, lng, 1);
            if(null!=listAddresses&&listAddresses.size()>0){

                // Make a comma separated list of the address
                locationNameText = listAddresses.get(0).getSubLocality() + ", " + listAddresses.get(0).getLocality() + ", " + listAddresses.get(0).getSubAdminArea() + ", " + listAddresses.get(0).getAdminArea() + ", " + listAddresses.get(0).getCountryName();

                // Since not all locations have the same method for getting the readable part of the address
                // we get all the possible parts of the address and then filter out all the 'null,' results
                // (e.g. some places return null for subLocality and proper name for Locality others return
                //       proper name for subLocality and null for Locality)
                locationNameText = locationNameText.replace("null, ", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the location name
        return locationNameText;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


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
