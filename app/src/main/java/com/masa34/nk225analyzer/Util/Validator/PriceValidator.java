package com.masa34.nk225analyzer.Util.Validator;

public class PriceValidator {
    public static boolean isValid(String price) {
        try {
            if (Float.parseFloat(price) >= 0.0f) {
                return true;
            }
        } catch (NumberFormatException e) {
        }
        return false;
    }
}
