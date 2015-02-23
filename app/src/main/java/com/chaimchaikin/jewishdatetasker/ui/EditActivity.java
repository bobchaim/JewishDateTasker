/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.chaimchaikin.jewishdatetasker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chaimchaikin.jewishdatetasker.LocationPoint;
import com.chaimchaikin.jewishdatetasker.R;
import com.chaimchaikin.jewishdatetasker.TimeZoneMapper;
import com.chaimchaikin.jewishdatetasker.bundle.BundleScrubber;
import com.chaimchaikin.jewishdatetasker.bundle.PluginBundleManager;
import com.chaimchaikin.jewishdatetasker.helper.LocationHelper;
import com.chaimchaikin.jewishdatetasker.helper.TaskerPlugin;
import com.google.android.gms.maps.model.LatLng;

/**
 * Edit activity for the Tasker Plug-in.
 *
 */
public final class EditActivity extends AbstractPluginActivity
{
    /**
     *  Global variables
     */

    // The TextView that shows location to user (is used a lot throughout)
    private TextView locationNameTextView;

    // Start a location helper
    LocationHelper locHelper;

    // This keeps track of whether a custom location is set or not in order to keep the buttons correctly disabled/enabled
    // It is not passed with the rest of the plugin settings
    boolean customLocationSet = false;

    // This is to keep track of the plugin settings during this activity
    // It is passed back in the bundle of settings
    boolean settingLocationAuto = false;
    LocationPoint settingsLocation = new LocationPoint();


    // Location request activity
    int CHOOSE_LOCATION_REQUEST = 1;

    /**
     * Called on creating activity
     * - Loads settings from intent bundle if necessary
     * - Makes a location helper for later use
     *
     * @param savedInstanceState if a saved instance of this activity exists already
     */

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get the bundle of settings for the plugin
        BundleScrubber.scrub(getIntent());
        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        // Create a new location helper
        locHelper = new LocationHelper(this);

        // Set the correct view
        setContentView(R.layout.activity_settings);

        // Set this to the TextView where we show the user the current location
        locationNameTextView = (TextView) findViewById(R.id.LocationName);

        // Make sure the "use current location" and "choose location" buttons are correctly enabled/disabled
        updateLocationButton();


