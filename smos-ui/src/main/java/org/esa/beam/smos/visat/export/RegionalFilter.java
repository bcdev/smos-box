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

package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.smos.SmosConstants;

import java.awt.Shape;
import java.io.IOException;

class RegionalFilter implements GridPointFilter {

    private final Shape region;

    RegionalFilter(Shape region) {
        this.region = region;
    }

    @Override
    public boolean accept(int id, CompoundData gridPointData) throws IOException {
        final double lat = gridPointData.getDouble(SmosConstants.GRID_POINT_LAT_NAME);
        final double lon = gridPointData.getDouble(SmosConstants.GRID_POINT_LON_NAME);

        return region.contains(lon, lat);
    }
}
