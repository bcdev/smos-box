package org.esa.beam.dataio.smos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ralf Quast
 */
public class AbstractValueProviderTest {

    @Test
    public void testAngularAverage() throws Exception {
        // one angle in quadrant 1, one in quadrant 2
        assertEquals(0.0, AbstractValueProvider.angularAverage(89.0, 91.0), 90.0);
        assertEquals(0.0, AbstractValueProvider.angularAverage(91.0, 89.0), 90.0);

        // one angle in quadrant 2, one in quadrant 3
        assertEquals(0.0, AbstractValueProvider.angularAverage(179.0, 181.0), 180.0);
        assertEquals(0.0, AbstractValueProvider.angularAverage(181.0, 179.0), 180.0);

        // one angle in quadrant 3, one in quadrant 4
        assertEquals(0.0, AbstractValueProvider.angularAverage(269.0, 271.0), 270.0);
        assertEquals(0.0, AbstractValueProvider.angularAverage(271.0, 269.0), 270.0);

        // one angle in quadrant 1, one in quadrant 4
        assertEquals(0.0, AbstractValueProvider.angularAverage(1.0, 359.0), 0.0);
        assertEquals(0.0, AbstractValueProvider.angularAverage(359.0, 1.0), 0.0);
    }
}
