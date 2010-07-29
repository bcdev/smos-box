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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class GeometryTracker {
    private Rectangle2D.Double area;
    private Point2D.Double firstPoint;

    GeometryTracker() {
        area = null;
        firstPoint = null;
    }


    Rectangle2D getArea() {
        if (area == null) {
            return new Rectangle2D.Double();
        }
        return area.getBounds2D();
    }

    boolean hasValidArea() {
        return area != null && !area.isEmpty();
    }

    void add(Point2D.Double point) {
        if (area == null && firstPoint != null) {
            final double minX = Math.min(firstPoint.getX(), point.getX());
            final double maxX = Math.max(firstPoint.getX(), point.getX());
            final double minY = Math.min(firstPoint.getY(), point.getY());
            final double maxY = Math.max(firstPoint.getY(), point.getY());
            area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        } else if (firstPoint == null) {
            firstPoint = point;
        } else {
            area.add(point);
        }
    }
}
