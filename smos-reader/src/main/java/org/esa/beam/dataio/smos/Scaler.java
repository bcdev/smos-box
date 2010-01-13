package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Scaling;

import java.awt.geom.Area;

class Scaler implements ValueProvider {

    private final ValueProvider provider;
    private final Scaling scaling;

    Scaler(ValueProvider provider, Scaling scaling) {
        this.provider = provider;
        this.scaling = scaling;
    }

    @Override
    public Area getEnvelope() {
        return provider.getEnvelope();
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
