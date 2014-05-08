package org.esa.beam.dataio.smos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GridPointInfoTest {

    @Test
    public void testConstructFillsWithInvalid() {
        final GridPointInfo gridPointInfo = new GridPointInfo(12, 19);

        assertEquals(-1, gridPointInfo.getGridPointIndex(14));
        assertEquals(-1, gridPointInfo.getGridPointIndex(18));
    }

    @Test
    public void testSetSeqNumAndRetrieve() {
        final int[] seqNumbers = {13, 14, 15, 16, 17, 18, 19, 20};
        final GridPointInfo gridPointInfo = new GridPointInfo(13, 20);

        gridPointInfo.setSequenceNumbers(seqNumbers);

        assertEquals(0, gridPointInfo.getGridPointIndex(13));
        assertEquals(1, gridPointInfo.getGridPointIndex(14));
        assertEquals(7, gridPointInfo.getGridPointIndex(20));
    }

    @Test
    public void testSetSeqNumAndRetrieve_outOfValidRange() {
        final int[] seqNumbers = {13, 14, 15, 16, 17, 18, 19, 20};
        final GridPointInfo gridPointInfo = new GridPointInfo(13, 20);

        gridPointInfo.setSequenceNumbers(seqNumbers);

        assertEquals(-1, gridPointInfo.getGridPointIndex(12));
        assertEquals(-1, gridPointInfo.getGridPointIndex(21));
    }
}
