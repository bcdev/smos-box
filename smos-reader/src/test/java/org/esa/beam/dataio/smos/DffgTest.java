package org.esa.beam.dataio.smos;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;

public class DffgTest {

    private Dffg dffg;

    @Before
    public void setup() {
        final Eeap eeap = new Eeap(89.0, 75.0, 5.0);
        final Rectangle2D bounds = eeap.getZoneBounds(0);

        dffg = new Dffg(bounds);
    }

    @Test
    public void cellCount() {
        assertEquals(74, dffg.getCellCount());
    }

}
