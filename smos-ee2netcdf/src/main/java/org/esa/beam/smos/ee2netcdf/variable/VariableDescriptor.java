package org.esa.beam.smos.ee2netcdf.variable;


import ucar.ma2.DataType;

public class VariableDescriptor {

    private String name;
    private boolean gridPointData;
    private DataType dataType;
    private String dimensionNames;
    private boolean is2d;
    private int btDataMemberIndex;
    private String unit;
    private float fillValue;
    private boolean fillValuePresent;
    private boolean validMinPresent;
    private float validMin;
    private boolean validMaxPresent;
    private float validMax;
    private String originalName;
    private String standardName;
    private short[] flagMasks;
    private String flagMeanings;
    double scaleFactor;
    private boolean scaleFactorPresent;
    private boolean unsigned;

    public boolean isUnsigned() {
        return unsigned;
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        scaleFactorPresent = true;
    }

    public String getFlagMeanings() {
        return flagMeanings;
    }

    public void setFlagMeanings(String flagMeanings) {
        this.flagMeanings = flagMeanings;
    }

    public short[] getFlagValues() {
        return flagValues;
    }

    public void setFlagValues(short[] flagValues) {
        this.flagValues = flagValues;
    }

    private short[] flagValues;

    public short[] getFlagMasks() {
        return flagMasks;
    }

    public void setFlagMasks(short[] flagMasks) {
        this.flagMasks = flagMasks;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public VariableDescriptor(String name, boolean gridPointData, DataType dataType, String dimensionNames, boolean is2d, int btDataMemberIndex) {
        this();
        this.name = name;
        this.gridPointData = gridPointData;
        this.dataType = dataType;
        this.is2d = is2d;
        this.dimensionNames = dimensionNames;
        this.btDataMemberIndex = btDataMemberIndex;
    }

    public VariableDescriptor() {
        fillValue = Float.NaN;
        validMin = Float.NaN;
        validMax = Float.NaN;
        scaleFactor = Double.NaN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGridPointData() {
        return gridPointData;
    }

    public void setGridPointData(boolean gridPointData) {
        this.gridPointData = gridPointData;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getDimensionNames() {
        return dimensionNames;
    }

    public void setDimensionNames(String dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    public boolean isIs2d() {
        return is2d;
    }

    public void setIs2d(boolean is2d) {
        this.is2d = is2d;
    }

    public int getBtDataMemberIndex() {
        return btDataMemberIndex;
    }

    public void setBtDataMemberIndex(int btDataMemberIndex) {
        this.btDataMemberIndex = btDataMemberIndex;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isFillValuePresent() {
        return fillValuePresent;
    }

    public float getFillValue() {
        return fillValue;
    }

    public void setFillValue(float fillValue) {
        this.fillValue = fillValue;
        fillValuePresent = true;
    }

    public boolean isValidMinPresent() {
        return validMinPresent;
    }

    public float getValidMin() {
        return validMin;
    }

    public void setValidMin(float validMin) {
        this.validMin = validMin;
        validMinPresent = true;
    }

    public boolean isValidMaxPresent() {
        return validMaxPresent;
    }

    public float getValidMax() {
        return validMax;
    }

    public void setValidMax(float validMax) {
        this.validMax = validMax;
        validMaxPresent = true;
    }

    public boolean isScaleFactorPresent() {
        return scaleFactorPresent;
    }
}
