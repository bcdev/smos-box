package org.esa.beam.smos.ee2netcdf;


import ucar.ma2.DataType;

class VariableDescriptor {

    private String name;
    private boolean gridPointData;
    private boolean floatValue;
    private DataType dataType;

    public VariableDescriptor(String name, boolean gridPointData, boolean floatValue, DataType dataType) {
        this.name = name;
        this.gridPointData = gridPointData;
        this.floatValue = floatValue;
        this.dataType = dataType;
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

    public boolean isFloatValue() {
        return floatValue;
    }

    public void setFloatValue(boolean floatValue) {
        this.floatValue = floatValue;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
