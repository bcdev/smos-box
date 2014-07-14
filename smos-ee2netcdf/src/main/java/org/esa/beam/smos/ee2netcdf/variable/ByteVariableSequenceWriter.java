package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class ByteVariableSequenceWriter extends AbstractVariableWriter {

    private final int memberIndex;
    private final int compoundIndex;

    ByteVariableSequenceWriter(NVariable variable, int arraySize, int memberIndex, int compoundIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        this.compoundIndex = compoundIndex;
        array = Array.factory(new byte[arraySize]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final byte data = btDataList.getCompound(compoundIndex).getByte(memberIndex);
        array.setByte(index, data);
    }
}
