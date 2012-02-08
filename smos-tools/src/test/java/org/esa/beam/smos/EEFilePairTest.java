package org.esa.beam.smos;

import junit.framework.TestCase;

import java.io.File;

public class EEFilePairTest extends TestCase {

    private EEFilePair eeFilePair;

    public void testSetGetHdrFile() {
        final File file_1 = new File("bla.hdr");
        final File file_2 = new File("hurra.hdr");

        eeFilePair.setHdrFile(file_1);
        assertEquals(file_1.getName(), eeFilePair.getHdrFile().getName());

        eeFilePair.setHdrFile(file_2);
        assertEquals(file_2.getName(), eeFilePair.getHdrFile().getName());
    }

    public void testSetGetDblFile() {
        final File file_1 = new File("schulze.dbl");
        final File file_2 = new File("shultze.dbl");

        eeFilePair.setDblFile(file_1);
        assertEquals(file_1.getName(), eeFilePair.getDblFile().getName());

        eeFilePair.setDblFile(file_2);
        assertEquals(file_2.getName(), eeFilePair.getDblFile().getName());
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void setUp() {
        eeFilePair = new EEFilePair();
    }
}
