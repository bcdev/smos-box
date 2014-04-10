package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

public class IntVariableSequenceWriter implements VariableWriter {

    private final Array array;
    private final NVariable variable;
    private final int memberIndex;

    public IntVariableSequenceWriter(NVariable variable, int arraySize, int memberIndex) {
        this.variable = variable;
        this.memberIndex = memberIndex;
        array = Array.factory(new int[arraySize]);
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final int data = btDataList.getCompound(0).getInt(memberIndex);
        array.setInt(index, data);
    }

    @Override
    public void close() throws IOException {
        variable.writeFully(array);
    }
}
