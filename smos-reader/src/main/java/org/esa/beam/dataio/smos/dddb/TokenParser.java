package org.esa.beam.dataio.smos.dddb;


import java.awt.*;

class TokenParser {

    private static final String WILDCARD = "*";

    static String parseString(String token, String defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return trimmedToken;
    }

    static String parseString(String token) {
        return token.trim();
    }

    static int parseInt(String token, int defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return Integer.parseInt(trimmedToken);
    }

    private static boolean isWildcard(String trimmedToken) {
        return trimmedToken.equals(WILDCARD);
    }

    public static int parseHex(String token, int defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return Integer.parseInt(trimmedToken, 16);
    }

    public static double parseDouble(String token, double defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return Double.parseDouble(trimmedToken);
    }

    public static Color parseColor(String token, Color defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return new Color(Integer.parseInt(trimmedToken, 16));
    }

    public static boolean parseBoolean(String token, boolean defaultValue) {
        final String trimmedToken = token.trim();
        if (isWildcard(trimmedToken)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(trimmedToken);
    }
}
