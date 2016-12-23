package com.masa34.nk225analyzer.Util.Validator;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

public class DateValidator {
    public static boolean isValid(String date, String format) {

        if(date.length() != format.length()) {
            return false;
        }

        DateFormat dateFormat = createStrictDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        return dateFormat.parse(date, pos) != null;
    }

    protected static DateFormat createStrictDateFormat(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}
