package com.masa34.nk225analyzer.Util.Validator;

public class CandlestickValidator {
    public static boolean isValid(String[] values) {

        if (values.length < 5) {
            return false;
        }

        if (!DateValidator.isValid(values[0], "yyyy-MM-dd")) {
            return false;
        }

        if (!PriceValidator.isValid(values[1])) {
            return false;
        }

        if (!PriceValidator.isValid(values[2])) {
            return false;
        }

        if (!PriceValidator.isValid(values[3])) {
            return false;
        }

        if (!PriceValidator.isValid(values[4])) {
            return false;
        }

        return true;
    }
}
