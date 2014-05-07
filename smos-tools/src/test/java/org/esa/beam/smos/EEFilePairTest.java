package org.esa.beam.smos;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class EEFilePairTest {

    @Test
    public void testConstructionWithFiles() {
        final File dblFile = new File("data_block.dbl");
        final File hdrFile = new File("header.hdr");

        EEFilePair filePair = new EEFilePair(hdrFile, dblFile);
        assertEquals(hdrFile.getName(), filePair.getHdrFile().getName());
        assertEquals(dblFile.getName(), filePair.getDblFile().getName());
    }
}
