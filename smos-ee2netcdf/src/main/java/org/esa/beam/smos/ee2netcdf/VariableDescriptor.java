package org.esa.beam.smos.ee2netcdf;


import ucar.ma2.DataType;

class VariableDescriptor {

    private String name;
    private boolean gridPointData;
    private DataType dataType;
    private String dimensionNames;
    private boolean is2d;

    public VariableDescriptor(String name, boolean gridPointData, DataType dataType, String dimensionNames, boolean is2d) {
        this.name = name;
        this.gridPointData = gridPointData;
        this.dataType = dataType;
        this.is2d = is2d;
        this.dimensionNames = dimensionNames;
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
}
