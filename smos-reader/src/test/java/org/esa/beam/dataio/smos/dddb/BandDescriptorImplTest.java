package org.esa.beam.dataio.smos.dddb;


import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BandDescriptorImplTest {


    @Test
    public void testConstruction_standardPropertySet() {
        final String[] tokens = new String[] {"false", "theBand", "theMember", "18", "19", "20.0", "21.1", "22.2", "23.3", "true", "24.4", "pixelExpression", "unit", "description", "codingName", "flagDescriptors"};
        final Dddb dddb = mock(Dddb.class);
        when(dddb.getFlagDescriptors(anyString())).thenReturn(new FlagDescriptors(new ArrayList<String[]>()));

        final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens, dddb);
        assertFalse(descriptor.isVisible());
        assertEquals("theBand", descriptor.getBandName());
        assertEquals("theMember", descriptor.getMemberName());
        assertEquals(18, descriptor.getPolarization());
        assertEquals(19, descriptor.getSampleModel());
        assertEquals(20.0, descriptor.getScalingOffset(), 1e-8);
        assertEquals(21.1, descriptor.getScalingFactor(), 1e-8);
        assertEquals(22.2, descriptor.getTypicalMin(), 1e-8);
        assertEquals(23.3, descriptor.getTypicalMax(), 1e-8);
        assertTrue(descriptor.isCyclic());
        assertEquals(24.4, descriptor.getFillValue(), 1e-8);
        assertEquals("pitheBandelEtheBandpression", descriptor.getValidPixelExpression());
        assertEquals("unit", descriptor.getUnit());
        assertEquals("description", descriptor.getDescription());
        assertEquals("codingName", descriptor.getFlagCodingName());
        assertNotNull(descriptor.getFlagDescriptors());
        assertTrue(descriptor.isGridPointData());
        assertNull(descriptor.getDimensionNames());
        assertEquals(-1, descriptor.getMemberIndex());
        assertEquals(-1, descriptor.getCompundIndex());
    }

    @Test
    public void testConstruction_extendedPropertySet() {
        final String[] tokens = new String[] {"false", "theBand", "theMember", "18", "19", "20.0", "21.1", "22.2", "23.3", "true", "24.4", "pixelExpression", "unit", "description", "codingName", "flagDescriptors", "false", "dimension_name", "3", "4"};
        final Dddb dddb = mock(Dddb.class);
        when(dddb.getFlagDescriptors(anyString())).thenReturn(new FlagDescriptors(new ArrayList<String[]>()));

        final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens, dddb);
        assertFalse(descriptor.isVisible());
        assertEquals("theBand", descriptor.getBandName());
        assertEquals("theMember", descriptor.getMemberName());
        assertEquals(18, descriptor.getPolarization());
        assertEquals(19, descriptor.getSampleModel());
        assertEquals(20.0, descriptor.getScalingOffset(), 1e-8);
        assertEquals(21.1, descriptor.getScalingFactor(), 1e-8);
        assertEquals(22.2, descriptor.getTypicalMin(), 1e-8);
        assertEquals(23.3, descriptor.getTypicalMax(), 1e-8);
        assertTrue(descriptor.isCyclic());
        assertEquals(24.4, descriptor.getFillValue(), 1e-8);
        assertEquals("pitheBandelEtheBandpression", descriptor.getValidPixelExpression());
        assertEquals("unit", descriptor.getUnit());
        assertEquals("description", descriptor.getDescription());
        assertEquals("codingName", descriptor.getFlagCodingName());
        assertNotNull(descriptor.getFlagDescriptors());
        assertFalse(descriptor.isGridPointData());
        assertEquals("dimension_name", descriptor.getDimensionNames());
        assertEquals(3, descriptor.getMemberIndex());
        assertEquals(4, descriptor.getCompundIndex());
    }

    @Test
    public void testConstruction_withDefaults_standardPropertySet() {
        final String[] tokens = new String[] {"*", "theBand", "*", "*", "*", "*", "*", "*", "*", "*", "*", "pixelExpression", "*", "*", "*", "flagDescriptors"};
        final Dddb dddb = mock(Dddb.class);

        final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens, dddb);
        assertTrue(descriptor.isVisible());
        assertEquals("theBand", descriptor.getBandName());
        assertEquals("theBand", descriptor.getMemberName());
        assertEquals(-1, descriptor.getPolarization());
        assertEquals(0, descriptor.getSampleModel());
        assertEquals(0.0, descriptor.getScalingOffset(), 1e-8);
        assertEquals(1.0, descriptor.getScalingFactor(), 1e-8);
        assertEquals( Double.NEGATIVE_INFINITY, descriptor.getTypicalMin(), 1e-8);
        assertEquals( Double.POSITIVE_INFINITY, descriptor.getTypicalMax(), 1e-8);
        assertFalse(descriptor.isCyclic());
        assertEquals(Double.NaN, descriptor.getFillValue(), 1e-8);
        assertEquals("", descriptor.getUnit());
        assertEquals("", descriptor.getDescription());
        assertEquals("", descriptor.getFlagCodingName());
        assertNull(descriptor.getFlagDescriptors());
        assertTrue(descriptor.isGridPointData());
        assertNull(descriptor.getDimensionNames());
        assertEquals(-1, descriptor.getMemberIndex());
        assertEquals(-1, descriptor.getCompundIndex());
    }

    @Test
    public void testConstruction_withDefaults_extendedPropertySet() {
        final String[] tokens = new String[] {"*", "theBand", "*", "*", "*", "*", "*", "*", "*", "*", "*", "pixelExpression", "*", "*", "*", "flagDescriptors", "*", "*", "*", "*"};
        final Dddb dddb = mock(Dddb.class);

        final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens, dddb);
        assertTrue(descriptor.isVisible());
        assertEquals("theBand", descriptor.getBandName());
        assertEquals("theBand", descriptor.getMemberName());
        assertEquals(-1, descriptor.getPolarization());
        assertEquals(0, descriptor.getSampleModel());
        assertEquals(0.0, descriptor.getScalingOffset(), 1e-8);
        assertEquals(1.0, descriptor.getScalingFactor(), 1e-8);
        assertEquals( Double.NEGATIVE_INFINITY, descriptor.getTypicalMin(), 1e-8);
        assertEquals( Double.POSITIVE_INFINITY, descriptor.getTypicalMax(), 1e-8);
        assertFalse(descriptor.isCyclic());
        assertEquals(Double.NaN, descriptor.getFillValue(), 1e-8);
        assertEquals("", descriptor.getUnit());
        assertEquals("", descriptor.getDescription());
        assertEquals("", descriptor.getFlagCodingName());
        assertNull(descriptor.getFlagDescriptors());
        assertTrue(descriptor.isGridPointData());
        assertNull(descriptor.getDimensionNames());
        assertEquals(-1, descriptor.getMemberIndex());
        assertEquals(-1, descriptor.getCompundIndex());
    }
}
