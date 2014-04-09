package org.esa.beam.smos.ee2netcdf;


class VariableDescriptor {

    private String name;
    private boolean gridPointData;

    public VariableDescriptor(String name, boolean gridPointData) {
        this.name = name;
        this.gridPointData = gridPointData;
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
}
