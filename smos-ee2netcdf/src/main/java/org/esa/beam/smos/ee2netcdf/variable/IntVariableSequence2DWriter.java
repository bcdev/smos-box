package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.io.IOException;

public class IntVariableSequence2DWriter extends AbstractVariableWriter {

    private final int memberIndex;

    IntVariableSequence2DWriter(NVariable variable, int width, int height, int memberIndex, int compoundIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        array = Array.factory(new int[width][height]);
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
