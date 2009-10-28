package org.esa.beam.dataio.smos;

public class BandDescriptor {

    private final boolean visible;
    private final String bandName;
    private final String memberName;
    private final int sampleModel;
    private final double scalingOffset;
    private final double scalingFactor;
    private final double typicalMin;
    private final double typicalMax;
    private final boolean cyclic;
    private final double fillValue;
    private final String validPixelExpression;
    private final String unit;
    private final String description;

    BandDescriptor(String[] tokens) {
        visible = getBoolean(tokens[0], true);
        bandName = getString(tokens[1]);
        memberName = getString(tokens[2], bandName);
        sampleModel = getInt(tokens[3], 0);

        scalingOffset = getDouble(tokens[4], 0.0);
        scalingFactor = getDouble(tokens[5], 1.0);

        typicalMin = getDouble(tokens[6], Double.NEGATIVE_INFINITY);
        typicalMax = getDouble(tokens[7], Double.POSITIVE_INFINITY);
        cyclic = getBoolean(tokens[8], false);

        fillValue = getDouble(tokens[9], Double.NaN);
        validPixelExpression = getString(tokens[10], "").replaceAll("x", bandName);

        unit = getString(tokens[11], "");
        description = getString(tokens[12], "");
    }

    public final boolean isVisible() {
        return visible;
    }

    public final String getBandName() {
        return bandName;
    }

    public final String getMemberName() {
        return memberName;
    }

    public final int getSampleModel() {
        return sampleModel;
    }

    public final double getScalingOffset() {
        return scalingOffset;
    }

    public final double getScalingFactor() {
        return scalingFactor;
    }

    public final boolean hasTypicalMin() {
        return !Double.isInfinite(typicalMin);
    }

    public final boolean hasTypicalMax() {
        return !Double.isInfinite(typicalMax);
    }

    public final boolean hasFillValue() {
        return !Double.isNaN(fillValue);
    }

    public final double getTypicalMin() {
        return typicalMin;
    }

    public final double getTypicalMax() {
        return typicalMax;
    }

    public final double getFillValue() {
        return fillValue;
    }

    public final String getValidPixelExpression() {
        return validPixelExpression;
    }

    public final String getUnit() {
        return unit;
    }

    public final boolean isCyclic() {
        return cyclic;
    }

    public final String getDescription() {
        return description;
    }

    private static String getString(String token) {
        return token.trim();
    }

    private static String getString(String token, String defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return token.trim();
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private static boolean getBoolean(String token, boolean defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Boolean.parseBoolean(token);
    }

    private static double getDouble(String token, double defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Double.parseDouble(token);
    }

    private static int getInt(String token, int defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Integer.parseInt(token);
    }
}
