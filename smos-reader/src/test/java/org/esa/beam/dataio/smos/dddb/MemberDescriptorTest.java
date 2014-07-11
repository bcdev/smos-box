package org.esa.beam.dataio.smos.dddb;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemberDescriptorTest {

    private MemberDescriptor descriptor;

    @Before
    public void setUp() {
        descriptor = new MemberDescriptor();
    }

    @Test
    public void testSetIsGridPointData() {
        descriptor.setGridPointData(true);
        assertTrue(descriptor.isGridPointData());

        descriptor.setGridPointData(false);
        assertFalse(descriptor.isGridPointData());
    }

    @Test
    public void testSetGetDataTypeName() {
        final String name_1 = "data";
        final String name_2 = "type";

        descriptor.setDataTypeName(name_1);
        assertEquals(name_1, descriptor.getDataTypeName());

        descriptor.setDataTypeName(name_2);
        assertEquals(name_2, descriptor.getDataTypeName());
    }

    @Test
    public void testSetGetBinXName() {
        final String binXName_1 = "binary";
        final String binXName_2 = "xName";

        descriptor.setBinXName(binXName_1);
        assertEquals(binXName_1, descriptor.getBinXName());

        descriptor.setBinXName(binXName_2);
        assertEquals(binXName_2, descriptor.getBinXName());
    }

    @Test
    public void testSetGetDimensionNames() {
        final String name_1 = "dimension";
        final String name_2 = "name";

        descriptor.setDimensionNames(name_1);
        assertEquals(name_1, descriptor.getDimensionNames());

        descriptor.setDimensionNames(name_2);
        assertEquals(name_2, descriptor.getDimensionNames());
    }

    @Test
    public void testSetGetMemberIndex() {
        descriptor.setMemberIndex(3);
        assertEquals(3, descriptor.getMemberIndex());

        descriptor.setMemberIndex(-1);
        assertEquals(-1, descriptor.getMemberIndex());
    }

    @Test
    public void testSetGetFlagMasks() {
        final short[] masks = {2, 4, 6};

        descriptor.setFlagMasks(masks);
        final short[] masksRead = descriptor.getFlagMasks();
        assertArrayEquals(masks, masksRead);
    }

    @Test
    public void testSetGetFlagValues() {
        final short[] values = {3, 6, 9};

        descriptor.setFlagValues(values);
        final short[] valuesRead = descriptor.getFlagValues();
        assertArrayEquals(values, valuesRead);
    }

    @Test
    public void testSetGetFlagMeanings() {
        final String meaning_1 = "I mean";
        final String meaning_2 = "it seriously";

        descriptor.setFlagMeanings(meaning_1);
        assertEquals(meaning_1, descriptor.getFlagMeanings());

        descriptor.setFlagMeanings(meaning_2);
        assertEquals(meaning_2, descriptor.getFlagMeanings());
    }

    @Test
    public void testSetGetUnit() {
        final String unit_1 = "square heads";
        final String unit_2 = "milligrams";

        descriptor.setUnit(unit_1);
        assertEquals(unit_1, descriptor.getUnit());

        descriptor.setUnit(unit_2);
        assertEquals(unit_2, descriptor.getUnit());
    }

    @Test
    public void testSetGetFillValue() {
        final float fill_1 = 3.6f;
        final float fill_2 = -9.4f;

        descriptor.setFillValue(fill_1);
        assertEquals(fill_1, descriptor.getFillValue(), 1e-8);

        descriptor.setFillValue(fill_2);
        assertEquals(fill_2, descriptor.getFillValue(), 1e-8);
    }

    @Test
    public void testSetGetScalingFactor() {
        final float factor_1 = 0.34f;
        final float factor_2 = 1.45f;

        descriptor.setScalingFactor(factor_1);
        assertEquals(factor_1, descriptor.getScalingFactor(), 1e-8);

        descriptor.setScalingFactor(factor_2);
        assertEquals(factor_2, descriptor.getScalingFactor(), 1e-8);
    }

    @Test
    public void testSetGetScalingOffset() {
        final float offset_1 = 1.67f;
        final float offset_2 = 2.18f;

        descriptor.setScalingOffset(offset_1);
        assertEquals(offset_1, descriptor.getScalingOffset(), 1e-8);

        descriptor.setScalingOffset(offset_2);
        assertEquals(offset_2, descriptor.getScalingOffset(), 1e-8);
    }
}
