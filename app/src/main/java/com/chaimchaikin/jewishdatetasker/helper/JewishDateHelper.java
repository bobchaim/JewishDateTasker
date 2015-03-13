package com.chaimchaikin.jewishdatetasker.helper;

import android.os.Bundle;

import net.sourceforge.zmanim.ZmanimCalendar;
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

    // Location details
    String locationName = "";
    double locationLat = 0;
    double locationLng = 0;
    TimeZone timeZone;


    // Setup a Bundle for storing all of our vars
    public Bundle vars = new Bundle();


    // We'll need these later on
    HebrewDateFormatter dateFormatterHebrew;
    HebrewDateFormatter dateFormatterEnglish;


    public JewishDateHelper() {
        /**
         * Create formatters for the date
         */

        // In hebrew
        dateFormatterHebrew = new HebrewDateFormatter();
        dateFormatterHebrew.setHebrewFormat(true);
        // And english
        dateFormatterEnglish = new HebrewDateFormatter();
    }

    /**
     * Allow to set location from out of the class
     *
     * @param name Location name
     * @param lat Latitude
     * @param lng Longitude
     * @param tZ Timezone
     */
    public void setLocation(String name, double lat, double lng, String tZ) {
        locationName = name;
        locationLat = lat;
        locationLng = lng;

        timeZone = TimeZone.getTimeZone(tZ);
    }


    /**
     * Update dates and create variables with all possible information
     */
    public void updateDates() {

        /*
          Get our location and make a new Zmanim Calendar for that location
         */

        // Make an english calendar with the current date in the required timezone
        Calendar englishDate = Calendar.getInstance();
        englishDate.setTimeZone(timeZone);

        double elevation = 0; // Use 0 as the elevation always for now

        // Make a location point
        GeoLocation location = new GeoLocation(locationName, locationLat, locationLng, elevation, timeZone);

        // Get a Zmanim Calendar with that location
        ZmanimCalendar zmanimCalendar = new ZmanimCalendar(location);


        /*
          See if we are after sunset
         */

        // Get a DateTime object with current times for the timezone we're dealing with
        DateTimeZone zone = DateTimeZone.forTimeZone(timeZone);
        DateTime currentTime = new DateTime(zone);

        // Get the sunset time in a DateTime object for comparison
        Date sunset = zmanimCalendar.getSunset();
        DateTime sunsetTime = new DateTime(sunset);

        // Is it after sunset or not?
        boolean afterSunset = afterSunset(currentTime, sunsetTime);

        // If after sunset then
        if (afterSunset) {
            // Go to the next day
            // (Since we get the hebrew date from the english date, we want to get the hebrew date for
            // the next day as it is after sunset)
            englishDate.add(Calendar.DATE, 1);
        }


        /*
          Find the Jewish Date
         */

        // Make a new jewish date calendar
        JewishCalendar jewishDate = new JewishCalendar();
        // And find the hebrew date from the current english date
        jewishDate.setDate(englishDate);


        /*
            All other variables
         */

        // Store all misc vars in separate bundle
        Bundle misc = new Bundle();

        // Description of date
        misc.putString("desc", getDescription(jewishDate, englishDate, zmanimCalendar, afterSunset));

        // If it's currently after sunset
        misc.putBoolean("afterSunset", afterSunset);


        /*
           Store the variables
         */

        // Parsha
        vars.putBundle("parsha", getParsha(jewishDate, englishDate));

        // All date possibilities
        vars.putBundle("date", getDates(jewishDate, afterSunset));

        // Date parts
        vars.putBundle("dp", getDateParts(jewishDate));

        // Misc vars
        vars.putBundle("misc", misc);

        // Zmanim
        vars.putBundle("zmanim", getZmanim(zmanimCalendar));
    }

    /**
     * Get date in different formats and languages
     *
     * @param jewishDate current Jewish Date
     * @param afterSunset are we after sunset (but before midnight)
     * @return Bundle with date in long and short form in English and Hebrew
     */
    protected Bundle getDates(JewishCalendar jewishDate, boolean afterSunset) {

        Bundle dates = new Bundle();

        // Get the short date (just day of month and month name)
        String shortDate =  jewishDate.getJewishDayOfMonth() + " " + dateFormatterEnglish.formatMonth(jewishDate);

        // Get the long date (add possible "eve" prefix to short date)
        String longDate = getEvePrefixEnglish(afterSunset) + shortDate;

        // Make English dates available as variables
        dates.putString("short", shortDate);
        dates.putString("long", longDate);

        // Make hebrew date strings
        String shortHebrewDate = dateFormatterHebrew.format(jewishDate);
        String longHebrewDate = getEvePrefixHebrew(afterSunset) + shortHebrewDate;

        // Make Hebrew dates available as variables
        dates.putString("short_hebrew", shortHebrewDate);
        dates.putString("long_hebrew", longHebrewDate);

        return dates;
    }

    /**
     * Get the Jewish Date in parts (month, year, day) in English and Hebrew
     *
     * @param jewishDate current Jewish Date
     * @return Bundle with parts of Jewish Date
     */
    protected Bundle getDateParts(JewishCalendar jewishDate) {
        Bundle dateParts = new Bundle();

        // English date parts
        dateParts.putString("month", dateFormatterEnglish.formatMonth(jewishDate));
        dateParts.putString("day", String.valueOf(jewishDate.getJewishDayOfMonth()));
        dateParts.putString("year", String.valueOf(jewishDate.getJewishYear()));

        // Hebrew date parts TODO: get day and year in hebrew
        dateParts.putString("month_hebrew", dateFormatterHebrew.formatMonth(jewishDate));
        //dateParts.putString("day_hebrew", String.valueOf(jewishDate.getJewishDayOfMonth()));
        //dateParts.putString("year_hebrew", String.valueOf(jewishDate.getJewishYear()));

        return dateParts;
    }

    /**
     * Get a hebrew/english prefix to append to the date if we are after sunset
     * TODO: replace with string
     */

    /**
     * @param afterSunset are we after sunset (but before midnight)
     * @return English prefix if after sunset
     */
    protected String getEvePrefixEnglish(boolean afterSunset) {
        if(afterSunset) return "Eve of ";
        else return "";
    }

    /**
     * @param afterSunset are we after sunset (but before midnight)
     * @return Hebrew prefix if after sunset
     */
    protected String getEvePrefixHebrew(boolean afterSunset) {
        if(afterSunset) return "ליל ";
        else return "";
    }

    /**
     * Check if we are currently after sunset
     *
     * @param currentTime current time
     * @param sunsetTime time of sunset
     * @return true if current time is after sunset time
     */
    protected boolean afterSunset(DateTime currentTime, DateTime sunsetTime) {
        return currentTime.isAfter(sunsetTime);
    }


    /**
     * Find the date for this coming Shabbos
     *
     * JewishCalendar can only get the parsha for the date of Shabbos itself
     * So we work out the date for the coming Shabbos
     *
     * @param jewishDate current Jewish Date
     * @param englishDate current English Date
     * @return Jewish Date of upcoming Shabbos
     */
    protected JewishCalendar getJewishDateShabbos(JewishCalendar jewishDate, Calendar englishDate) {
        // Make a calendar for finding the date for the coming Shabbos
        JewishCalendar jewishDateShabbos = new JewishCalendar();

        // Work out how many days to Shabbos (7 days in the week - day of the week)
        int daysToShabbos = 7 - jewishDate.getDayOfWeek();
        // Add days until we get to Shabbos
        englishDate.add(Calendar.DATE, daysToShabbos);

        // Set the Jewish date for shabbos
        jewishDateShabbos.setDate(englishDate);

        return jewishDateShabbos;
    }

    /**
     * Day or night, useful for calling the day correctly in description of holidays
     * E.g. 1st Night of Chanukah (after sunset) vs 1st Day of Chanukah (after midnight)
     *
     * @param afterSunset are we after sunset (but before midnight)
     * @return Night if afterSunset is true, Day if false
     */
    protected String getDayOrNight(boolean afterSunset) {
        if(afterSunset) {
            return "Night";
        } else {
            return "Day";
        }
    }

    /**
     * Get an informative description of the current date
     * - Parsha of the week
     * - Candle lighting time (on Friday)
     * - Rosh Chodesh, Yom Tov, Omer etc. (if present)
     *
     * @param jewishDate current Jewish Date
     * @param englishDate current English Date
     * @param zmanimCalendar a ZmanimCalendar with the date to get zmanim for
     * @param afterSunset are we after sunset (but before midnight)
     * @return String with description of date
     */
    protected String getDescription(JewishCalendar jewishDate, Calendar englishDate, ZmanimCalendar zmanimCalendar, boolean afterSunset) {

        // Initialize description string
        String descriptionString = "";

        // Get and format the candle lighting in a good form
        Date candleLighting = zmanimCalendar.getCandleLighting();
        SimpleDateFormat candleLightingFormat = new SimpleDateFormat("h:m", Locale.US);
        String candleLightingTime = candleLightingFormat.format(candleLighting);

        // Show candle lighting only on 6th day of Hebrew Week (after midnight)
        if(jewishDate.getDayOfWeek() == 6 && !afterSunset) {
            descriptionString += "Candle Lighting: " + candleLightingTime;
            descriptionString += ", ";
        }

        // Get the english parsha
        String englishParsha = getParsha(jewishDate, englishDate).getString("english");

        // Add the parsha to the description string
        descriptionString += "Parshas " + englishParsha;


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
            // The day number of Chanukah + suffix for that number (e.g. nd for 2nd) + day/night + / of Chanukah
            descriptionString += jewishDate.getDayOfChanukah() + appendInt(jewishDate.getDayOfChanukah()) + " " + getDayOrNight(afterSunset) +  " of Chanukah";
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

        return descriptionString;
    }

    /**
     * Get the parsha in English and Hebrew for a given date
     *
     * @param jewishDate current Jewish Date
     * @param englishDate current English Date
     * @return Bundle with the parsha in English and Hebrew
     */
    protected Bundle getParsha(JewishCalendar jewishDate, Calendar englishDate) {
        JewishCalendar jewishDateShabbos = getJewishDateShabbos(jewishDate, englishDate);

        // Get the parsha in hebrew and english
        String englishParsha = dateFormatterEnglish.formatParsha(jewishDateShabbos);
        String hebrewParsha = jewishDateShabbos.toString();

        // Make a new bundle for the parsha
        Bundle parsha = new Bundle();

        // Make the parsha in English/Hebrew available as variables
        parsha.putString("english", englishParsha);
        parsha.putString("hebrew", hebrewParsha);

        return parsha;
    }

    /**
     * Gets zmanim based on a given Zmanim Calendar
     *
     * @param zmanimCalendar a ZmanimCalendar with the date to get zmanim for
     * @return a Bundle containing times for zmanim
     */
    protected Bundle getZmanim(ZmanimCalendar zmanimCalendar) {

        // Make a new bundle of zmanim
        Bundle zmanim = new Bundle();

        // Put zmanim in the bundle
        zmanim.putString("Sunrise", getTimestampStringFromDate(zmanimCalendar.getSunrise()) );
        zmanim.putString("Sunset", getTimestampStringFromDate(zmanimCalendar.getSunset()));
        zmanim.putString("Candle_Lighting", getTimestampStringFromDate(zmanimCalendar.getCandleLighting()));
        zmanim.putString("Mincha_Ketana", getTimestampStringFromDate(zmanimCalendar.getMinchaKetana()));
        zmanim.putString("Mincha_Gedolah", getTimestampStringFromDate(zmanimCalendar.getMinchaGedola()) );
        zmanim.putString("Chatzos", getTimestampStringFromDate(zmanimCalendar.getChatzos()) );
        zmanim.putString("Sof_Zman_Shma_MGA", getTimestampStringFromDate(zmanimCalendar.getSofZmanShmaMGA()) );
        zmanim.putString("Sof_Zman_Shma_GRA", getTimestampStringFromDate(zmanimCalendar.getSofZmanShmaGRA()) );
        zmanim.putString("Plag_Hamincha", getTimestampStringFromDate(zmanimCalendar.getPlagHamincha()) );
        zmanim.putString("Sof_Zman_Tfila_GRA", getTimestampStringFromDate(zmanimCalendar.getSofZmanTfilaGRA()) );
        zmanim.putString("Sof_Zman_Tfila_MGA", getTimestampStringFromDate(zmanimCalendar.getSofZmanTfilaMGA()) );

        return zmanim;
    }

    /**
     * Get a Timestamp (since epoch) in seconds for a given date/time as a String
     *
     * @param date date/time to calculate timestamp for
     * @return String with Timestamp
     */
    protected String getTimestampStringFromDate(Date date) {
        // Get seconds of the time by getting milliseconds divided by 1000 and return as String
        return String.valueOf(new DateTime(date).getMillis() / 1000);
    }

    /**
     * Returns the correct suffix for a given number (e.g. 1st, 2nd, 23rd, 12th etc.)
     *
     * @param number number to find suffix for
     * @return correct suffix
     */
    protected static String appendInt(int number) {
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
