package org.esa.beam.dataio.smos;

import junit.framework.TestCase;

public class SmosProductReaderTest extends TestCase {

    public void testIsSmUserFormat() {
        assertTrue(SmosProductReader.isSmUserFormat("SM_TEST_MIR_SMUDP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosProductReader.isSmUserFormat("SM_OPER_MIR_SCSF1C_20100315T144805_20100315T154207_330_001_1"));
    }

    public void testIsSmAnalysisFormat() {
        assertTrue(SmosProductReader.isSmAnalysisFormat("SM_TEST_MIR_SMDAP2_20121117T183648_20121117T193048_304_001_1.zip"));
        assertFalse(SmosProductReader.isSmAnalysisFormat("SM_OPER_MIR_SCSF1C_20100315T144805_20100315T154207_330_001_1"));
    }

    public void testIsOsUserFormat() {
        assertTrue(SmosProductReader.isOsUserFormat("SM_TEST_MIR_OSUDP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosProductReader.isOsUserFormat("SM_TEST_MIR_BWSF1C_20070223T112729_20070223T121644_141_000_0.zip"));
    }

    public void testIsOsAnalysisFormat() {
        assertTrue(SmosProductReader.isOsAnalysisFormat("SM_TEST_MIR_OSDAP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosProductReader.isOsAnalysisFormat("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));
    }
}
