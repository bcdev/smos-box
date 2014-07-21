package org.esa.beam.dataio.smos;

import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class DggUtilsTest {

    @Test
    public void testCreateTileRectangle() {
        Rectangle2D rectangle = DggUtils.createTileRectangle(0, 0);
        assertEquals(-180.0, rectangle.getX(), 1e-8);
        assertEquals(78.75, rectangle.getY(), 1e-8);
        assertEquals(11.25, rectangle.getWidth(), 1e-8);
        assertEquals(11.25, rectangle.getHeight(), 1e-8);

        rectangle = DggUtils.createTileRectangle(31, 0);
        assertEquals(168.75, rectangle.getX(), 1e-8);
        assertEquals(78.75, rectangle.getY(), 1e-8);
        assertEquals(11.25, rectangle.getWidth(), 1e-8);
        assertEquals(11.25, rectangle.getHeight(), 1e-8);

        rectangle = DggUtils.createTileRectangle(31, 15);
        assertEquals(168.75, rectangle.getX(), 1e-8);
        assertEquals(-90.0, rectangle.getY(), 1e-8);
        assertEquals(11.25, rectangle.getWidth(), 1e-8);
        assertEquals(11.25, rectangle.getHeight(), 1e-8);
    }
}
