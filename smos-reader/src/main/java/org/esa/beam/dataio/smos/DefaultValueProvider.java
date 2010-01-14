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

/**
 * Provides the value of a certain member in the grid point data records
 * of a SMOS file. Suitable for SMOS L2 and ECMWF data.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
class DefaultValueProvider implements ValueProvider {

    private final SmosFile smosFile;
    private final int memberIndex;

    DefaultValueProvider(SmosFile smosFile, int memberIndex) {
        this.smosFile = smosFile;
        this.memberIndex = memberIndex;
    }

    DefaultValueProvider(SmosFile smosFile, String memberName) {
        this.smosFile = smosFile;
        this.memberIndex = smosFile.getGridPointType().getMemberIndex(memberName);
    }

    public final SmosFile getSmosFile() {
        return smosFile;
    }

    public final int getMemberIndex() {
        return memberIndex;
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
    public byte getValue(int gridPointIndex, byte noDataValue) {
        try {
            return smosFile.getGridPointData(gridPointIndex).getByte(getMemberIndex());
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        try {
            return smosFile.getGridPointData(gridPointIndex).getShort(getMemberIndex());
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        try {
            return smosFile.getGridPointData(gridPointIndex).getInt(getMemberIndex());
        } catch (IOException e) {
            return noDataValue;
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        try {
            return smosFile.getGridPointData(gridPointIndex).getFloat(getMemberIndex());
        } catch (IOException e) {
            return noDataValue;
        }
    }
}
