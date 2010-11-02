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
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;

class RegionFilter implements GridPointFilter {

    private final GridPointFilter filter;

    RegionFilter(Shape region) {
        if (region instanceof RectangularShape) {
            final RectangularShape rectangularShape = (RectangularShape) region;
            if (rectangularShape.isEmpty()) {
                filter = createPointFilter(new Point2D.Double(rectangularShape.getX(), rectangularShape.getY()));
            } else {
                filter = createAreaFilter(region);
            }
        } else {
            filter = createAreaFilter(region);
        }
    }

    @Override
    public boolean accept(int id, CompoundData gridPointData) throws IOException {
        return filter.accept(id, gridPointData);
    }

    private GridPointFilter createPointFilter(Point2D point) {
        try {
            SmosDgg.getInstance().getImageToMapTransform().inverseTransform(point, point);
        } catch (NoninvertibleTransformException e) {
            // ignore, cannot happen
        }
        final int x = (int) point.getX();
        final int y = (int) point.getY();
        final MultiLevelImage mli = SmosDgg.getInstance().getMultiLevelImage();
        final int seqnum = mli.getData(new Rectangle(x, y, 1, 1)).getSample(x, y, 0);

        return new GridPointFilter() {
            @Override
            public boolean accept(int id, CompoundData gridPointData) throws IOException {
                return SmosDgg.gridPointIdToSeqnum(id) == seqnum;
            }
        };
    }

    private static GridPointFilter createAreaFilter(final Shape area) {
        return new GridPointFilter() {
            @Override
            public boolean accept(int id, CompoundData gridPointData) throws IOException {
                double lat = gridPointData.getDouble(SmosConstants.GRID_POINT_LAT_NAME);
                double lon = gridPointData.getDouble(SmosConstants.GRID_POINT_LON_NAME);
                if (lon > 180.0) {
                    lon = lon - 360.0;
                }
                return area.contains(lon, lat);
            }
        };
    }
}
