package org.esa.beam.dataio.smos;

import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;

class Eeap {

    private final double maxLat;
    private final double cutLat;
    private final double deltaLon;

    private final int zoneCount;

    Eeap(double maxLat, double cutLat, double deltaLon) {
        if (deltaLon <= 0.0 || !isIntegralValue(180.0 / deltaLon) || !isIntegralValue(360.0 / deltaLon)) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal longitude delta: {0}", deltaLon));
        }

        this.maxLat = maxLat;
        this.cutLat = cutLat;
        this.deltaLon = deltaLon;

        zoneCount = (int) (360.0 / deltaLon) + 2;
    }

    public int getZoneCount() {
        return zoneCount;
    }

    public Rectangle2D getZoneBounds(int zoneIndex) {
        if (zoneIndex < 0 || zoneIndex >= zoneCount) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal zone index: {0}", zoneIndex));
        }

        switch (zoneIndex) {
        case 0:
            return new Rectangle2D.Double(0.0, cutLat, 360.0, maxLat - cutLat);
        case 1:
            return new Rectangle2D.Double(0.0, -maxLat, 360.0, maxLat - cutLat);
        default:
            return new Rectangle2D.Double((zoneIndex - 2) * deltaLon, -cutLat, deltaLon, 2 * cutLat);
        }
    }

    private static boolean isIntegralValue(double value) {
        return value == Math.floor(value);
    }
}
