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

import org.jdom.Document;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;

public class EEHdrFilePatcherTest {

    private static final String SCENARIO_27_HDR_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.HDR";

    @Test
    public void copyScenario27Hdr() throws URISyntaxException, IOException {
        final EEHdrFilePatcher patcher = new EEHdrFilePatcher() {
            @Override
            Document patchDocument(Document document) {
                return document;
            }
        };

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        patcher.patch(getClass().getResourceAsStream(SCENARIO_27_HDR_NAME), os);

        final byte[] bytes = os.toByteArray();
        InputStream source = null;
        try {
            source = getClass().getResourceAsStream(SCENARIO_27_HDR_NAME);
            for (final byte target : bytes) {
                assertEquals(source.read(), target);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }

        System.out.println(new String(bytes));
    }
}
