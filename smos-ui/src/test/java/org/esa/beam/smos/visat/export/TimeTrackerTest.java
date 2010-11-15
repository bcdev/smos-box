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

    public void testTrackNullDate() {
        try {
            timeTracker.track(null);            
        } catch (Exception e) {
            fail("no exception expected");
        }

        assertFalse(timeTracker.hasValidPeriod());
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
