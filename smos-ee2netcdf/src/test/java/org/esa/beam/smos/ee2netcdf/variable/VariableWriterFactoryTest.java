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
    public void testCreateByteVariableGridPointWriter() {
        variableDescriptor.setDataType(DataType.BYTE);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 12, 13);
        assertTrue(writer instanceof ByteVariableGridPointWriter);
    }

    @Test
    public void testCreateFloatVariableGridPointWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 13, 14);
        assertTrue(writer instanceof FloatVariableGridPointWriter);
    }

    @Test
    public void testCreateFloatVariableSequence2DWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 14, 15);
        assertTrue(writer instanceof FloatVariableSequence2DWriter);
    }

    @Test
    public void testCreateFloatVariableSequenceWriter() {
        variableDescriptor.setDataType(DataType.FLOAT);
        variableDescriptor.setIs2d(false);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 15, 16);
        assertTrue(writer instanceof FloatVariableSequenceWriter);
    }

    @Test
    public void testCreateIntVariableGridPointWriter() {
        variableDescriptor.setDataType(DataType.INT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 16, 17);
        assertTrue(writer instanceof IntVariableGridPointWriter);
    }

    @Test
    public void testCreateIntVariableSequence2DWriter() {
        variableDescriptor.setDataType(DataType.INT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 18, 19);
        assertTrue(writer instanceof IntVariableSequence2DWriter);
    }

    @Test
    public void testCreateIntVariableSequenceWriter() {
        variableDescriptor.setDataType(DataType.INT);
        variableDescriptor.setIs2d(false);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 17, 18);
        assertTrue(writer instanceof IntVariableSequenceWriter);
    }

    @Test
    public void testCreateShortVariableGridPointWriter() {
        variableDescriptor.setDataType(DataType.SHORT);
        variableDescriptor.setGridPointData(true);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 18, 19);
        assertTrue(writer instanceof ShortVariableGridPointWriter);
    }

    @Test
    public void testCreateShortVariableSequence2DWriter() {
        variableDescriptor.setDataType(DataType.SHORT);
        variableDescriptor.setIs2d(true);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 19, 20);
        assertTrue(writer instanceof ShortVariableSequence2DWriter);
    }

    @Test
    public void testCreateShortVariableSequenceWriter() {
        variableDescriptor.setDataType(DataType.SHORT);
        variableDescriptor.setIs2d(false);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 19, 20);
        assertTrue(writer instanceof ShortVariableSequenceWriter);
    }

    @Test
    public void testCreateByteVariableSequenceWriter() {
        variableDescriptor.setDataType(DataType.BYTE);
        variableDescriptor.setIs2d(false);
        variableDescriptor.setGridPointData(false);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 21, 22);
        assertTrue(writer instanceof ByteVariableSequenceWriter);
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
