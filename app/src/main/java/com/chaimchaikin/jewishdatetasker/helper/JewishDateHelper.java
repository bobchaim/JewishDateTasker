package com.chaimchaikin.jewishdatetasker.helper;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Chaim on 2015-01-02.
 */
public class JewishDateHelper {

    String locationName = "";
    double locationLat = 0;
    double locationLng = 0;
    TimeZone timeZone;


    public String longDate = "";
    public String shortDate = "";
    public String longText = "";
    public String shortHebrewDate = "";
    public String longHebrewDate = "";


    public void setLocation(String name, double lat, double lng, String tZ) {
        locationName = name;
        locationLat = lat;
        locationLng = lng;

        timeZone = TimeZone.getTimeZone(tZ);
    }


    public void updateDates() {


        // Make an english calendar
        Calendar englishDate = Calendar.getInstance();

        englishDate.setTimeZone(timeZone);

        double elevation; //optional elevation
        elevation = 0;


        GeoLocation location = new GeoLocation(locationName, locationLat, locationLng, elevation, timeZone);
        ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);

        Date sunset = czc.getSunset();
        Date candleLighting = czc.getCandleLighting();
        SimpleDateFormat candleLightingFormat = new SimpleDateFormat("h:m");
        String candleLightingTime = candleLightingFormat.format(candleLighting);

        Date curTime = new Date();

        DateTimeZone zone = DateTimeZone.forTimeZone(timeZone);
        DateTime currentTime = new DateTime(zone);

        DateTime sunsetTime = new DateTime(sunset);

        SimpleDateFormat englishDateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy");
        englishDateFormat.setTimeZone(timeZone);

        String hebrewDateText = "";
        String englishDateText = englishDateFormat.format(curTime);

        String dayOrNight = "Day";
        String evePrefixHebrew = "";

        // If after sunset then
        if (currentTime.isAfter(sunsetTime)) {
            // Add "eve" prefix
            hebrewDateText += "Eve of ";
            evePrefixHebrew = "ליל ";

            // Go to the next day
            englishDate.add(Calendar.DATE, 1);

            dayOrNight = "Night";
        }

        // Find the jewish date
        JewishCalendar jewishDate = new JewishCalendar();
        //jewishDate.setJewishDate(5729, JewishDate.KISLEV, 28);
        jewishDate.setDate(englishDate);

        JewishCalendar jewishDateShabbos = new JewishCalendar();

        Calendar englishDateShabbos = englishDate;

        int daysToShabbos = 7 - jewishDate.getDayOfWeek();
        englishDateShabbos.add(Calendar.DATE, daysToShabbos);

        jewishDateShabbos.setDate(englishDateShabbos);


        // Create a new formatter for the hebrew date
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);

        // Add the date to the existing string of hebrew date
        hebrewDateText += jewishDate;

        // Use the date string for the title of the extension
        longDate = hebrewDateText;

        // Open the description
        String descriptionString = "";

        // Create a formatter for writing hebrew stuff in english
        HebrewDateFormatter hdfTransliterated = new HebrewDateFormatter();

        // Get the short date (just day of month and month name)
        shortDate =  jewishDate.getJewishDayOfMonth() + " " + hdfTransliterated.formatMonth(jewishDate);

        shortHebrewDate = hdf.format(jewishDate);

        longHebrewDate = evePrefixHebrew + shortHebrewDate;

        DateTime midnightToday = new DateTime().withTimeAtStartOfDay();

        // Show candle lighting only on 6th day of Hebrew Week (after midnight)
        if(jewishDate.getDayOfWeek() == 6  && currentTime.isAfter(midnightToday)) {
            descriptionString += "Candle Lighting: " + candleLightingTime;
        }

        if(descriptionString.length() > 0) {
            descriptionString += ", ";
        }

        // Check for any special days to add as a description
        descriptionString += "Parshas " + hdfTransliterated.formatParsha(jewishDateShabbos);

        // Yom Tov or fast day
        if (jewishDate.isYomTov() || jewishDate.isTaanis()) {
            descriptionString += ", ";

            descriptionString += hdfTransliterated.formatYomTov(jewishDate);
        }


        // Chanukah
        if (jewishDate.isChanukah()) {
            descriptionString += ", ";
            descriptionString += jewishDate.getDayOfChanukah() + appendInt(jewishDate.getDayOfChanukah()) + " " + dayOrNight +  " of Chanukah";
        }
        // Rosh Chodesh
        if (jewishDate.isRoshChodesh()) {
            descriptionString += ", ";
            descriptionString += hdfTransliterated.formatRoshChodesh(jewishDate);
        }
        // Omer
        if(jewishDate.getDayOfOmer() > 0){
            descriptionString += ", ";
            descriptionString += hdf.formatOmer(jewishDate);
        }


        longText = descriptionString;
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
