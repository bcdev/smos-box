package org.esa.beam.smos.ee2netcdf.variable;


import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.DataType;

public class VariableWriterFactory {

    public static VariableWriter create(NVariable nVariable, VariableDescriptor variableDescriptor, int gridPointCount, int btDataCount) {
        final DataType dataType = variableDescriptor.getDataType();
        final int memberIndex = variableDescriptor.getBtDataMemberIndex();
        final boolean is2d = variableDescriptor.isIs2d();
        float fillValue = 0.f;

        if (variableDescriptor.isFillValuePresent()) {
            fillValue = variableDescriptor.getFillValue();
        }

        if (is2d) {
            if (dataType == DataType.FLOAT) {
                if (variableDescriptor.isGridPointData()) {
                    return new FloatStructSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex, fillValue);
                } else {
                    return new FloatStructMember2DWriter(nVariable, gridPointCount, btDataCount, memberIndex, fillValue);
                }
            } else if (dataType == DataType.INT) {
                return new IntStructSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex, (int) fillValue);
            } else if (dataType == DataType.SHORT) {
                return new ShortStructSequence2DWriter(nVariable, gridPointCount, btDataCount, memberIndex, (short) fillValue);
            } else if (dataType == DataType.BYTE) {
                throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
            }
        } else {
            if (dataType == DataType.FLOAT) {
                return new FloatStructMemberWriter(nVariable, memberIndex, gridPointCount, fillValue);
            } else if (dataType == DataType.INT) {
                return new IntStructMemberWriter(nVariable, memberIndex, gridPointCount, (int) fillValue);
            } else if (dataType == DataType.SHORT) {
                return new ShortStructMemberWriter(nVariable, memberIndex, gridPointCount, (short) fillValue);
            } else if (dataType == DataType.BYTE) {
                return new ByteStructMemberWriter(nVariable, memberIndex, gridPointCount, (byte) fillValue);
            } else if (dataType == DataType.LONG) {
                return new LongStructMemberWriter(nVariable, memberIndex, gridPointCount, (long) fillValue);
            } else if (dataType == DataType.DOUBLE) {
                return new DoubleStructMemberWriter(nVariable, memberIndex, gridPointCount, fillValue);
            }
        }
        throw new IllegalArgumentException("Unsupported data type for writer: " + dataType);
    }
}
