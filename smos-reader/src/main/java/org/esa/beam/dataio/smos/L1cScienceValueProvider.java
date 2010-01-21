package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;

import java.awt.geom.Area;
import java.io.IOException;

public class L1cScienceValueProvider implements ValueProvider {

    private final L1cScienceSmosFile smosFile;
    private final int memberIndex;
    private final int polarisation;
    private volatile long snapshotId;

    L1cScienceValueProvider(L1cScienceSmosFile smosFile, int memberIndex, int polarization) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
        this.polarisation = polarization;
        this.snapshotId = -1;
    }

    // todo - make the snapshot ID a field of the L1C SMOS file
    public long getSnapshotId() {
        return snapshotId;
    }

    // todo - make the snapshot ID a field of the L1C SMOS file
    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
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
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueByte(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getByte(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueShort(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getShort(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueInt(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getInt(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        final long snapshotId = getSnapshotId();
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtDataValueFloat(gridPointIndex, memberIndex, polarisation, noDataValue);
            } else {
                final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
                if (data != null) {
                    return data.getFloat(memberIndex);
                } else {
                    return noDataValue;
                }
            }
        } catch (IOException e) {
            return noDataValue;
        }
    }
}
