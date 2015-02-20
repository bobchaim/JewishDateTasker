package com.chaimchaikin.jewishdatedashclockextension;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.apps.dashclock.api.ExtensionData;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "JewishDate";

    public static final String PREF_NAME = "pref_name";

    private LocationManager locationManager;
    private String provider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            JewishDateHelper jewishDate = new JewishDateHelper();

            jewishDate.updateDates();

            String hebrewDateTextShort = jewishDate.getShortDate();
            String hebrewDateTextLong = jewishDate.getLongDate();
            String descriptionString = jewishDate.getLongText();

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
            //mNotificationManager.notify(mId, mBuilder.build());
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
