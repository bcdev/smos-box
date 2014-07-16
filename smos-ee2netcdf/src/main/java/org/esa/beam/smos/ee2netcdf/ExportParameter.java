package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.smos.gui.BindingConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportParameter {

    /**
     * Valid source product types are
     * <p/>
     * MIR_SM_SMUDP2
     * MIR_SM_OSUDP2
     * MIR_SM_SCxF1C
     * MIR_SM_SCxD1C
     * MIR_SM_BWxF1C
     * MIR_SM_BWxD1C
     */
    public static final String PRODUCT_TYPE_REGEX = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2";

    @Parameter(alias = BindingConstants.SELECTED_PRODUCT, label = "Use selected product")
    private boolean useSelectedProduct;

    @Parameter(alias = BindingConstants.SOURCE_DIRECTORY, label = "Source directory")
    private File sourceDirectory;

    @Parameter(alias = BindingConstants.OPEN_FILE_DIALOG, label = "Open file dialog")
    private boolean openFileDialog;

    private Geometry geometry;

    // @todo adapt Binding constant name   tb 2014-05-28
    // @Parameter(alias = BindingConstants.GEOMETRY)
    // private VectorDataNode geometryNode;

    @Parameter(alias = BindingConstants.ROI_TYPE, defaultValue = "2", valueSet = {"0", "1", "2"}, label = "ROI type")
    private int roiType;

    @Parameter(label = "Target directory")
    private File targetDirectory;
    @Parameter(interval = "[-90.0, 90.0]", label = "Northern bound")
    private double northBound;
    @Parameter(interval = "[-180.0, 180.0]", label = "Eastern bound")
    private double eastBound;
    @Parameter(interval = "[-90.0, 90.0]", label = "Southern bound")
    private double southBound;
    @Parameter(interval = "[-180.0, 180.0]", label = "Western bound")
    private double westBound;
    @Parameter(description = "Overwrite the target product", label = "Overwrite target")
    private boolean overwriteTarget;
    @Parameter(description = "The contact address to be included in the meta data of the target product", label = "Contact")
    private String contact;
    @Parameter(description = "The institution to be included in the mata data of the target product", label = "Institution")
    private String institution;
    @Parameter(description = "The output band names", label = "Output band names")
    private List<String> outputBandNames;

    public ExportParameter() {
        northBound = 90.0;
        southBound = -90.0;
        westBound = -180.0;
        eastBound = 180.0;
        outputBandNames = new ArrayList<>();
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

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setRoiType(int roiType) {
        this.roiType = roiType;
    }

    public int getRoiType() {
        return roiType;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public String toAreaWKT() {
        final StringBuilder wktBuilder = new StringBuilder(128);
        wktBuilder.append("POLYGON((");
        appendCoordinate(wktBuilder, westBound, northBound, true);
        appendCoordinate(wktBuilder, eastBound, northBound, true);
        appendCoordinate(wktBuilder, eastBound, southBound, true);
        appendCoordinate(wktBuilder, westBound, southBound, true);
        appendCoordinate(wktBuilder, westBound, northBound, false);
        wktBuilder.append("))");

        return wktBuilder.toString();
    }

    public void setNorthBound(double northBound) {
        this.northBound = northBound;
    }

    public double getNorthBound() {
        return northBound;
    }

    public void setEastBound(double eastBound) {
        this.eastBound = eastBound;
    }

    public double getEastBound() {
        return eastBound;
    }

    public void setSouthBound(double southBound) {
        this.southBound = southBound;
    }

    public double getSouthBound() {
        return southBound;
    }

    public void setWestBound(double westBound) {
        this.westBound = westBound;
    }

    public double getWestBound() {
        return westBound;
    }

    public void setOverwriteTarget(boolean overwriteTarget) {
        this.overwriteTarget = overwriteTarget;
    }

    public boolean isOverwriteTarget() {
        return overwriteTarget;
    }

    private void appendCoordinate(StringBuilder wktBuilder, double x, double y, boolean appendComma) {
        wktBuilder.append(x);
        wktBuilder.append(" ");
        wktBuilder.append(y);
        if (appendComma) {
            wktBuilder.append(",");
        }
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getInstitution() {
        return institution;
    }

    public void setOutputBandNames(List<String> outputBandNames) {
        this.outputBandNames = outputBandNames;
    }

    public List<String> getOutputBandNames() {
        return outputBandNames;
    }
}
