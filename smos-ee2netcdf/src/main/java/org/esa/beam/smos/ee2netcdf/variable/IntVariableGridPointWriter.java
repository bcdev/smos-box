package org.esa.beam.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class IntVariableGridPointWriter extends AbstractVariableWriter {

    private final int memberIndex;

    IntVariableGridPointWriter(NVariable variable, int memberIndex, int arraySize) {
        this.memberIndex = memberIndex;
        array = Array.factory(new int[arraySize]);
        this.variable = variable;
    }

    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final int gpInt = gridPointData.getInt(memberIndex);
        array.setInt(index, gpInt);
    }
}
