package org.esa.beam.smos.visat.export;

import junit.framework.TestCase;

import java.util.Date;

public class TimeTrackerTest extends TestCase {

    private TimeTracker timeTracker;

    public void testCreateAndGet() {
        final Date start = timeTracker.getIntervalStart();
        assertEquals(new Date(Long.MAX_VALUE), start);

        final Date stop = timeTracker.getIntervalStop();
        assertEquals(new Date(Long.MIN_VALUE), stop);
    }

    public void testTrackOneDate() {
        final Date date = new Date(300);
        timeTracker.track(date);

        assertEquals(date, timeTracker.getIntervalStart());
        assertEquals(date, timeTracker.getIntervalStop());
    }

    public void testTrackTwoDates() {
        final Date date_1 = new Date(4000);
        final Date date_2 = new Date(5000);

        timeTracker.track(date_1);
        timeTracker.track(date_2);

        assertEquals(date_1, timeTracker.getIntervalStart());
        assertEquals(date_2, timeTracker.getIntervalStop());
    }

    public void testTrackThreeDates() {
        final Date date_1 = new Date(11000);
        final Date date_2 = new Date(10000);
        final Date date_3 = new Date(9000);

        timeTracker.track(date_2);
        timeTracker.track(date_3);
        timeTracker.track(date_1);

        assertEquals(date_3, timeTracker.getIntervalStart());
        assertEquals(date_1, timeTracker.getIntervalStop());
    }

    public void testHasValidPeriod_noDates() {
        assertFalse(timeTracker.hasValidPeriod());
    }

    public void testHasValidPeriod_oneDate() {
        timeTracker.track(new Date(2000));

        assertTrue(timeTracker.hasValidPeriod());
    }

    public void testHasValidPeriod_twoDate() {
        timeTracker.track(new Date(2100));
        timeTracker.track(new Date(2000));

        assertTrue(timeTracker.hasValidPeriod());
    }

    @Override
    protected void setUp() {
        timeTracker = new TimeTracker();
    }
}
