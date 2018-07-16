package vn.com.sonhasg.dynamiccalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nguyenphuoc on 25-04-2017.
 */

public class GlobalMethods {

    public static String convertDate(String inputDate, SimpleDateFormat inputFormat, SimpleDateFormat outputFormat) {

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(inputDate);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

}
