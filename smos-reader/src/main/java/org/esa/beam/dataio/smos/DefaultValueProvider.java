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
class DefaultValueProvider extends AbstractValueProvider {

    private final DggFile dggFile;
    private final int memberIndex;

    DefaultValueProvider(DggFile dggFile, int memberIndex) {
        this.dggFile = dggFile;
        this.memberIndex = memberIndex;
    }

    @Override
    public final Area getArea() {
        return dggFile.getArea();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return dggFile.getGridPointIndex(seqnum);
    }

    @Override
    protected byte getByte(int gridPointIndex) throws IOException {
        return dggFile.getGridPointData(gridPointIndex).getByte(memberIndex);
    }

    @Override
    protected short getShort(int gridPointIndex) throws IOException {
        return dggFile.getGridPointData(gridPointIndex).getShort(memberIndex);
    }

    @Override
    protected int getInt(int gridPointIndex) throws IOException {
        return dggFile.getGridPointData(gridPointIndex).getInt(memberIndex);
    }

    protected long getLong(int gridPointIndex) throws IOException {
        return dggFile.getGridPointData(gridPointIndex).getLong(memberIndex);
    }

    @Override
    protected float getFloat(int gridPointIndex) throws IOException {
        return dggFile.getGridPointData(gridPointIndex).getFloat(memberIndex);
    }
}
