package org.esa.beam.smos.visat.export;

import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.smos.gui.BindingConstants;

import java.io.File;

public class ExportParameter {

    @Parameter(alias = BindingConstants.SELECTED_PRODUCT)
    private boolean useSelectedProduct;

    @Parameter(alias = BindingConstants.SOURCE_DIRECTORY)
    private File sourceDirectory;

    @Parameter(alias = BindingConstants.OPEN_FILE_DIALOG)
    private boolean openFileDialog;

    @Parameter(alias = GridPointExportDialog.ALIAS_RECURSIVE, defaultValue = "false")
    private boolean recursive;

    @Parameter(alias = BindingConstants.ROI_TYPE, defaultValue = "2", valueSet = {"0", "1", "2"})
    private int roiType;

    @Parameter(alias = BindingConstants.GEOMETRY)
    private VectorDataNode geometry;    // @todo 4 tb/** rename - this is not a geometry tb 2014-05-28

    @Parameter(alias = BindingConstants.NORTH, defaultValue = "90.0", interval = "[-90.0, 90.0]")
    private double north;

    @Parameter(alias = BindingConstants.SOUTH, defaultValue = "-90.0", interval = "[-90.0, 90.0]")
    private double south;

    @Parameter(alias = BindingConstants.EAST, defaultValue = "180.0", interval = "[-180.0, 180.0]")
    private double east;

    @Parameter(alias = BindingConstants.WEST, defaultValue = "-180.0", interval = "[-180.0, 180.0]")
    private double west;

    @Parameter(alias = GridPointExportDialog.ALIAS_TARGET_FILE, notNull = true, notEmpty = true)
    private File targetFile;

    @Parameter(alias = GridPointExportDialog.ALIAS_EXPORT_FORMAT, defaultValue = GridPointExportDialog.NAME_CSV,
            valueSet = {GridPointExportDialog.NAME_CSV, GridPointExportDialog.NAME_EEF})
    private String exportFormat;

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

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRoiType(int roiType) {
        this.roiType = roiType;
    }

    public int getRoiType() {
        return roiType;
    }

    public VectorDataNode getGeometry() {
        return geometry;
    }

    public void setGeometry(VectorDataNode geometry) {
        this.geometry = geometry;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getNorth() {
        return north;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getSouth() {
        return south;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getEast() {
        return east;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public double getWest() {
        return west;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public ExportParameter getClone() {
        final ExportParameter clone = new ExportParameter();
        clone.setUseSelectedProduct(useSelectedProduct);
        clone.setSourceDirectory(new File(sourceDirectory.getPath()));
        clone.setOpenFileDialog(openFileDialog);
        clone.setRecursive(recursive);
        clone.setRoiType(roiType);
        clone.setGeometry(geometry);
        clone.setNorth(north);
        clone.setSouth(south);
        clone.setEast(east);
        clone.setWest(west);
        clone.setTargetFile(new File(targetFile.getPath()));
        clone.setExportFormat(exportFormat);
        return clone;
    }
}
