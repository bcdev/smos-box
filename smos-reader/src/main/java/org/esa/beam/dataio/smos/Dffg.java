package org.esa.beam.dataio.smos;

import java.awt.geom.Rectangle2D;

class Dffg {

    private final double maxLon;
    private final double minLon;
    private final double maxLat;
    private final double minLat;

    public Dffg(Rectangle2D bounds) {
        maxLon = bounds.getMaxX();
        minLon = bounds.getMinX();
        maxLat = bounds.getMaxY();
        minLat = bounds.getMinY();
    }

    public int getCellCount() {
        return 0;
    }
}
