package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class LongVariableGridPointWriter extends AbstractVariableWriter {

    private final int memberIndex;

    LongVariableGridPointWriter(NVariable variable, int memberIndex, int arraySize) {
        this.memberIndex = memberIndex;
        array = Array.factory(new long[arraySize]);
        this.variable = variable;
    }

    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final long gpLong = gridPointData.getLong(memberIndex);
        array.setLong(index, gpLong);
    }
}
