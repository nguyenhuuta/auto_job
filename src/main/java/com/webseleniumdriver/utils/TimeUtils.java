package com.webseleniumdriver.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {
    public static final String formatDate1 = "yyyy-MM-dd HH:mm:ss";
    public static final String formatDate2 = "yyyy-MM-dd";
    public static final String formatDate3 = "yyyyMMdd";
    public static final String formatDate4 = "yyyyMMddHHmm";
    public static final String formatDate5 = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String formatDate6 = "yyyy/MM/dd";
    public static final String formatDate7 = "HH:mm:ss MM-dd-yyyy";
    public static final String formatDate8 = "yyyyMMddHHmmss";
    public static final String ddMM = "ddMM";

    public static final int M_5_MINUTES = 300000;
    public static final int M_10_MINUTE = 600000;
    public static final long M_30_MINUTES = 1800000;
    public static final long M_40_MINUTES = 2400000;
    public static final long M_1_HOURS = 3600000;
    public static final long M_2_HOURS = 7200000;
    public static final long M_3_HOURS = 10800000;
    public static final long M_1_MINUTES = 60000;
    public static final long M_2_MINUTES = 120000;
    public static final long M_4_HOURS = 14400000L;
    public static final long M_1_DAY = 86400000L;

    public static String formatDate(String dateString, String beforeFormat, String afterFormat) {
        Date date = getDateFromString(dateString, beforeFormat);
        return getStringFromDate(date, afterFormat);
    }

    public static Date getDateFromString(String dateString, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date outDate;
        try {
            outDate = df.parse(dateString);
            return outDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getLongFromString(String dateString, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date outDate;
        try {
            outDate = df.parse(dateString);
            return outDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getStringFromDate(Date date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        String reportDate = df.format(date);
        return reportDate;
    }

    public static String getCurrentDate(String format) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return getStringFromDate(date, format);
    }

    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return date;
    }

    public static String getCurrentDateUTC(String format) {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.format(DateTimeFormatter.ofPattern(format));
    }

    public static long getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static Date addTime(Date date, int day, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, day);
        c.add(Calendar.HOUR_OF_DAY, hour);
        c.add(Calendar.MINUTE, min);
        return c.getTime();
    }
}