        if (null == savedInstanceState)
        {
            // If there is settings for this action already..
            if(localeBundle != null) {

                // Get location point from previous plugin settings
                double lat = localeBundle.getDouble("loc_lat", 0);
                double lng = localeBundle.getDouble("loc_lng", 0);
                String locationName = localeBundle.getString("loc_name");
                String timezone = localeBundle.getString("timezone");

                // Set the settings location as a new LocationPoint with the location information just retrieved
                settingsLocation = new LocationPoint(new LatLng(lat,lng), locationName, timezone);
                settingLocationAuto = localeBundle.getBoolean("loc_auto", false);

                // Since there is a location set already enable the "current location" button
                customLocationSet = true; // By setting customLocation
                updateLocationButton(); // And then calling method to update button

                // Set the location to be automatic or not based on the current setting
                // (The 'true' argument tells the method to also set the check based on the setting, usually it checked by the user)
                setAutoLocation(settingLocationAuto, true);

                // Update the location TextView to have the current location name
                locationNameTextView.setText(locationName);


            } else {
                // Location defaults as not automatic
                // (The 'true' argument tells the method to also clear the check)
                setAutoLocation(false, true);

                // Update the location to the current location
                updateLocation();

            }

        }
    }

    /**
     * User presses button to select a new location
     * - Launch map activity (pass current settings location)
     *
     * @param view pressed button
     */
    public void selectLocation(View view) {

        // Launch map activity
        Intent intent = new Intent(this, CustomLocationActivity.class);

        // Get the lat/lng from the current location settings
        double lat = settingsLocation.location.latitude;
        double lng = settingsLocation.location.longitude;

        // Make a bundle with the lat/lng data
        Bundle b = new Bundle();
        b.putDouble("lat", lat);
        b.putDouble("lng", lng);
        intent.putExtras(b);

        // Start an intent to choose the location
        startActivityForResult(intent, CHOOSE_LOCATION_REQUEST);
    }

    /**
     * When the location has been chosen
     * - Confirm the request was OK and was the right request
     * - Get the location from that intent
     * - Get related required information (location name, timezone)
     * - Store it in the settings
     * - Update ui to reflect changes (custom location set, location name changed)
     *
     * @param requestCode   Code for the request
     * @param resultCode    Code for the result (to see if it went OK)
     * @param data          Intent returned from activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the request went well (OK) and the request was CHOOSE_LOCATION_REQUEST
        if (resultCode == Activity.RESULT_OK && requestCode == CHOOSE_LOCATION_REQUEST) {

            // Stop requesting location updates for current location
            locHelper.removeUpdates();

            // Get lat/lng from chosen location
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);

            // Make a LatLng object
            LatLng locationPoint = new LatLng(lat, lng);

            // Get the location name from the chosen location
            String locationName = locHelper.getLocationName(lat, lng);

            // Turn off auto location (usually it's not possible to get here if auto location is on, but just in case)
            setAutoLocation(false, true);

            // Save this location to the settings
            settingsLocation = new LocationPoint(locationPoint, locationName, getTimezone(locationPoint));

            // Update TextView of location shown to user
            locationNameTextView.setText(locationName);

            // Set and update the "custom location" buttons
            customLocationSet = true;
            updateLocationButton();
        }
    }

    /**
     * Sets the "use current location" button to be enabled or disabled based on whether a custom location is selected
     */
    public void updateLocationButton() {
        // Find the button from view
        Button button = (Button) findViewById(R.id.UseCurrentLocation);

        // Enable/disable the button
        button.setEnabled(customLocationSet);
    }

    /**
     * When the use current location button is pressed in the ui
     * - Turn off auto location
     * - Find updated location
     * - Update ui (custom location not set)
     *
     * @param view  button that was pressed
     */
    public void useCurrentLocation(View view) {

        // Turn off auto location (should be impossible to press this if auto location is on, but just in case)
        setAutoLocation(false, true);

        // Update the location to current location
        updateLocation();
    }

    /**
     * Update location to current location
     * - Update the location in the helper
     * - Update ui (location name changed)
     * - Set the settings for the new (current) location
     */
    public void updateLocation() {

        // Set the custom location to be off
        customLocationSet = false;
        updateLocationButton();


        // Show the user that we are finding the location
        locationNameTextView.setText(R.string.finding_location);

        // Start a location helper to find the current location (pass the current context)
        locHelper = new LocationHelper(this) {

            // When the location changes
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);

                // Set the shown location to the current location
                locationNameTextView.setText(locHelper.locationName);

                // Set the settings based on the found location
                setSettingsForCurrentLocation();
            }
        };

        // Get an updated location
        locHelper.requestLocationUpdates();

    }

    /**
     * Finds the location from current location helper
     * - Sets the current settings to the location in the helper (current location)
     */
    public void setSettingsForCurrentLocation() {
        LatLng location = new LatLng(locHelper.lat, locHelper.lng);
        settingsLocation = new LocationPoint(location, locHelper.locationName, getTimezone(location));
    }


    /**
     * Called when auto location checkbox is changed
     * @param view the checkbox that was checked
     */
    public void setAutoLocationOn(View view) {
        CheckBox check = (CheckBox) view;
        // Set the auto location based on whether button is checked or not
        // (Pass "false" argument to not change the checkbox being checked or not, that's already done by the user)
        setAutoLocation(check.isChecked(), false);
    }

    /**
     * Set Auto location
     * - (Set check if needed)
     * - Set auto location setting to correct value
     * - Disable/enable parts of ui to select location
     *
     * @param on            Switch auto location on or off (true/false)
     * @param setCheck      True to change the check to reflect setting
     *                      (only needed if method was not called as a result of user i.e. when loaded from settings)
     */
    // Set auto location
    public void setAutoLocation(boolean on, boolean setCheck) {
        // Get all the views that we need to disable/enable
        Button currentLoc = (Button) findViewById(R.id.UseCurrentLocation);
        Button customLoc = (Button) findViewById(R.id.CustomLocation);
        CheckBox autoLocationCheck = (CheckBox) findViewById(R.id.AutoLocation);


        // Set the check if needed (e.g. when loaded from settings)
        if(setCheck) {
            autoLocationCheck.setChecked(on);
        }

        if(on) {

            // Set the auto location setting to be on
            settingLocationAuto = true;

            // Get the current location
            updateLocation();

            // Disable all other ui to choose a location
            customLoc.setEnabled(false);
            currentLoc.setEnabled(false);
            locationNameTextView.setEnabled(false);

        } else {
            // Set auto location setting off
            settingLocationAuto = false;

            // Enable ui to choose a location
            customLoc.setEnabled(true);
            currentLoc.setEnabled(true);
            locationNameTextView.setEnabled(true);

        }
    }


    /**
     * Return timezone from provided location
     * @param location the lat/lng to find the timezone for
     * @return The timezone ID as a String
     */
    public String getTimezone(LatLng location) {
        return TimeZoneMapper.latLngToTimezoneString(location.latitude, location.longitude);
    }

    /***
     *  Request location updates on startup, pause them on activity paused
     *

    /* Request updates at startup *
    @Override
    protected void onResume() {
        super.onResume();
        locHelper.requestLocationUpdates();
    }

    /* Remove the locationlistener updates when Activity is paused *
    @Override
    protected void onPause() {
        super.onPause();
        locHelper.removeUpdates();
    }
*/



    /**
     *  When the activity is over send all the settings back
     *  - Set the message to show user in Tasker plugin settings
     *  - Create a bundle with all the settings
     *  - Create an intent with bundle of settings, message and description of available Tasker variables created
     *
     */

    @Override
    public void finish()
    {

        // Stop requesting location updates for current location
        locHelper.removeUpdates();


        if (!isCanceled())
        {
            // The message is what is shown in the Tasker plugin settings
            String messageText = settingsLocation.locationName; // Set it to the name
            if(settingLocationAuto) messageText = getString(R.string.use_automatic_location); // unless auto location is on

            final String message = messageText;

            final Intent resultIntent = new Intent();

            /*
             * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
             * that anything placed in this Bundle must be available to Locale's class loader. So storing
             * String, int, and other standard objects will work just fine. Parcelable objects are not
             * acceptable, unless they also implement Serializable. Serializable objects must be standard
             * Android platform objects (A Serializable class private to this plug-in's APK cannot be
             * stored in the Bundle, as Locale's classloader will not recognize it).
             */
            final Bundle resultBundle =
                    PluginBundleManager.generateBundle(getApplicationContext(), message);

            // All the plugin action settings
            resultBundle.putDouble("loc_lat", settingsLocation.location.latitude);
            resultBundle.putDouble("loc_lng", settingsLocation.location.longitude);
            resultBundle.putString("loc_name", settingsLocation.locationName);
            resultBundle.putString("timezone", settingsLocation.timezone);
            resultBundle.putBoolean("loc_auto", settingLocationAuto);

            // Add the settings to the intent
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            // Set the action timeout to 1 second (to allow time for
            if (  TaskerPlugin.Setting.hostSupportsSynchronousExecution( getIntent().getExtras() ) )
                TaskerPlugin.Setting.requestTimeoutMS(resultIntent, 1000 );

            /*
             * The blurb is concise status text to be displayed in the host's UI.
             */
            final String blurb = generateBlurb(getApplicationContext(), message);
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

            // Description of Tasker variables this action makes available
            if ( TaskerPlugin.hostSupportsRelevantVariables( getIntent().getExtras() ) )
                TaskerPlugin.addRelevantVariableList( resultIntent, new String [] {
                        "%jd_short\nShort Date\nOnly the date <b>e.g. 9 Kislev</b>",
                        "%jd_long\nLong Date\nFull Date (may include \"Eve of\" prefix after sunset) <b>e.g. Eve of 9 Kislev</b>",
                        "%jd_desc\nDescription\nFull description includes parsha, special dates and relevant times <b>e.g. Parshat Beshalach</b>",
                        "%jd_loc\nLocation\nLocation for Zmanim calculations <b>e.g. Brooklyn, New York, USA</b>"
                } );

            // Set the result as ok and pass the intent
            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }

    /**
     * @param context Application context.
     * @param message The toast message to be displayed by the plug-in. Cannot be null.
     * @return A blurb for the plug-in.
     */
    /* package */static String generateBlurb(final Context context, final String message)
    {
        final int maxBlurbLength =
                context.getResources().getInteger(R.integer.twofortyfouram_locale_maximum_blurb_length);

        if (message.length() > maxBlurbLength)
        {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }
}