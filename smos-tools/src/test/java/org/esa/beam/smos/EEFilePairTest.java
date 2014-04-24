package org.esa.beam.smos;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class EEFilePairTest {

    private EEFilePair eeFilePair;

    @Before
    public void setUp() {
        eeFilePair = new EEFilePair();
    }

    @Test
    public void testSetGetHdrFile() {
        final File file_1 = new File("bla.hdr");
        final File file_2 = new File("hurra.hdr");

        eeFilePair.setHdrFile(file_1);
        assertEquals(file_1.getName(), eeFilePair.getHdrFile().getName());

        eeFilePair.setHdrFile(file_2);
        assertEquals(file_2.getName(), eeFilePair.getHdrFile().getName());
    }

    @Test
    public void testSetGetDblFile() {
        final File file_1 = new File("schulze.dbl");
        final File file_2 = new File("shultze.dbl");

        eeFilePair.setDblFile(file_1);
        assertEquals(file_1.getName(), eeFilePair.getDblFile().getName());

        eeFilePair.setDblFile(file_2);
        assertEquals(file_2.getName(), eeFilePair.getDblFile().getName());
    }
}
