package fr.imelo.tracker.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gl on 21/03/2015.
 */
public abstract class DateUtil {

    final static String datePattern = "dd-MM-yyyy";
    final static String dateTimePattern = "dd-MM-yyyy kk:mm:ss";
    final static String timePattern = "kk:mm:ss";
    final static String dateTimeNoSpacePattern = "ddMMyyyykkmmss";

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        return sdf.format(new Date());
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimePattern);
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
        return sdf.format(new Date());
    }

    public static String getCurrentDateTimeNoSpace() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeNoSpacePattern);
        return sdf.format(new Date());
    }

}
