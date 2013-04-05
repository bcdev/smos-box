package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.Operator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConverterOp_SpiTest {

    @Test
    public void testCreateOperator() {
        final ConverterOp.Spi spi = new ConverterOp.Spi();

        final Operator operator = spi.createOperator();
        assertNotNull(operator);
        assertTrue(operator instanceof ConverterOp);
    }
}
