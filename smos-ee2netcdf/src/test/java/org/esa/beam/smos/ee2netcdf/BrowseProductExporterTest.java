package org.esa.beam.smos.ee2netcdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrowseProductExporterTest {

    @Test
    public void testGetBtDataDimension() {
        assertEquals(2, BrowseProductExporter.getBtDataDimension("SM_OPER_MIR_BWLD1C_20130326T122713_20110326T132027_505_001_1.zip"));
        assertEquals(4, BrowseProductExporter.getBtDataDimension("SM_OPER_MIR_BWLF1C_20130326T122713_20110326T132027_505_001_1.zip"));
    }

    @Test
    public void testGetBtDataDimension_throwsOnInvalidFilename() {
        try {
            BrowseProductExporter.getBtDataDimension("SM_OPER_MIR_SMUDP2_20130326T190735_20130326T200046_551_001_1.zip");
        } catch (IllegalArgumentException expected) {
        }
    }
}
