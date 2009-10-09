package org.esa.beam.smos.visat.export;

import static junit.framework.Assert.assertEquals;
import org.jdom.Document;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

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
        final InputStream source = getClass().getResourceAsStream(SCENARIO_27_HDR_NAME);
        for (final byte target : bytes) {
            assertEquals(source.read(), target);
        }

        System.out.println(new String(bytes));
    }
}
