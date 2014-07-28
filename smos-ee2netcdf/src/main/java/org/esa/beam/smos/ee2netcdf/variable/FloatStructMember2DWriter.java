package org.esa.beam.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.io.IOException;

class FloatStructMember2DWriter extends AbstractVariableWriter {

    private final int memberIndex;

    FloatStructMember2DWriter(NVariable variable, int width, int height, int memberIndex, float fillValue) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        final float[][] floatArray = VariableHelper.getFloatArray(width, height, fillValue);
        array = Array.factory(floatArray);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final Index arrayIndex = array.getIndex();
        final SequenceData sequence = gridPointData.getSequence(memberIndex);
        final long size = sequence.getElementCount();
        for (int i = 0; i < size; i++) {
            final float data = sequence.getFloat(i);
            arrayIndex.set(index, i);
            array.setFloat(arrayIndex, data);
        }
    }
}
