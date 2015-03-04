package com.chaimchaikin.jewishdatetasker.helper;

import android.os.Bundle;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Chaim on 2015-01-02.
 *
 * Takes a location and makes variables for the Jewish date and various Zmanim
 */
public class JewishDateHelper {

    String locationName = "";
    double locationLat = 0;
    double locationLng = 0;
    TimeZone timeZone;


    public Bundle vars = new Bundle();


    public void setLocation(String name, double lat, double lng, String tZ) {
        locationName = name;
        locationLat = lat;
        locationLng = lng;

        timeZone = TimeZone.getTimeZone(tZ);
    }


    public void updateDates() {


        // Make an english calendar with the current date in the required timezone
        Calendar englishDate = Calendar.getInstance();
        englishDate.setTimeZone(timeZone);

        double elevation = 0; // Use 0 as the elevation always


        // Make a location point
        GeoLocation location = new GeoLocation(locationName, locationLat, locationLng, elevation, timeZone);
        // Get a Zmanim Calendar with that location
        ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);


        /**
         * See if we are after sunset
         */

        // Get a DateTime object with current times for the timezone we're dealing with
        DateTimeZone zone = DateTimeZone.forTimeZone(timeZone);
        DateTime currentTime = new DateTime(zone);

        // Get the sunset time in a DateTime object for comparison
        Date sunset = czc.getSunset();
        DateTime sunsetTime = new DateTime(sunset);

        // Initialize variables for prefixes to add after sunset
        String evePrefixHebrew = "";
        String evePrefixEnglish = "";

        // Day or night, useful for calling the day correctly in description of holidays
        // E.g. 1st Night of Chanukah (after sunset) vs 1st Day of Chanukah (after midnight)
        String dayOrNight;

        // Is it after sunset or not?
        boolean afterSunset;

        // If after sunset then
        if (currentTime.isAfter(sunsetTime)) {

            // Set the "eve" prefix
            evePrefixEnglish += "Eve of ";
            evePrefixHebrew = "ליל ";

            // Set it to be after sunset
            afterSunset = true;

            // Go to the next day
            // (Since we get the hebrew date from the english date, we want to get the hebrew date for
            // the next day as it is after sunset)
            englishDate.add(Calendar.DATE, 1);

            // Set it to night
            dayOrNight = "Night";
        } else {
            // Set to day
            dayOrNight = "Day";

            // Is not after sunset
            afterSunset = false;
        }

        // Store afterSunset as an accessible var
        vars.putBoolean("afterSunset", afterSunset);

        /**
         * Find the Jewish Date
         */

        // Make a new jewish date calendar
        JewishCalendar jewishDate = new JewishCalendar();
        // And find the hebrew date from the current english date
        jewishDate.setDate(englishDate);


        /**
         * Find the date for this coming Shabbos
         *
         * JewishCalendar can only get the parsha for the date of Shabbos itself
         * So we work out the date for the coming Shabbos
         *
        */

        // Make a calendar for finding the date for the coming Shabbos
        JewishCalendar jewishDateShabbos = new JewishCalendar();

        // Copy the current english date
        Calendar englishDateShabbos = englishDate;

        // Work out how many days to Shabbos (7 days in the week - day of the week)
        int daysToShabbos = 7 - jewishDate.getDayOfWeek();
        // Add days until we get to Shabbos
        englishDateShabbos.add(Calendar.DATE, daysToShabbos);

        // Set the Jewish date for shabbos
        jewishDateShabbos.setDate(englishDateShabbos);


        /**
         * Create formatters for the date
         */

        // In hebrew
        HebrewDateFormatter dateFormatterHebrew = new HebrewDateFormatter();
        dateFormatterHebrew.setHebrewFormat(true);
        // And english
        HebrewDateFormatter dateFormatterEnglish = new HebrewDateFormatter();


        /**
         * Get and save variables for the date
         */

        // Get the short date (just day of month and month name)
        String shortDate =  jewishDate.getJewishDayOfMonth() + " " + dateFormatterEnglish.formatMonth(jewishDate);

