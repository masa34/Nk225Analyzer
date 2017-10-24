package com.masa34.nk225analyzer.Util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final String TAG = "DateUtil";

    // コンストラクタ
    // インスタンス化禁止
    private DateUtils() {
    }

    public static Date convertToDate(String date, String format) throws ParseException {
        Log.d(TAG, "convertToDate");

        try {
            SimpleDateFormat fmt = new SimpleDateFormat(format);
            return fmt.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            throw e;
        }
    }

    public static Date getNow() {
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DATE);
        try {
            return convertToDate(String.format("%04d/%02d/%02d", y, m, d), "yyyy/MM/dd");
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int DifferenceDays(Date date1, Date date2) {
        return (int)(date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);
    }
}
