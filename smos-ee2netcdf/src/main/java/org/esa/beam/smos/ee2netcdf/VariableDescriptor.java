package org.esa.beam.smos.ee2netcdf;


class VariableDescriptor {

    private String name;
    private boolean gridPointData;
    private boolean floatValue;

    public VariableDescriptor(String name, boolean gridPointData, boolean floatValue) {
        this.name = name;
        this.gridPointData = gridPointData;
        this.floatValue = floatValue;
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
}
