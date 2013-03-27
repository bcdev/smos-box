package org.esa.beam.smos.ee2netcdf.visat;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExportParameterTest {

    @Test
    public void testSetIsUseSelectedProduct() {
        final ExportParameter parameter = new ExportParameter();

        parameter.setUseSelectedProduct(true);
        assertTrue(parameter.isUseSelectedProduct());

        parameter.setUseSelectedProduct(false);
        assertFalse(parameter.isUseSelectedProduct());
    }
}

