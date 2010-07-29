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

package org.esa.beam.dataio.smos.dddb;

import org.junit.Before;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

public class LsMaskColorsTest {

    private Family<FlagDescriptor> descriptors;

    @Before
    public void setup() {
        descriptors = Dddb.getInstance().getFlagDescriptors("DBL_SM_XXXX_AUX_LSMASK_0200_flags");
    }

    @Test
    public void coastalFlag200() {
        assertEquals(Color.ORANGE.darker(), descriptors.getMember("200_KM_COASTAL_FLAG").getColor());
    }

    @Test
    public void coastalFlag100() {
        assertEquals(Color.ORANGE, descriptors.getMember("100_KM_COASTAL_FLAG").getColor());
    }

    @Test
    public void coastalFlag40() {
        assertEquals(Color.ORANGE.brighter(), descriptors.getMember("40_KM_COASTAL_FLAG").getColor());
    }
}
