package org.esa.beam.dataio.smos;

import junit.framework.TestCase;

public class SmosProductReaderTest extends TestCase {

    public void testIs_SMUPD_File() {
        assertFalse(SmosProductReader.is_SMUDP_File("Gnagnagna"));
        assertTrue(SmosProductReader.is_SMUDP_File("SM_TEST_MIR_SMUDP2_20121117T160642_20121117T170041_303_001_1.DBL"));
    }

    public void testIs_OSUDP_File() {
        assertFalse(SmosProductReader.is_OSUDP_File("Blablabla"));
        assertTrue(SmosProductReader.is_OSUDP_File("SM_TEST_MIR_OSUDP2_20121118T143742_20121118T153047_306_002_1.DBL"));
    }

    public void testIs_L2_User_File() {
        assertFalse(SmosProductReader.is_L2_User_File("Hibbel-Bibbel.zack"));
        assertTrue(SmosProductReader.is_L2_User_File("SM_TEST_MIR_SMUDP2_20121117T160642_20121117T170041_303_001_1.DBL"));
        assertTrue(SmosProductReader.is_L2_User_File("SM_TEST_MIR_OSUDP2_20121118T143742_20121118T153047_306_002_1.DBL"));
    }
}
