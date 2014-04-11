package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.io.IOException;

class FloatVariableSequence2DWriter extends AbstractVariableWriter {

    private final int memberIndex;
    private int height;

    public FloatVariableSequence2DWriter(NVariable variable, int width, int height, int memberIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        this.height = height;
        array = Array.factory(new float[width][height]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final Index arrayIndex = array.getIndex();
        for (int i = 0; i < height; i++) {
            final float data = btDataList.getCompound(i).getFloat(memberIndex);
            arrayIndex.set(index, i);
            array.setFloat(arrayIndex, data);
        }
    }
}
