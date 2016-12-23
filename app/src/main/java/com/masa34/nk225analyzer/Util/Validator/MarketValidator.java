package com.masa34.nk225analyzer.Util.Validator;

public class MarketValidator {
    public static boolean isValid(String[] values) {

        if (values.length < 9) {
            return false;
        }

        if (!DateValidator.isValid(values[0], "yyyy-MM-dd")) {
            return false;
        }

        for (int i = 1; i < 9; ++i) {
            try {
                if (Long.parseLong(values[i]) < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
