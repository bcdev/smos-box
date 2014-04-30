package org.esa.beam.dataio.smos.dddb;


class BandDescriptorImpl implements BandDescriptor {

    private final boolean visible;
    private final String bandName;
    private final String memberName;
    private final int polarization;
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
    private final String flagCodingName;
    private final Family<FlagDescriptor> flagDescriptors;

    BandDescriptorImpl(String[] tokens, Dddb dddb) {
        visible = TokenParser.parseBoolean(tokens[0], true);
        bandName = TokenParser.parseString(tokens[1]);
        memberName = TokenParser.parseString(tokens[2], bandName);
        polarization = TokenParser.parseInt(tokens[3], -1);
        sampleModel = TokenParser.parseInt(tokens[4], 0);

        scalingOffset = TokenParser.parseDouble(tokens[5], 0.0);
        scalingFactor = TokenParser.parseDouble(tokens[6], 1.0);

        typicalMin = TokenParser.parseDouble(tokens[7], Double.NEGATIVE_INFINITY);
        typicalMax = TokenParser.parseDouble(tokens[8], Double.POSITIVE_INFINITY);
        cyclic = TokenParser.parseBoolean(tokens[9], false);

        fillValue = TokenParser.parseDouble(tokens[10], Double.NaN);
        validPixelExpression = TokenParser.parseString(tokens[11], "").replaceAll("x", bandName);

        unit = TokenParser.parseString(tokens[12], "");
        description = TokenParser.parseString(tokens[13], "");
        flagCodingName = TokenParser.parseString(tokens[14], "");
        flagDescriptors = getFlagDescriptors(tokens[15], dddb);
    }

    private Family<FlagDescriptor> getFlagDescriptors(String token, Dddb dddb) {
        if (flagCodingName.isEmpty()) {
            return null;
        }
        return dddb.getFlagDescriptors(TokenParser.parseString(token));
    }

    @Override
    public final String getBandName() {
        return bandName;
    }

    @Override
    public final String getMemberName() {
        return memberName;
    }

    @Override
    public int getPolarization() {
        return polarization;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public final int getSampleModel() {
        return sampleModel;
    }

    @Override
    public final double getScalingOffset() {
        return scalingOffset;
    }

    @Override
    public final double getScalingFactor() {
        return scalingFactor;
    }

    @Override
    public final boolean hasTypicalMin() {
        return !Double.isInfinite(typicalMin);
    }

    @Override
    public final boolean hasTypicalMax() {
        return !Double.isInfinite(typicalMax);
    }

    @Override
    public final boolean hasFillValue() {
        return !Double.isNaN(fillValue);
    }

    @Override
    public final double getTypicalMin() {
        return typicalMin;
    }

    @Override
    public final double getTypicalMax() {
        return typicalMax;
    }

    @Override
    public final double getFillValue() {
        return fillValue;
    }

    @Override
    public final String getValidPixelExpression() {
        return validPixelExpression;
    }

    @Override
    public final String getUnit() {
        return unit;
    }

    @Override
    public final boolean isCyclic() {
        return cyclic;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getFlagCodingName() {
        return flagCodingName;
    }

    @Override
    public final Family<FlagDescriptor> getFlagDescriptors() {
        return flagDescriptors;
    }
}