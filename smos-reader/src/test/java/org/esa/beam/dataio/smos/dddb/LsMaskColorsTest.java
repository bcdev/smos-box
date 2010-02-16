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
