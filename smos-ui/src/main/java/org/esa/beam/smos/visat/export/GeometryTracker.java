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
