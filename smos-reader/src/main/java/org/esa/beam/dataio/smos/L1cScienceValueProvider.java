/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;

import java.awt.geom.Area;
import java.io.IOException;
import java.text.MessageFormat;

public class L1cScienceValueProvider extends SmosValueProvider {

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

    public final long getSnapshotId() {
        return snapshotId;
    }

    public final void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public Area getArea() {
        if (snapshotId == -1) {
            return smosFile.getArea();
        } else {
            return smosFile.getSnapshotInfo().getArea(snapshotId);
        }
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    protected byte getByte(int gridPointIndex) throws IOException {
        final long snapshotId = getSnapshotId();
        if (snapshotId == -1) {
            return smosFile.getBrowseBtDataValueByte(gridPointIndex, memberIndex, polarisation);
        } else {
            final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
            if (data != null) {
                return data.getByte(memberIndex);
            }
            throw new IOException(MessageFormat.format("No data found for grid point ''{0}''.", gridPointIndex));
        }
    }

    @Override
    protected short getShort(int gridPointIndex) throws IOException {
        final long snapshotId = getSnapshotId();
        if (snapshotId == -1) {
            return smosFile.getBrowseBtDataValueShort(gridPointIndex, memberIndex, polarisation);
        } else {
            final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
            if (data != null) {
                return data.getShort(memberIndex);
            }
            throw new IOException(MessageFormat.format("No data found for grid point ''{0}''.", gridPointIndex));
        }
    }

    @Override
    protected int getInt(int gridPointIndex) throws IOException {
        final long snapshotId = getSnapshotId();
        if (snapshotId == -1) {
            return smosFile.getBrowseBtDataValueInt(gridPointIndex, memberIndex, polarisation);
        } else {
            final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
            if (data != null) {
                return data.getInt(memberIndex);
            }
            throw new IOException(MessageFormat.format("No data found for grid point ''{0}''.", gridPointIndex));
        }
    }

    @Override
    protected float getFloat(int gridPointIndex) throws IOException {
        final long snapshotId = getSnapshotId();
        if (snapshotId == -1) {
            return smosFile.getBrowseBtDataValueFloat(gridPointIndex, memberIndex, polarisation);
        } else {
            final CompoundData data = smosFile.getSnapshotBtData(gridPointIndex, polarisation, snapshotId);
            if (data != null) {
                return data.getFloat(memberIndex);
            }
            throw new IOException(MessageFormat.format("No data found for grid point ''{0}''.", gridPointIndex));
        }
    }
}
