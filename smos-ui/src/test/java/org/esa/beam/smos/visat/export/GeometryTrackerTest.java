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

import junit.framework.TestCase;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GeometryTrackerTest extends TestCase {

    private GeometryTracker tracker;

    public void testGetGeometry_nothingAdded() {
        final Rectangle2D area = tracker.getArea();

        assertNotNull(area);
        assertTrue(area.isEmpty());
    }

    public void testGetGeometry_addOnePoint() {
        tracker.add(new Point2D.Double(20, 10));
        final Rectangle2D area = tracker.getArea();

        assertNotNull(area);
        assertTrue(area.isEmpty());
    }

    public void testGetGeometry_addTwoPoints() {
        tracker.add(new Point2D.Double(20, 10));
        tracker.add(new Point2D.Double(30, 5));
        final Rectangle2D area = tracker.getArea();

        assertNotNull(area);
        assertFalse(area.isEmpty());

        assertEquals(20, area.getMinX(), 1e-8);
        assertEquals(30, area.getMaxX(), 1e-8);
        assertEquals(5, area.getMinY(), 1e-8);
        assertEquals(10, area.getMaxY(), 1e-8);
    }

    public void testGetGeometry_addThreePoints() {
        tracker.add(new Point2D.Double(20, 10));
        tracker.add(new Point2D.Double(30, 5));
        tracker.add(new Point2D.Double(40, 0));
        final Rectangle2D area = tracker.getArea();

        assertNotNull(area);
        assertFalse(area.isEmpty());

        assertEquals(20, area.getMinX(), 1e-8);
        assertEquals(40, area.getMaxX(), 1e-8);
        assertEquals(0, area.getMinY(), 1e-8);
        assertEquals(10, area.getMaxY(), 1e-8);
    }

    public void testHasValidArea_nothingAdded() {
        assertFalse(tracker.hasValidArea());
    }

    public void testHasValidArea_onePointAdded() {
        tracker.add(new Point2D.Double(25, -4));
        assertFalse(tracker.hasValidArea());
    }

    public void testHasValidArea_twoPointsAdded() {
        tracker.add(new Point2D.Double(25, -4));
        tracker.add(new Point2D.Double(26, -7));
        assertTrue(tracker.hasValidArea());
    }

    public void testHasValidArea_threePointsAdded() {
        tracker.add(new Point2D.Double(25, -4));
        tracker.add(new Point2D.Double(26, -7));
        tracker.add(new Point2D.Double(27, -9));
        assertTrue(tracker.hasValidArea());
    }

    @Override
    protected void setUp() {
        tracker = new GeometryTracker();
    }
}
