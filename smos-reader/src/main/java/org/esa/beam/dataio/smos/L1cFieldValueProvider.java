/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import java.awt.geom.Area;
import java.io.IOException;

public class L1cFieldValueProvider implements GridPointValueProvider {
    private final L1cSmosFile smosFile;
    private final int fieldIndex;
    private final int polMode;
    private volatile long snapshotId;

    public L1cFieldValueProvider(L1cSmosFile smosFile, int fieldIndex, int polMode) {
        this.smosFile = smosFile;
        this.fieldIndex = fieldIndex;
        this.polMode = polMode;
        this.snapshotId = -1;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public Area getRegion() {
        if (smosFile instanceof L1cScienceSmosFile && snapshotId != -1) {
            L1cScienceSmosFile scienceSmosFile = (L1cScienceSmosFile) smosFile;
            return new Area(scienceSmosFile.getSnapshotRegion(snapshotId));
        } else {
            return smosFile.getRegion();
        }
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
            } else {
                return smosFile.getSnapshotBtData(gridPointIndex, fieldIndex, polMode, snapshotId, noDataValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
            } else {
                return smosFile.getSnapshotBtData(gridPointIndex, fieldIndex, polMode, snapshotId, noDataValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        try {
            if (snapshotId == -1) {
                return smosFile.getBrowseBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
            } else {
                return smosFile.getSnapshotBtData(gridPointIndex, fieldIndex, polMode, snapshotId, noDataValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
