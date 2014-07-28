package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.io.IOException;

class IntStructSequence2DWriter extends AbstractVariableWriter {

    private final int memberIndex;

    IntStructSequence2DWriter(NVariable variable, int width, int height, int memberIndex, int fillValue) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        final int[][] intArray = VariableHelper.getIntArray(width, height, fillValue);
        array = Array.factory(intArray);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final Index arrayIndex = array.getIndex();
        final long size = btDataList.getElementCount();
        for (int i = 0; i < size; i++) {
            final int data = btDataList.getCompound(i).getInt(memberIndex);
            arrayIndex.set(index, i);
            array.setInt(arrayIndex, data);
        }
    }
}
