package org.esa.beam.dataio.smos.util;

import junit.framework.TestCase;
import org.esa.beam.dataio.smos.SmosFile;

import java.util.Date;

public class DateTimeUtilsTest extends TestCase {

    public void testCfiDateToUtc() {
        Date date = DateTimeUtils.cfiDateToUtc(0, 0, 0);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.cfiDateToUtc(1, 0, 0);
        assertEquals("Sun Jan 02 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.cfiDateToUtc(1, 10, 0);
        long timeWithoutMillis = date.getTime();
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());

        date = DateTimeUtils.cfiDateToUtc(1, 10, 100000);    // last argument is microsecond, date can only handle millis ...
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());
        assertEquals(timeWithoutMillis + 100, date.getTime());
    }

     public void testMjdFloatDateToUtc() {
        Date date = DateTimeUtils.mjdFloatDateToUtc(0.0f);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f);
        assertEquals("Sun Jan 02 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f + 10.f/86400);
        long timeWithoutMillis = date.getTime();
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f + 10.1f/86400);
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());
        assertEquals(timeWithoutMillis + 103, date.getTime());
    }

    public void testToVariableHeaderFormat() {
        Date date = DateTimeUtils.cfiDateToUtc(0, 0, 0);
        String variableHeaderFormat = DateTimeUtils.toVariableHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:00.000000", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(1, 0, 0);
        variableHeaderFormat = DateTimeUtils.toVariableHeaderFormat(date);
        assertEquals("UTC=2000-01-02T00:00:00.000000", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 1, 0);
        variableHeaderFormat = DateTimeUtils.toVariableHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:01.000000", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 0, 1000);
        variableHeaderFormat = DateTimeUtils.toVariableHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:00.000001", variableHeaderFormat);
    }

    public void testToFixedHeaderFormat() {
        Date date = DateTimeUtils.cfiDateToUtc(0, 0, 0);
        String variableHeaderFormat = DateTimeUtils.toFixedHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:00", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(1, 0, 0);
        variableHeaderFormat = DateTimeUtils.toFixedHeaderFormat(date);
        assertEquals("UTC=2000-01-02T00:00:00", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 1, 0);
        variableHeaderFormat = DateTimeUtils.toFixedHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:01", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 0, 1000);
        variableHeaderFormat = DateTimeUtils.toFixedHeaderFormat(date);
        assertEquals("UTC=2000-01-01T00:00:00", variableHeaderFormat);
    }

    public void testToFileNameFormat() {
        Date date = DateTimeUtils.cfiDateToUtc(0, 0, 0);
        String variableHeaderFormat = DateTimeUtils.toFileNameFormat(date);
        assertEquals("20000101T000000", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(1, 0, 0);
        variableHeaderFormat = DateTimeUtils.toFileNameFormat(date);
        assertEquals("20000102T000000", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 1, 0);
        variableHeaderFormat = DateTimeUtils.toFileNameFormat(date);
        assertEquals("20000101T000001", variableHeaderFormat);

        date = DateTimeUtils.cfiDateToUtc(0, 0, 1000);
        variableHeaderFormat = DateTimeUtils.toFileNameFormat(date);
        assertEquals("20000101T000000", variableHeaderFormat);
    }
}
