package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;

import static org.junit.Assert.assertTrue;
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
}
