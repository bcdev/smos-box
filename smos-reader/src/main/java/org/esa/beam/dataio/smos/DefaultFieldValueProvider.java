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

import java.io.IOException;
import java.awt.geom.Area;

/**
 * Provides the value of a certain field in the grid point data records
 * of a SMOS product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class DefaultFieldValueProvider implements FieldValueProvider {

    private final SmosDggFile smosFile;
    private final int fieldIndex;

    DefaultFieldValueProvider(SmosDggFile smosFile, int fieldIndex) {
        this.smosFile = smosFile;
        this.fieldIndex = fieldIndex;
    }

    public final SmosDggFile getSmosFile() {
        return smosFile;
    }

    public final int getFieldIndex() {
        return fieldIndex;
    }

    @Override
    public final Area getRegion() {
        return smosFile.getRegion();
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return smosFile.getGridPointIndex(seqnum);
    }
    
    @Override
    public byte getValue(int gridPointIndex, byte noDataValue) {
        try {
            return getSmosFile().getGridPointData(gridPointIndex).getByte(getFieldIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short getValue(int gridPointIndex, short noDataValue) {
        try {
            return getSmosFile().getGridPointData(gridPointIndex).getShort(getFieldIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getValue(int gridPointIndex, int noDataValue) {
        try {
            return getSmosFile().getGridPointData(gridPointIndex).getInt(getFieldIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getValue(int gridPointIndex, float noDataValue) {
        try {
            return getSmosFile().getGridPointData(gridPointIndex).getFloat(getFieldIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
