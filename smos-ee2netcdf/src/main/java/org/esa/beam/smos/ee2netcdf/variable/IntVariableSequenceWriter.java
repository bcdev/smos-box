package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class IntVariableSequenceWriter extends AbstractVariableWriter {

    private final int memberIndex;

    IntVariableSequenceWriter(NVariable variable, int arraySize, int memberIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        array = Array.factory(new int[arraySize]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final int data = btDataList.getCompound(index).getInt(memberIndex);
        array.setInt(index, data);
    }
}
