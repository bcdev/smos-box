package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class VariableWriterFactoryTest {

    private VariableDescriptor variableDescriptor;
    private NVariable nVariable;

    @Before
    public void setUp() {
        variableDescriptor = new VariableDescriptor();
        nVariable = mock(NVariable.class);
    }

    @Test
    public void testCreateByteStructMemberWriter() {
        variableDescriptor.setDataType(DataType.BYTE);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 12, 13);
        assertTrue(writer instanceof ByteStructMemberWriter);
    }

    @Test
    public void testCreateFloatStructMemberWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 13, 14);
        assertTrue(writer instanceof FloatStructMemberWriter);
    }

    @Test
    public void testCreateFloatStructSequence2DWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 14, 15);
        assertTrue(writer instanceof FloatStructSequence2DWriter);
    }

    @Test
    public void testCreateFloatStructMember2DWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 14, 15);
        assertTrue(writer instanceof FloatStructMember2DWriter);
    }

    @Test
    public void testCreateIntStructMemberWriter() {
        variableDescriptor.setDataType(DataType.INT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 16, 17);
        assertTrue(writer instanceof IntStructMemberWriter);
    }

    @Test
    public void testCreateIntStructSequence2DWriter() {
        variableDescriptor.setDataType(DataType.INT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 18, 19);
        assertTrue(writer instanceof IntStructSequence2DWriter);
    }

    @Test
    public void testCreateShortStructMemberWriter() {
        variableDescriptor.setDataType(DataType.SHORT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 18, 19);
        assertTrue(writer instanceof ShortStructMemberWriter);
    }

    @Test
    public void testCreateShortStructSequence2DWriter() {
        variableDescriptor.setDataType(DataType.SHORT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 19, 20);
        assertTrue(writer instanceof ShortStructSequence2DWriter);
    }

    @Test
    public void testCreateLongStructMemberWriter() {
        variableDescriptor.setDataType(DataType.LONG);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 21, 22);
        assertTrue(writer instanceof LongStructMemberWriter);
    }

    @Test
    public void testCreateDoubleStructMemberWriter() {
        variableDescriptor.setDataType(DataType.DOUBLE);
        variableDescriptor.setIs2d(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 21, 22);
        assertTrue(writer instanceof DoubleStructMemberWriter);
    }

    @Test
    public void testCreateVariableWithUnsupportedDataTypeThrowsException() {
        variableDescriptor.setDataType(DataType.BOOLEAN);
        variableDescriptor.setGridPointData(true);
        try {
            VariableWriterFactory.create(nVariable, variableDescriptor, 19, 20);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        variableDescriptor.setDataType(DataType.ENUM2);
        variableDescriptor.setGridPointData(false);
        try {
            VariableWriterFactory.create(nVariable, variableDescriptor, 19, 20);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
