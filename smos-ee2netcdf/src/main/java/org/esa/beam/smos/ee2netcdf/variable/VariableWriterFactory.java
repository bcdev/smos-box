package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.DataType;

public class VariableWriterFactory {

    public static VariableWriter create(NVariable nVariable,
                                        VariableDescriptor variableDescriptor, int gridPointCount, int btDataCount) {
        final DataType dataType = variableDescriptor.getDataType();
        final int memberIndex = variableDescriptor.getBtDataMemberIndex();
        if (variableDescriptor.isGridPointData()) {
            if (dataType == DataType.FLOAT) {
                return new FloatVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.INT) {
                return new IntVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.SHORT) {
                return new ShortVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            } else if (dataType == DataType.BYTE) {
                return new ByteVariableGridPointWriter(nVariable, memberIndex, gridPointCount);
            }
        } else {
            final boolean is2d = variableDescriptor.isIs2d();
            if (dataType == DataType.FLOAT) {
                if (is2d) {
                    return new FloatVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
                } else {
                    return new FloatVariableSequenceWriter(nVariable, gridPointCount, memberIndex);
                }
            } else if (dataType == DataType.INT) {
                if (is2d) {
                    return new IntVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
                } else {
                    return new IntVariableSequenceWriter(nVariable, gridPointCount, memberIndex);
                }
            } else if (dataType == DataType.SHORT) {
                if (is2d) {
                    return new ShortVariableSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex);
                } else {
                    return new ShortVariableSequenceWriter(nVariable, gridPointCount, memberIndex);
                }
            } else if (dataType == DataType.BYTE) {
                if (is2d) {
                    throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
                } else {
                    return new ByteVariableSequenceWriter(nVariable, gridPointCount, memberIndex);
                }
            }
        }
        throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
    }
}
