package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.junit.Test;
import ucar.ma2.DataType;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class VariableWriterFactoryTest {

    @Test
    public void testCreateByteVariableGridPointWriter() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.setDataType(DataType.BYTE);
        variableDescriptor.setGridPointData(true);
        final NVariable nVariable = mock(NVariable.class);

        final VariableWriter writer = VariableWriterFactory.create(nVariable, variableDescriptor, 12, 13);
        assertTrue(writer instanceof ByteVariableGridPointWriter);
    }
}
