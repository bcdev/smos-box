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

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

import java.util.Date;

public class ExplorerFileTest {

    @Test
    public void testCfiToUtc() {
        Date utc = ExplorerFile.cfiDateToUtc(3456, 2267, 778734);
        assertEquals(1245285467778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3457, 2267, 778734);
        assertEquals(1245371867778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3456, 2268, 778734);
        assertEquals(1245285468778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3456, 2267, 878734);
        assertEquals(1245285467878L, utc.getTime());
    }

}
