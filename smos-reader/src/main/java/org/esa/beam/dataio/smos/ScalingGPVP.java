package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.util.Map;

class ScalingGPVP implements GridPointValueProvider {

    private final Band band;
    private final GridPointValueProvider gpvp;

    ScalingGPVP(Product product, String name, Map<String, GridPointValueProvider> providerMap) {
        band = product.getBand(name);
        gpvp = providerMap.get(name);
    }

    @Override
    public Area getRegion() {
        return gpvp.getRegion();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return gpvp.getGridPointIndex(seqnum);
    }

    @Override
    public byte getValue(int gridPointIndex, byte noDataValue) {
        return gpvp.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        return gpvp.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        return gpvp.getValue(gridPointIndex, noDataValue);
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        return (float) band.scale(gpvp.getValue(gridPointIndex, noDataValue));
    }
}
