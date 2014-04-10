package org.esa.beam.smos.ee2netcdf.variable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariableDescriptorTest {

    @Test
    public void testSettingFillValueTriggersPresentIndicator() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();

        assertFalse(variableDescriptor.isFillValuePresent());
        assertEquals(Float.NaN, variableDescriptor.getFillValue(), 1e-8);

        variableDescriptor.setFillValue(982.f);
        assertTrue(variableDescriptor.isFillValuePresent());
        assertEquals(982.f, variableDescriptor.getFillValue(), 1e-8);
    }
}
