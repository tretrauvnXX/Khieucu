package vn.com.sonhasg.dynamiccalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by nguyenphuoc on 25-04-2017.
 */

public class AppConstants {

    public static ArrayList<EventModel> eventList = new ArrayList<>();

    public static Calendar main_calendar = Calendar.getInstance();

    public static SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");

    public static boolean isSaturdayOff = false;
    public static boolean isSundayOff = false;

    public static String strSaturdayOffBackgroundColor = "null";
    public static int saturdayOffBackgroundColor = -1;

    public static String strSaturdayOffTextColor = "null";
    public static int saturdayOffTextColor = -1;

    public static String strSundayOffBackgroundColor = "null";
    public static int sundayOffBackgroundColor = -1;

    public static String strSundayOffTextColor = "null";
    public static int sundayOffTextColor = -1;

    public static String strExtraDatesBackgroundColor = "null";
    public static int extraDatesBackgroundColor = -1;

    public static String strExtraDatesTextColor = "null";
    public static int extraDatesTextColor = -1;

    public static String strDatesBackgroundColor = "null";
    public static int datesBackgroundColor = -1;

    public static String strDatesTextColor = "null";
    public static int datesTextColor = -1;

    public static String strCurrentDateBackgroundColor = "null";
    public static int currentDateBackgroundColor = -1;

    public static String strCurrentDateTextColor = "null";
    public static int currentDateTextColor = -1;

    public static String strEventCellBackgroundColor = "null";
    public static int eventCellBackgroundColor = -1;

    public static String strEventCellTextColor = "null";
    public static int eventCellTextColor = -1;

}
