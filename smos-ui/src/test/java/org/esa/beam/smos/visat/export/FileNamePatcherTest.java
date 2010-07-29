/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat.export;

import junit.framework.TestCase;

import java.util.Date;

public class FileNamePatcherTest extends TestCase {

    public void testPatchDates() {
        final FileNamePatcher patcher = new FileNamePatcher(
                "SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0");

        patcher.setStartDate(new Date(1000000));
        patcher.setStopDate(new Date(1010000));

        assertEquals("SM_TEST_MIR_SCLD1C_19700101T011640_19700101T011650_141_000_0.HDR", patcher.getHdrFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_19700101T011640_19700101T011650_141_000_0.DBL", patcher.getDblFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_19700101T011640_19700101T011650_141_000_0",
                     patcher.getFileNameWithoutExtension());
    }

    public void testPatchNoDates() {
        final FileNamePatcher patcher = new FileNamePatcher(
                "SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0");

        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.HDR", patcher.getHdrFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.DBL", patcher.getDblFileName());
        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0",
                     patcher.getFileNameWithoutExtension());
    }
}
