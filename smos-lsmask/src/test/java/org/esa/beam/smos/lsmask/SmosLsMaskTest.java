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
