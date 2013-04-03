package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.VectorDataNode;

import java.io.File;

public class ExportParameter {
    private boolean useSelectedProduct;
    private File sourceDirectory;
    private boolean openFileDialog;
    private VectorDataNode geometry;
    private int roiType;

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
}
