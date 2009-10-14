package org.esa.beam.dataio.smos;

import junit.framework.TestCase;

import java.util.*;

public class SmosFileTest extends TestCase {

    public void testCfiToUtc() {
        Date utc = SmosFile.getCfiDateInUtc(3456, 2267, 778734);
        assertEquals(1245278267778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3457, 2267, 778734);
        assertEquals(1245364667778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3456, 2268, 778734);
        assertEquals(1245278268778L, utc.getTime());

        utc = SmosFile.getCfiDateInUtc(3456, 2267, 878734);
        assertEquals(1245278267878L, utc.getTime());

    }
}
