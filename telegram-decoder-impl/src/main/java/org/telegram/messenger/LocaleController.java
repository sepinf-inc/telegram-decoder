package org.telegram.messenger;

public class LocaleController {

    public static String formatNumber(long asDecimal, char thousandsSeparator) {
        return Long.toString(asDecimal);
    }

    public static String formatPluralStringSpaced(String string, int asDecimal) {
        return string + " " + asDecimal;
    }

    public static String formatString(String string, String asDecimalString) {
        return string + " " + asDecimalString;
    }

}
