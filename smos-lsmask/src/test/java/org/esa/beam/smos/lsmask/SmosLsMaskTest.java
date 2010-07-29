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

package org.esa.beam.smos.lsmask;

import com.bc.ceres.glevel.MultiLevelImage;
import org.junit.Test;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

import static org.junit.Assert.assertEquals;

public class SmosLsMaskTest {

    private final MultiLevelImage multiLevelImage = SmosLsMask.getInstance().getMultiLevelImage();

    @Test
    public void imageProperties() {
        assertEquals(16384, multiLevelImage.getWidth());
        assertEquals(8192, multiLevelImage.getHeight());
        assertEquals(7, multiLevelImage.getModel().getLevelCount());
        assertEquals(DataBuffer.TYPE_BYTE, multiLevelImage.getSampleModel().getDataType());
    }

    @Test
    public void imageSamples() {
        final Raster data = multiLevelImage.getData(new Rectangle(0, 0, 512, 512));
        assertEquals(193, data.getSample(0, 0, 0), 0.0);
    }
}
