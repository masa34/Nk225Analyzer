package com.masa34.nk225analyzer.Util;

import java.util.Calendar;
import java.util.Date;

public class BusinessDay {
    public int year;
    public int month;
    public int day;

    public BusinessDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH) + 1;   // Calendar は 0 始まり
        this.day = cal.get(Calendar.DAY_OF_MONTH);
    }
}
