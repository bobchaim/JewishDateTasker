package com.chaimchaikin.jewishdatedashclockextension;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import java.io.Console;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class JewishDateDashClockExtension extends DashClockExtension {
    private static final String TAG = "JewishDate";

    public static final String PREF_NAME = "pref_name";

    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onUpdateData(int reason) {

        JewishDateHelper jewishDate = new JewishDateHelper();

        jewishDate.updateDates();

        String hebrewDateTextShort = jewishDate.getShortDate();
        String hebrewDateTextLong = jewishDate.getLongDate();
        String descriptionString = jewishDate.getLongText();

        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                        .visible(true)
                        .icon(R.drawable.jd_dashclock_logo)
                        .status(hebrewDateTextShort)
                        .expandedTitle(hebrewDateTextLong)
                        .expandedBody(descriptionString)
                //.contentDescription("Completely different text for accessibility if needed.")
                //.clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")))
        );

        int mId = 1;

        Bitmap largeIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.jd_dashclock_logo);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.jd_dashclock_logo)
                        //.setLargeIcon(largeIcon)
                        .setContentTitle(hebrewDateTextLong)
                        .setContentText(descriptionString)
                        //.setOngoing(true)
                        .setShowWhen(false)
                        .setPriority(NotificationCompat.PRIORITY_MIN);
// Creates an explicit intent for an Activity in your app
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, SettingsActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SettingsActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
                mNotificationManager.notify(mId, mBuilder.build());
/*
        int icon = R.drawable.jd_dashclock_logo;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, "Custom Notification", when);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.date_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.title, hebrewDateTextLong);
        contentView.setTextViewText(R.id.text, descriptionString);
        notification.contentView = contentView;

        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification

        mNotificationManager.notify(1, notification);*/
    }




}