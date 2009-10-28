package org.esa.beam.dataio.smos;

import static junit.framework.Assert.*;
import org.junit.Test;

public class BandDescriptorRegistryTest {

    @Test
    public void getDescriptorsFor_DBL_SM_XXXX_AUX_ECMWF__0200() {
        final String formatName = "DBL_SM_XXXX_AUX_ECMWF__0200";
        final BandDescriptors descriptors = BandDescriptorRegistry.getInstance().getDescriptors(formatName);
        assertEquals(38, descriptors.asList().size());

        final BandDescriptor descriptor = descriptors.getDescriptor("RR");
        assertNotNull(descriptor);
        assertEquals("RR", descriptor.getBandName());
        assertEquals("Rain_Rate", descriptor.getMemberName());
        assertTrue(descriptor.isVisible());
        assertTrue(descriptor.hasTypicalMin());
        assertTrue(descriptor.hasTypicalMax());
        assertFalse(descriptor.isCyclic());
        assertTrue(descriptor.hasFillValue());
        assertTrue(!descriptor.getValidPixelExpression().isEmpty());
        assertEquals("RR.raw != -99999.0 && RR.raw != -99998.0", descriptor.getValidPixelExpression());
        assertTrue(!descriptor.getUnit().isEmpty());
        assertTrue(!descriptor.getDescription().isEmpty());

        assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertEquals("mm h-1", descriptor.getUnit());
    }

}
