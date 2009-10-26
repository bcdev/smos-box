package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Scaling;

import java.awt.geom.Area;

class ScalingDecorator implements FieldValueProvider {

    private final FieldValueProvider provider;
    private final Scaling scaling;

    ScalingDecorator(FieldValueProvider provider, Scaling scaling) {
        this.provider = provider;
        this.scaling = scaling;
    }

    @Override
    public Area getRegion() {
        return provider.getRegion();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return provider.getGridPointIndex(seqnum);
    }

    @Override
    public byte getValue(int gridPointIndex, byte noDataValue) {
        return provider.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        return provider.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        return provider.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        return (float) scaling.scale(provider.getValue(gridPointIndex, noDataValue));
    }
}
