package org.esa.beam.dataio.smos.dddb;


public class MemberDescriptor {

    private String name;
    private boolean gridPointData;
    private String dataTypeName;
    private String dimensionNames;
    private int memberIndex;

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
}
