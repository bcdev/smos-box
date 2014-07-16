package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class ShortVariableSequenceWriter extends AbstractVariableWriter {

    private final int memberIndex;

    ShortVariableSequenceWriter(NVariable variable, int arraySize, int memberIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        array = Array.factory(new short[arraySize]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final short data = btDataList.getCompound(index).getShort(memberIndex);
        array.setShort(index, data);
    }
}