        // Get the long date (add possible "eve" prefix to short date)
        String longDate = evePrefixEnglish + shortDate;

        // Make English dates available as variables
        vars.putString("shortDate", shortDate);
        vars.putString("longDate", longDate);

        // Make hebrew date strings
        String shortHebrewDate = dateFormatterHebrew.format(jewishDate);
        String longHebrewDate = evePrefixHebrew + shortHebrewDate;

        // Make Hebrew dates available as variables
        vars.putString("shortHebrewDate", shortHebrewDate);
        vars.putString("longHebrewDate", longHebrewDate);


        /**
         * Check for any special days to add as a description
         */

        // Initialize description string
        String descriptionString = "";


        // Get and format the candle lighting in a good form
        Date candleLighting = czc.getCandleLighting();
        SimpleDateFormat candleLightingFormat = new SimpleDateFormat("h:m", Locale.US);
        String candleLightingTime = candleLightingFormat.format(candleLighting);


        // Show candle lighting only on 6th day of Hebrew Week (after midnight)
        if(jewishDate.getDayOfWeek() == 6 && !afterSunset) {
            descriptionString += "Candle Lighting: " + candleLightingTime;
            descriptionString += ", ";
        }

        // Get the parsha in hebrew and english
        String englishParsha = dateFormatterEnglish.formatParsha(jewishDateShabbos);
        String hebrewParsha = jewishDateShabbos.toString();

        // Add the parsha to the description string
        descriptionString += "Parshas " + englishParsha;

        // Make the parsha in English/Hebrew available as variables
        vars.putString("englishParsha", englishParsha);
        vars.putString("hebrewParsha", hebrewParsha);


        // Yom Tov or fast day
        if (jewishDate.isYomTov() || jewishDate.isTaanis()) {
            descriptionString += ", ";

            descriptionString += dateFormatterEnglish.formatYomTov(jewishDate);
        }

/*
        // TODO: On a fast day show fast starting/ending times
        if(jewishDate.isTaanis()) {

            descriptionString += ", ";

        }
*/

        // Chanukah
        if (jewishDate.isChanukah()) {
            descriptionString += ", ";
            descriptionString += jewishDate.getDayOfChanukah() + appendInt(jewishDate.getDayOfChanukah()) + " " + dayOrNight +  " of Chanukah";
        }
        // Rosh Chodesh
        if (jewishDate.isRoshChodesh()) {
            descriptionString += ", ";
            descriptionString += dateFormatterEnglish.formatRoshChodesh(jewishDate);
        }
        // Omer
        if(jewishDate.getDayOfOmer() > 0){
            descriptionString += ", ";
            descriptionString += dateFormatterHebrew.formatOmer(jewishDate);
        }

        // Make description available as a variable
        vars.putString("longText", descriptionString);

        /**
         * Get variables for Zmanim
         */

        // Get a date format (for time in 24 hour format)
        SimpleDateFormat zmanimFormat = new SimpleDateFormat("k:m", Locale.US);

        // Get and format the times
        String zmanimCandleLighting = zmanimFormat.format(czc.getCandleLighting());
        String zmanimSunset = zmanimFormat.format(czc.getSunset());
        //Date zmanimHavdolah = zmanimFormat.format(czc.get.??..);

        // Make zmanim available as variables
        vars.putString("zmanimSunset", zmanimSunset);
        vars.putString("zmanimCandleLighting", zmanimCandleLighting);
        //vars.putString("zmanimHavdolah", zmanimHavdolah);

    }


    public static String appendInt(int number) {
        String value = String.valueOf(number);
        if(value.length() > 1) {
            // Check for special case: 11 - 13 are all "th".
            // So if the second to last digit is 1, it is "th".
            char secondToLastDigit = value.charAt(value.length()-2);
            if(secondToLastDigit == '1')
                return "th";
        }
        char lastDigit = value.charAt(value.length()-1);
        switch(lastDigit) {
            case '1':
                return "st";
            case '2':
                return "nd";
            case '3':
                return "rd";
            default:
                return "th";
        }
    }


}
