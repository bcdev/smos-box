package org.esa.beam.dataio.smos.dddb;


public class MemberDescriptor {

    private String name;
    private boolean gridPointData;
    private String dataTypeName;
    private String dimensionNames;
    private int memberIndex;
    private short[] flagMasks;
    private short[] flagValues;
    private String flagMeanings;
    private String unit;
    private float fillValue;
    private float scalingFactor;
    private float scalingOffset;

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

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDimensionNames(String dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    public String getDimensionNames() {
        return dimensionNames;
    }

    public void setMemberIndex(int memberIndex) {
        this.memberIndex = memberIndex;
    }

    public int getMemberIndex() {
        return memberIndex;
    }

    public void setFlagMasks(short[] flagMasks) {
        this.flagMasks = flagMasks;
    }

    public short[] getFlagMasks() {
        return flagMasks;
    }

    public void setFlagValues(short[] flagValues) {
        this.flagValues = flagValues;
    }

    public short[] getFlagValues() {
        return flagValues;
    }

    public void setFlagMeanings(String flagMeanings) {
        this.flagMeanings = flagMeanings;
    }

    public String getFlagMeanings() {
        return flagMeanings;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public void setFillValue(float fillValue) {
        this.fillValue = fillValue;
    }

    public float getFillValue() {
        return fillValue;
    }

    public void setScalingFactor(float scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public float getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingOffset(float scalingOffset) {
        this.scalingOffset = scalingOffset;
    }

    public float getScalingOffset() {
        return scalingOffset;
    }
}
