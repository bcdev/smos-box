package org.esa.beam.dataio.smos;

import java.awt.geom.Area;
import java.io.IOException;

class L1cBrowseValueProvider extends SmosValueProvider {

    private final L1cBrowseSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;

    L1cBrowseValueProvider(L1cBrowseSmosFile smosFile, int memberIndex, int polarization) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarization;
    }

    @Override
    public final Area getArea() {
        return smosFile.getArea();
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    protected byte getByte(int gridPointIndex) throws IOException {
        return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getByte(memberIndex);
    }

    @Override
    protected short getShort(int gridPointIndex) throws IOException {
        return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getShort(memberIndex);
    }

    @Override
    protected int getInt(int gridPointIndex) throws IOException {
        return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getInt(memberIndex);
    }

    @Override
    protected float getFloat(int gridPointIndex) throws IOException {
        return smosFile.getBtDataList(gridPointIndex).getCompound(polarisation).getFloat(memberIndex);
    }
}
