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
}
