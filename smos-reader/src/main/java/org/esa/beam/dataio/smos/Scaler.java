package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Scaling;

import java.awt.geom.Area;
import java.io.IOException;

class Scaler extends SmosValueProvider {

    private final SmosValueProvider provider;
    private final Scaling scaling;

    Scaler(SmosValueProvider provider, Scaling scaling) {
        this.provider = provider;
        this.scaling = scaling;
    }

    @Override
    public Area getArea() {
        return provider.getArea();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return provider.getGridPointIndex(seqnum);
    }

    @Override
    protected final byte getByte(int gridPointIndex) throws IOException {
        return provider.getByte(gridPointIndex);
    }

    @Override
    protected final short getShort(int gridPointIndex) throws IOException {
        return provider.getShort(gridPointIndex);
    }

    @Override
    protected final int getInt(int gridPointIndex) throws IOException {
        return provider.getInt(gridPointIndex);
    }

    @Override
    protected final float getFloat(int gridPointIndex) throws IOException {
        return (float) scaling.scale(provider.getFloat(gridPointIndex));
    }
}
