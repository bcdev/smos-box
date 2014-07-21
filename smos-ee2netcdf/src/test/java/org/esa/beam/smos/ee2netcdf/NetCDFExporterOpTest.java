package org.esa.beam.smos.ee2netcdf;

import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NetCDFExporterOpTest {

    @Test
    public void testCreateInputFileSet_emptyList() {
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[0]);
        assertNotNull(inputFileSet);
        assertEquals(0, inputFileSet.size());
    }

    @Test
    public void testCreateInputFileSet_oneDir() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*"});
        assertNotNull(inputFileSet);
        assertEquals(4, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPEB_MIR_SMUDP2_20140413T185915_20140413T195227_551_026_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip", iterator.next().getName());
        assertEquals("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip", iterator.next().getName());
    }

    @Test
    public void testCreateInputFileSet_oneDir_wildcard() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*BWL*"});
        assertNotNull(inputFileSet);
        assertEquals(1, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
    }

    private String getResourcePath() {
        File testDir = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/");
        if (!testDir.exists()) {
            testDir = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/");
        }
        return testDir.getPath();
    }
}
