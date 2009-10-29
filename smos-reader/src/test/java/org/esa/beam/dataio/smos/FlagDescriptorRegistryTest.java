package org.esa.beam.dataio.smos;

import static junit.framework.Assert.*;
import org.junit.Test;

public class FlagDescriptorRegistryTest {

    @Test
    public void getDescriptorsFor_DBL_SM_XXXX_AUX_ECMWF__0200_F1() {
        final String name = "DBL_SM_XXXX_AUX_ECMWF__0200_F1.txt";
        final FlagDescriptors descriptors = FlagDescriptorRegistry.getInstance().getDescriptors(name);
        assertEquals(21, descriptors.asList().size());

        FlagDescriptor descriptor;
        descriptor = descriptors.getDescriptor("RR_FLAG");
        assertNotNull(descriptor);

        assertEquals("RR_FLAG", descriptor.getFlagName());
        assertEquals(0x00000080, descriptor.getMask());
        assertNull(descriptor.getColor());
        assertEquals(0.5, descriptor.getTransparency(), 0.0);
        assertFalse(descriptor.getDescription().isEmpty());
    }
}
