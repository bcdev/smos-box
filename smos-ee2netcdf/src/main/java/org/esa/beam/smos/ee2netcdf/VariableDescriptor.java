package org.esa.beam.smos.ee2netcdf;


import ucar.ma2.DataType;

class VariableDescriptor {

    private String name;
    private boolean gridPointData;
    private DataType dataType;

    public VariableDescriptor(String name, boolean gridPointData, DataType dataType) {
        this.name = name;
        this.gridPointData = gridPointData;
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

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
