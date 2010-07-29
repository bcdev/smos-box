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
