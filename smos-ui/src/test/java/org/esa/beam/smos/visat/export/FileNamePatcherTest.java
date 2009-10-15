package org.esa.beam.smos.visat.export;

import junit.framework.TestCase;

import java.util.Date;

public class FileNamePatcherTest extends TestCase {

    public void testPatchDates() {
        final FileNamePatcher patcher = new FileNamePatcher("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0");

        patcher.setStartDate(new Date(1000000));
        patcher.setStopDate(new Date(1010000));

        assertEquals("SM_TEST_MIR_SCLD1C_19700101T011640_19700101T011650_141_000_0.HDR", patcher.getHdrFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_19700101T011640_19700101T011650_141_000_0.DBL", patcher.getDblFileName());
    }

    public void testPatchNoDates() {
        final FileNamePatcher patcher = new FileNamePatcher("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0");

        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.HDR", patcher.getHdrFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.DBL", patcher.getDblFileName());
    }
}
