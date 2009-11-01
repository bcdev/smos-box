package org.esa.beam.dataio.smos;

import static junit.framework.Assert.*;
import org.junit.Test;

public class DDDBTest {

    private static final String IDENTIFIER = "DBL_SM_XXXX_AUX_ECMWF__0200";

    @Test
    public void getBandDescriptors() {
        final Family<BandDescriptor> descriptors = DDDB.getInstance().getBandDescriptors(IDENTIFIER);
        assertEquals(38, descriptors.asList().size());

        final BandDescriptor descriptor = descriptors.getMember("RR");
        assertNotNull(descriptor);

        assertEquals("RR", descriptor.getBandName());
        assertEquals("Rain_Rate", descriptor.getMemberName());
        assertTrue(descriptor.hasTypicalMin());
        assertTrue(descriptor.hasTypicalMax());
        assertFalse(descriptor.isCyclic());
        assertTrue(descriptor.hasFillValue());
        assertFalse(descriptor.getValidPixelExpression().isEmpty());
        assertEquals("RR.raw != -99999.0 && RR.raw != -99998.0", descriptor.getValidPixelExpression());
        assertFalse(descriptor.getUnit().isEmpty());
        assertFalse(descriptor.getDescription().isEmpty());
        assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertEquals("m 3h-1", descriptor.getUnit());
    }

    @Test
    public void getFlagDescriptors() {
        final Family<FlagDescriptorI> descriptors = DDDB.getInstance().getFlagDescriptors(IDENTIFIER + "_F1");
        assertEquals(21, descriptors.asList().size());

        FlagDescriptorI descriptor;
        descriptor = descriptors.getMember("RR_FLAG");
        assertNotNull(descriptor);

        assertEquals("RR_FLAG", descriptor.getFlagName());
        assertEquals(0x00000080, descriptor.getMask());
        assertNull(descriptor.getColor());
        assertEquals(0.5, descriptor.getTransparency(), 0.0);
        assertFalse(descriptor.getDescription().isEmpty());
    }

    @Test
    public void getFlagDescriptorsFromBandDescriptor() {
        final DDDB dddb = DDDB.getInstance();
        final Family<BandDescriptor> bandDescriptor = dddb.getBandDescriptors(IDENTIFIER);
        final Family<FlagDescriptorI> flagDescriptors = bandDescriptor.getMember("F1").getFlagDescriptors();
        assertNotNull(flagDescriptors);
        
        assertNotNull(dddb.getFlagDescriptors(IDENTIFIER + "_F1"));
        assertSame(flagDescriptors, dddb.getFlagDescriptors(IDENTIFIER + "_F1"));
    }
}
