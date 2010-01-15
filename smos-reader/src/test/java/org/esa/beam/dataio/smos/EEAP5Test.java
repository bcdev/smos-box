package org.esa.beam.dataio.smos;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class EEAP5Test {

    private EEAP5 eeap5;

    @Before
    public void setup() {
        eeap5 = new EEAP5();
    }

    @Test
    public void initialState() {
        assertNotNull(eeap5);
    }
}
