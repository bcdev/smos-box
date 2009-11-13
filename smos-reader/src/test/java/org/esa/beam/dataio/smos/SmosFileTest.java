package org.esa.beam.dataio.smos;

import junit.framework.TestCase;

import java.util.Date;

public class SmosFileTest extends TestCase {

    public void testCfiDateToUtc() {
        Date date = SmosFile.cfiDateToUtc(0, 0, 0);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());

        date = SmosFile.cfiDateToUtc(1, 0, 0);
        assertEquals("Sun Jan 02 01:00:00 CET 2000", date.toString());

        date = SmosFile.cfiDateToUtc(1, 10, 0);
        long timeWithoutMillis = date.getTime();
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());

        date = SmosFile.cfiDateToUtc(1, 10, 100000);    // last argument is microsecond, date can only handle millis ...
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());
        assertEquals(timeWithoutMillis + 100, date.getTime());
    }

    public void testMjdFloatDateToUtc() {
        Date date = SmosFile.mjdFloatDateToUtc(0.0f);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());

        date = SmosFile.mjdFloatDateToUtc(1.0f);
        assertEquals("Sun Jan 02 01:00:00 CET 2000", date.toString());

        date = SmosFile.mjdFloatDateToUtc(1.0f + 10.f/86400);
        long timeWithoutMillis = date.getTime();
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());

        date = SmosFile.mjdFloatDateToUtc(1.0f + 10.1f/86400);
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());
        assertEquals(timeWithoutMillis + 103, date.getTime());
    }
}
