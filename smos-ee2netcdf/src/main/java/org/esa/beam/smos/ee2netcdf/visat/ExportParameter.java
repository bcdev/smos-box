package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.VectorDataNode;

import java.io.File;

public class ExportParameter {

    private boolean useSelectedProduct;
    private File sourceDirectory;
    private boolean openFileDialog;
    private VectorDataNode geometry;
    private int roiType;
    private double north;
    private double east;
    private double south;
    private double west;
    private File targetDirectory;

    public ExportParameter() {
        north = 90.0;
        south = -90.0;
        west = -180.0;
        east = 180.0;
    }

    public void setUseSelectedProduct(boolean useSelectedProduct) {
        this.useSelectedProduct = useSelectedProduct;
    }

    public boolean isUseSelectedProduct() {
        return useSelectedProduct;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setOpenFileDialog(boolean openFileDialog) {
        this.openFileDialog = openFileDialog;
    }

    public boolean isOpenFileDialog() {
        return openFileDialog;
    }

    public void setGeometry(VectorDataNode geometry) {
        this.geometry = geometry;
    }

    public VectorDataNode getGeometry() {
        return geometry;
    }

    public void setRoiType(int roiType) {
        this.roiType = roiType;
    }

    public int getRoiType() {
        return roiType;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getNorth() {
        return north;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getEast() {
        return east;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getSouth() {
        return south;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public double getWest() {
        return west;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }
}
