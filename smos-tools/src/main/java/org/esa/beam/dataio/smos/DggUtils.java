package org.esa.beam.dataio.smos;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class DggUtils {

    public static Area computeArea(PointList pointList) throws IOException {
        final Rectangle2D[] tileRectangles = new Rectangle2D[512];
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 16; ++j) {
                tileRectangles[i * 16 + j] = createTileRectangle(i, j);
            }
        }

        final Area envelope = new Area();
        for (int i = 0; i < pointList.getElementCount(); i++) {
            double lon = pointList.getLon(i);
            double lat = pointList.getLat(i);

            // normalisation to [-180, 180] necessary for some L1c test products
            if (lon > 180.0) {
                lon = lon - 360.0;
            }
            final double hw = 0.02;
            final double hh = 0.02;

            final double x = lon - hw;
            final double y = lat - hh;
            final double w = 0.04;
            final double h = 0.04;

            if (!envelope.contains(x, y, w, h)) {
                for (final Rectangle2D tileRectangle : tileRectangles) {
                    if (tileRectangle.intersects(x, y, w, h) && !envelope.contains(tileRectangle)) {
                        envelope.add(new Area(tileRectangle));
                        if (envelope.contains(x, y, w, h)) {
                            break;
                        }
                    }
                }
            }
        }

        return envelope;
    }

    // package access for testing only tb 2014-07-21
    static Rectangle2D createTileRectangle(int i, int j) {
        final double w = 11.25;
        final double h = 11.25;
        final double x = w * i - 180.0;
        final double y = 90.0 - h * (j + 1);

        return new Rectangle2D.Double(x, y, w, w);
    }
}
