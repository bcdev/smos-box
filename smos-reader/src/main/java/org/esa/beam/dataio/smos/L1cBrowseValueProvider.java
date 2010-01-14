package org.esa.beam.dataio.smos;

import java.awt.geom.Area;
import java.io.IOException;

class L1cBrowseValueProvider implements ValueProvider {

    private final L1cBrowseSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;

    L1cBrowseValueProvider(L1cBrowseSmosFile smosFile, int memberIndex, int polarization) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarization;
    }

    @Override
    public Area getArea() {
        return smosFile.getArea();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    public byte getValue(int gridPointIndex, byte noDataValue) {
        try {
            return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getByte(memberIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        try {
            return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getShort(memberIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        try {
            return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getInt(memberIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        try {
            return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getFloat(memberIndex);
        } catch (IOException e) {
            return noDataValue;
        }
    }
}
