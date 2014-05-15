package org.esa.beam.dataio.smos;

import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class L1cScienceSmosFileTest {

    @Test
    public void testCreateGridPointRectangle() {
        final Rectangle2D rectangle = L1cScienceSmosFile.createGridPointRectangle(34, -10);
        assertEquals(33.98, rectangle.getX(), 1e-8);
        assertEquals(-10.01, rectangle.getY(), 1e-8);
        assertEquals(0.04, rectangle.getWidth(), 1e-8);
        assertEquals(0.02, rectangle.getHeight(), 1e-8);
    }

    @Test
    public void testCreateGridPointRectangle_lonCrossesAntiMeridian() {
        Rectangle2D rectangle = L1cScienceSmosFile.createGridPointRectangle(-181, 19);
        assertEquals(-180.0, rectangle.getX(), 1e-8);
        assertEquals(18.99, rectangle.getY(), 1e-8);
        assertEquals(0.04, rectangle.getWidth(), 1e-8);
        assertEquals(0.02, rectangle.getHeight(), 1e-8);

        rectangle = L1cScienceSmosFile.createGridPointRectangle(179.99, 20);
        assertEquals(179.93, rectangle.getX(), 1e-8);
        assertEquals(19.99, rectangle.getY(), 1e-8);
        assertEquals(0.04, rectangle.getWidth(), 1e-8);
        assertEquals(0.02, rectangle.getHeight(), 1e-8);
    }

    @Test
    public void testCreateGridPointRectangle_latBeyondPole() {
         Rectangle2D rectangle = L1cScienceSmosFile.createGridPointRectangle(35, -90.2);
        assertEquals(34.98, rectangle.getX(), 1e-8);
        assertEquals(-90.0, rectangle.getY(), 1e-8);
        assertEquals(0.04, rectangle.getWidth(), 1e-8);
        assertEquals(0.02, rectangle.getHeight(), 1e-8);

        rectangle = L1cScienceSmosFile.createGridPointRectangle(36, 90.0);
        assertEquals(35.98, rectangle.getX(), 1e-8);
        assertEquals(89.97, rectangle.getY(), 1e-8);
        assertEquals(0.04, rectangle.getWidth(), 1e-8);
        assertEquals(0.02, rectangle.getHeight(), 1e-8);
    }
}
