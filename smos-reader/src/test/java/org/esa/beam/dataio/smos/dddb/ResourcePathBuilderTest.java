package org.esa.beam.dataio.smos.dddb;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourcePathBuilderTest {

    @Test
    public void testBuildPath(){
        final ResourcePathBuilder builder = new ResourcePathBuilder();

        String path = builder.buildPath("DBL_SM_XXXX_MIR_SMDAP2_0300", "flags", ".csv");
        assertEquals("flags/MIR_/SMDAP2/DBL_SM_XXXX_MIR_SMDAP2_0300.csv", path);

        path = builder.buildPath("DBL_SM_XXXX_MIR_BWLF1C_0300", "schemas", ".binXschema.xml");
        assertEquals("schemas/MIR_/BWLF1C/DBL_SM_XXXX_MIR_BWLF1C_0300.binXschema.xml", path);
    }
}
