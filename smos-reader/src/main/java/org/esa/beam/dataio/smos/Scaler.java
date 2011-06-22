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

import org.esa.beam.framework.datamodel.Scaling;

import java.awt.geom.Area;
import java.io.IOException;

class Scaler extends AbstractValueProvider {

    private final AbstractValueProvider provider;
    private final Scaling scaling;

    Scaler(AbstractValueProvider provider, Scaling scaling) {
        this.provider = provider;
        this.scaling = scaling;
    }

    @Override
    public Area getArea() {
        return provider.getArea();
    }

    @Override
    public int getGridPointIndex(int seqnum) {
        return provider.getGridPointIndex(seqnum);
    }

    @Override
    protected final byte getByte(int gridPointIndex) throws IOException {
        return provider.getByte(gridPointIndex);
    }

    @Override
    protected final short getShort(int gridPointIndex) throws IOException {
        return provider.getShort(gridPointIndex);
    }

    @Override
    protected final int getInt(int gridPointIndex) throws IOException {
        return provider.getInt(gridPointIndex);
    }

    @Override
    protected final float getFloat(int gridPointIndex) throws IOException {
        return (float) scaling.scale(provider.getFloat(gridPointIndex));
    }
}
