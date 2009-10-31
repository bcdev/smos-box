package org.esa.beam.dataio.smos;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

import java.util.Date;

public class ExplorerFileTest {

    @Test
    public void testCfiToUtc() {
        Date utc = ExplorerFile.cfiDateToUtc(3456, 2267, 778734);
        assertEquals(1245285467778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3457, 2267, 778734);
        assertEquals(1245371867778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3456, 2268, 778734);
        assertEquals(1245285468778L, utc.getTime());

        utc = ExplorerFile.cfiDateToUtc(3456, 2267, 878734);
        assertEquals(1245285467878L, utc.getTime());
    }

}
