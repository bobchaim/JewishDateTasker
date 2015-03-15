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

package com.chaimchaikin.jewishdatetasker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.chaimchaikin.jewishdatetasker.Constants;
import com.chaimchaikin.jewishdatetasker.bundle.BundleScrubber;
import com.chaimchaikin.jewishdatetasker.helper.JewishDateHelper;
import com.chaimchaikin.jewishdatetasker.helper.TaskerPlugin;
import com.chaimchaikin.jewishdatetasker.ui.EditActivity;

import java.util.Locale;
import java.util.TimeZone;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class FireReceiver extends BroadcastReceiver
{

    /**
     * @param context {@inheritDoc}.
     * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING} Intent. This
     *            should contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by
     *            {@link EditActivity} and later broadcast by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {

        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         */

        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            if (Constants.IS_LOGGABLE) {
                Log.e(Constants.LOG_TAG,
                      String.format(Locale.US, "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

/*
        if (PluginBundleManager.isBundleValid(bundle)) {

        }
*/

        if ( isOrderedBroadcast() ) {

            // Set result OK
            setResultCode(TaskerPlugin.Setting.RESULT_CODE_OK);

            // Create a new JewishDateHelper to calculate times and dates
            JewishDateHelper jewishDate = new JewishDateHelper();

            // Initialize Variables to values in settings
            boolean autoLocation = bundle.getBoolean("loc_auto", false);
            String locName = bundle.getString("loc_name");
            double lat = bundle.getDouble("loc_lat");
            double lng = bundle.getDouble("loc_lng");
            double alt = bundle.getDouble("loc_alt");
            String timezone = bundle.getString("timezone");

            // If auto location is set, try to find a more up to date location
            if(autoLocation) {
                // Get the location from the variable
                String currentLocation = bundle.getString("tasker_location");
                // Split the location by the comma to get lat and lng
                String[] parts = currentLocation.split(",");
                lat = Double.parseDouble(parts[0]);
                lng = Double.parseDouble(parts[1]);

/*
                This is slow and possibly useless

                // LocationHelper helps us find location name and timezone
                LocationHelper locHelper = new LocationHelper(context);

                // Find the location name for this lat/lng
                locName = locHelper.getLocationName(lat, lng);
                // Find the timezone for this lat/lng
                timezone = locHelper.getTimezoneFromLocation(lat, lng);
*/

                // For the sake of speed use the set timezone and don't bother finding a location name
                timezone = TimeZone.getDefault().getID();
                locName = "Automatic";

            }

            // Set the location for the JewishDateHelper
            jewishDate.setLocation(locName, lat, lng, timezone, alt);

            // Update the dates
            jewishDate.updateDates();

            // Check support for returning variables
            if ( TaskerPlugin.Setting.hostSupportsVariableReturn( intent.getExtras() ) ) {

                // Create a new bundle
                Bundle vars = new Bundle();

                // Prefix for all variables
                String varPrefix = "%jewish_";

                // Loop through all the bundles in the main vars bundle
                for (String bundleKey: jewishDate.vars.keySet()) {

                    // Format the bundle key for the variable name
                    String formattedBundleKey = bundleKey.toLowerCase() + "_";

                    // Items in the misc bundle don't get a prefix in the variable name
                    if(bundleKey.equals("misc")) formattedBundleKey = "";

                    // Get the bundle we are dealing with
                    Bundle thisBundle = jewishDate.vars.getBundle(bundleKey);

                    // Loop through that bundle
                    for (String varKey: thisBundle.keySet()) {

                        // Get the next key formatted for variable name
                        String formattedKey = varKey.toLowerCase();


                        // Put a variable for each date part
                        vars.putString(varPrefix + formattedBundleKey + formattedKey, String.valueOf(thisBundle.get(varKey)));
                    }
                }

                // Location doesn't come from the bundle of vars so add it now
                vars.putString( varPrefix + "loc", locName );

                // Return the bundle of variables
                TaskerPlugin.addVariableBundle( getResultExtras( true ), vars );
            }
        }
    }
}