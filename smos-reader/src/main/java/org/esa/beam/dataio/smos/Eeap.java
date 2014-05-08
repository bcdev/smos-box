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

class Eeap {

    private final double maxLat;
    private final double cutLat;
    private final double deltaLon;

    private final int zoneCount;

    static Eeap getInstance() {
        return Holder.INSTANCE;
    }

    int getZoneCount() {
        return zoneCount;
    }

    int getZoneIndex(double lon, double lat) {

        if (lat <= maxLat && lat > cutLat) {
            return 0;
        }
        if (lat <= -cutLat && lat > -maxLat) {
            return 1;
        }
        if (lat <= cutLat && lat > -cutLat) {
            if (lon < 0.0) {
                lon += 360.0;
            }
            return (int) Math.floor(lon / deltaLon) + 2;
        }

        return -1;
    }

    private Eeap() {
        this.maxLat = 89.0;
        this.cutLat = 75.0;
        this.deltaLon = 5.0;

        zoneCount = 2 + 2 * (int) (180.0 / deltaLon);
    }

    private static class Holder {

        private static final Eeap INSTANCE = new Eeap();
    }
}
