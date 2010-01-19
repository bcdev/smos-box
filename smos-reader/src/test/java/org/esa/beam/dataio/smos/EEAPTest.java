package org.esa.beam.dataio.smos;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class EEAPTest {

    private static final double MAX_LAT = 89.0;
    private static final double CUT_LAT = 75.0;


    @Test
    public void zoneCount() {
        assertEquals(74, EEAP.getInstance().getZoneCount());
    }

    @Test
    public void zoneBounds() {
        Rectangle2D bounds;

        bounds = EEAP.getInstance().getZoneBounds(0);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(MAX_LAT, bounds.getMaxY(), 0.0);

        bounds = EEAP.getInstance().getZoneBounds(1);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(-MAX_LAT, bounds.getMinY(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = EEAP.getInstance().getZoneBounds(2);
        assertEquals(0.0, bounds.getMinX(), 0.0);
        assertEquals(5.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = EEAP.getInstance().getZoneBounds(3);
        assertEquals(5.0, bounds.getMinX(), 0.0);
        assertEquals(10.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = EEAP.getInstance().getZoneBounds(EEAP.getInstance().getZoneCount() - 2);
        assertEquals(350.0, bounds.getMinX(), 0.0);
        assertEquals(355.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);

        bounds = EEAP.getInstance().getZoneBounds(EEAP.getInstance().getZoneCount() - 1);
        assertEquals(355.0, bounds.getMinX(), 0.0);
        assertEquals(360.0, bounds.getMaxX(), 0.0);
        assertEquals(-CUT_LAT, bounds.getMinY(), 0.0);
        assertEquals(CUT_LAT, bounds.getMaxY(), 0.0);
    }
}
