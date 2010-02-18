package org.esa.beam.dataio.smos;

import java.io.IOException;

abstract class SmosValueProvider implements ValueProvider {

    @Override
    public byte getValue(int seqnum, byte noDataValue) {
        final int gridPointIndex = getGridPointIndex(seqnum);
        if (gridPointIndex == -1) {
            return noDataValue;
        }
        try {
            return getByte(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int seqnum, short noDataValue) {
        final int gridPointIndex = getGridPointIndex(seqnum);
        if (gridPointIndex == -1) {
            return noDataValue;
        }
        try {
            return getShort(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int seqnum, int noDataValue) {
        final int gridPointIndex = getGridPointIndex(seqnum);
        if (gridPointIndex == -1) {
            return noDataValue;
        }
        try {
            return getInt(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int seqnum, float noDataValue) {
        final int gridPointIndex = getGridPointIndex(seqnum);
        if (gridPointIndex == -1) {
            return noDataValue;
        }
        try {
            return getFloat(gridPointIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    protected abstract int getGridPointIndex(int seqnum);

    protected abstract byte getByte(int gridPointIndex) throws IOException;

    protected abstract short getShort(int gridPointIndex) throws IOException;

    protected abstract int getInt(int gridPointIndex) throws IOException;

    protected abstract float getFloat(int gridPointIndex) throws IOException;
}
