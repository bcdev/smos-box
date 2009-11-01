package org.esa.beam.dataio.smos;

public interface BandDescriptor {

    String getBandName();

    String getMemberName();

    boolean isVisible();

    int getSampleModel();

    double getScalingOffset();

    double getScalingFactor();

    boolean hasTypicalMin();

    boolean hasTypicalMax();

    boolean hasFillValue();

    double getTypicalMin();

    double getTypicalMax();

    double getFillValue();

    String getValidPixelExpression();

    String getUnit();

    boolean isCyclic();

    String getDescription();

    String getFlagCodingName();

    Family<FlagDescriptorI> getFlagDescriptors();
}
