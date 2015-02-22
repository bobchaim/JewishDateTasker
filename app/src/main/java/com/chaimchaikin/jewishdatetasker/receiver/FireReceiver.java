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
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         */

        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format(Locale.US, "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

  /*      if (PluginBundleManager.isBundleValid(bundle))
        {

            //final String message = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
            //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
*/
        if ( isOrderedBroadcast() ) {

            setResultCode(TaskerPlugin.Setting.RESULT_CODE_OK);

            JewishDateHelper jewishDate = new JewishDateHelper();


            String locName;
            double lat;
            double lng;
            String timezone;



            /*if(bundle.getBoolean("loc_auto", false)) {
                LocationHelper locHelper = new LocationHelper(context);
                locHelper.updateLocation();

                locName = locHelper.locationName;
                lat = locHelper.lat;
                lng = locHelper.lng;
                timezone = locHelper.timezone;
            } else {*/
                locName = bundle.getString("loc_name");
                lat = bundle.getDouble("loc_lat");
                lng = bundle.getDouble("loc_lng");
                timezone = bundle.getString("timezone");
            //}



            jewishDate.setLocation(locName, lat, lng, timezone);

            jewishDate.updateDates();

            String hebrewDateTextShort = jewishDate.getShortDate();
            String hebrewDateTextLong = jewishDate.getLongDate();
            String descriptionString = jewishDate.getLongText();

            if ( TaskerPlugin.Setting.hostSupportsVariableReturn( intent.getExtras() ) ) {

                Bundle vars = new Bundle();
                vars.putString( "%jd_short", hebrewDateTextShort );
                vars.putString( "%jd_long", hebrewDateTextLong );
                vars.putString( "%jd_desc", descriptionString );
                vars.putString( "%jd_loc", locName );

                TaskerPlugin.addVariableBundle( getResultExtras( true ), vars );
            }
        }
    }
}