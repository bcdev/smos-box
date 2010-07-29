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

import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class EeapTest {

    private static final double MAX_LAT = 89.0;
    private static final double CUT_LAT = 75.0;


    @Test
    public void zoneCount() {
        assertEquals(74, Eeap.getInstance().getZoneCount());
    }

    @Test
    public void zoneBounds() {
        Rectangle2D bounds;

        bounds = Eeap.getInstance().getZoneBounds(0);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(MAX_LAT, bounds.getMaxY(), 0.0);

        bounds = Eeap.getInstance().getZoneBounds(1);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(-MAX_LAT, bounds.getMinY(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = Eeap.getInstance().getZoneBounds(2);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(5.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = Eeap.getInstance().getZoneBounds(3);
        assertEquals(5.0, bounds.getMinX(), 0.0);
        assertEquals(10.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = Eeap.getInstance().getZoneBounds(Eeap.getInstance().getZoneCount() - 2);
        assertEquals(350.0, bounds.getMinX(), 0.0);
        assertEquals(355.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = Eeap.getInstance().getZoneBounds(Eeap.getInstance().getZoneCount() - 1);
        assertEquals(355.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);
    }
}
