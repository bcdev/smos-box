package org.esa.beam.smos.ee2netcdf.variable;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariableDescriptorTest {

    private VariableDescriptor variableDescriptor;

    @Before
    public void setUp() {
        variableDescriptor = new VariableDescriptor();
    }

    @Test
    public void testSettingFillValueTriggersPresentIndicator() {
        assertFalse(variableDescriptor.isFillValuePresent());
        assertEquals(Float.NaN, variableDescriptor.getFillValue(), 1e-8);

        variableDescriptor.setFillValue(982.f);
        assertTrue(variableDescriptor.isFillValuePresent());
        assertEquals(982.f, variableDescriptor.getFillValue(), 1e-8);
    }

    @Test
    public void testSettingValidMinTriggersPresentIndicator() {
        assertFalse(variableDescriptor.isValidMinPresent());
        assertEquals(Float.NaN, variableDescriptor.getValidMin(), 1e-8);

        variableDescriptor.setValidMin(983.f);
        assertTrue(variableDescriptor.isValidMinPresent());
        assertEquals(983.f, variableDescriptor.getValidMin(), 1e-8);
    }

    @Test
    public void testSettingValidMaxTriggersPresentIndicator() {
        assertFalse(variableDescriptor.isValidMaxPresent());
        assertEquals(Float.NaN, variableDescriptor.getValidMax(), 1e-8);

        variableDescriptor.setValidMax(984.f);
        assertTrue(variableDescriptor.isValidMaxPresent());
        assertEquals(984.f, variableDescriptor.getValidMax(), 1e-8);
    }

    @Test
    public void testSettingScaleFactorTriggersPresentIndicator() {
        assertFalse(variableDescriptor.isScaleFactorPresent());
        assertEquals(1.0, variableDescriptor.getScaleFactor(), 1e-8);

        variableDescriptor.setScaleFactor(985.0);
        assertTrue(variableDescriptor.isScaleFactorPresent());
        assertEquals(985.0, variableDescriptor.getScaleFactor(), 1e-8);
    }

    @Test
    public void testSettingScaleOffsetTriggersPresentIndicator() {
        assertFalse(variableDescriptor.isScaleOffsetPresent());
        assertEquals(0.0, variableDescriptor.getScaleOffset(), 1e-8);

        variableDescriptor.setScaleOffset(986.0);
        assertTrue(variableDescriptor.isScaleOffsetPresent());
        assertEquals(986.0, variableDescriptor.getScaleOffset(), 1e-8);
    }

    @Test
    public void testConstruction() {
        assertEquals(1.0, variableDescriptor.getScaleFactor(), 1e-8);
        assertEquals(0.0, variableDescriptor.getScaleOffset(), 1e-8);
    }
}
