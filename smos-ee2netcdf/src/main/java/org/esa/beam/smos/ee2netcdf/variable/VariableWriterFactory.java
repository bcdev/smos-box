package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.DataType;

public class VariableWriterFactory {

    public static VariableWriter create(NVariable nVariable, VariableDescriptor variableDescriptor, int gridPointCount, int btDataCount) {
        final DataType dataType = variableDescriptor.getDataType();
        final int memberIndex = variableDescriptor.getBtDataMemberIndex();
        final boolean is2d = variableDescriptor.isIs2d();
        if (is2d) {
            if (dataType == DataType.FLOAT) {
                if (variableDescriptor.isGridPointData()) {
                    return new FloatVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
                } else {
                    return new FloatSequenceWriter(nVariable, gridPointCount, btDataCount, memberIndex);
                }
            } else if (dataType == DataType.INT) {
                return new IntVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
            } else if (dataType == DataType.SHORT) {
                return new ShortVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
            } else if (dataType == DataType.BYTE) {
                throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
            }
        } else {
            if (dataType == DataType.FLOAT) {
                return new FloatVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.INT) {
                return new IntVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.SHORT) {
                return new ShortVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.BYTE) {
                return new ByteVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.LONG) {
                return new LongVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.DOUBLE) {
                return new DoubleVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            }
        }
        throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
    }
}
