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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EeapTest {

    private Eeap eeap;

    @Before
    public void setUp() {
        eeap = Eeap.getInstance();
    }

    @Test
    public void testGetZoneCount() {
        assertEquals(74, eeap.getZoneCount());
    }

    @Test
    public void testGetZoneIndex() {
        assertEquals(0, eeap.getZoneIndex(0.0, 75.1));
        assertEquals(0, eeap.getZoneIndex(0.0, 89.0));

        assertEquals(1, eeap.getZoneIndex(0.0, -75.1));
        assertEquals(1, eeap.getZoneIndex(0.0, -88.9));

        assertEquals(2, eeap.getZoneIndex(0.0, 0.0));
        assertEquals(12, eeap.getZoneIndex(50.0, 0.0));
        assertEquals(64, eeap.getZoneIndex(-50.0, 0.0));

        assertEquals(-1, eeap.getZoneIndex(0.0, -89.4));
        assertEquals(-1, eeap.getZoneIndex(0.0, 89.2));
    }
}
