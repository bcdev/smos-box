package org.esa.beam.smos.ee2netcdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrowseFormatExporterTest {

    @Test
    public void testGetBtDataDimension() {
        assertEquals(2, BrowseFormatExporter.getBtDataDimension("SM_OPER_MIR_BWLD1C_20130326T122713_20110326T132027_505_001_1.zip"));
        assertEquals(4, BrowseFormatExporter.getBtDataDimension("SM_OPER_MIR_BWLF1C_20130326T122713_20110326T132027_505_001_1.zip"));

        assertEquals(2, BrowseFormatExporter.getBtDataDimension("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.zip"));
        assertEquals(4, BrowseFormatExporter.getBtDataDimension("SM_TEST_MIR_BWSF1C_20100802T153857_20100802T163215_620_001_1.HDR"));
    }

    @Test
    public void testGetBtDataDimension_throwsOnInvalidFilename() {
        try {
            BrowseFormatExporter.getBtDataDimension("SM_OPER_MIR_SMUDP2_20130326T190735_20130326T200046_551_001_1.zip");
        } catch (IllegalArgumentException expected) {
        }
    }
}
