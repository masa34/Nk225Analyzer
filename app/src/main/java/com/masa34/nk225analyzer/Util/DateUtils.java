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

    public static Date convertToDate(String dateString) throws ParseException {
        Log.d(TAG, "convertToDate");

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            return fmt.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            throw e;
        }
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
}
