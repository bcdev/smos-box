package org.esa.beam.smos;

import com.bc.ceres.binio.CompoundData;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DateTimeUtilsTest {

    @Test
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

    @Test
    public void testCfiDateToUtc_formCompound() throws IOException {
        final CompoundData compoundData = mock(CompoundData.class);
        when(compoundData.getInt("Days")).thenReturn(187);
        when(compoundData.getUInt("Seconds")).thenReturn(78765L);
        when(compoundData.getUInt("Microseconds")).thenReturn(1007L);

        Date date = DateTimeUtils.cfiDateToUtc(compoundData);
        assertEquals("Thu Jul 06 23:52:45 CEST 2000", date.toString());

        when(compoundData.getInt("Days")).thenReturn(0);
        when(compoundData.getUInt("Seconds")).thenReturn(0L);
        when(compoundData.getUInt("Microseconds")).thenReturn(0L);
        date = DateTimeUtils.cfiDateToUtc(compoundData);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());
    }

    @Test
    public void testMjdFloatDateToUtc() {
        Date date = DateTimeUtils.mjdFloatDateToUtc(0.0f);
        assertEquals("Sat Jan 01 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f);
        assertEquals("Sun Jan 02 01:00:00 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f + 10.f / 86400);
        long timeWithoutMillis = date.getTime();
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());

        date = DateTimeUtils.mjdFloatDateToUtc(1.0f + 10.1f / 86400);
        assertEquals("Sun Jan 02 01:00:10 CET 2000", date.toString());
        assertEquals(timeWithoutMillis + 103, date.getTime());
    }

    @Test
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

    @Test
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

    @Test
    public void testFromFixedHeaderFormat() throws ParseException {
        Date date = DateTimeUtils.fromFixedHeaderFormat("UTC=2000-01-01T00:00:00");
        assertEquals(946684800000L, date.getTime());

        date = DateTimeUtils.fromFixedHeaderFormat("UTC=2000-01-01T00:00:01");
        assertEquals(946684801000L, date.getTime());
    }

    @Test
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

    @Test
    public void testGetUtcCalendar()  {
        final Calendar calendar = DateTimeUtils.getUtcCalendar();
        assertNotNull(calendar);
        assertEquals("Coordinated Universal Time", calendar.getTimeZone().getDisplayName());
    }
}
