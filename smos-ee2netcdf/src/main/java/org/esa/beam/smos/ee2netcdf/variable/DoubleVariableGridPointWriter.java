package org.esa.beam.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class DoubleVariableGridPointWriter extends AbstractVariableWriter {

    private final int memberIndex;

    DoubleVariableGridPointWriter(NVariable variable, int memberIndex, int arraySize) {
        this.memberIndex = memberIndex;
        array = Array.factory(new double[arraySize]);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final double gpDouble = gridPointData.getDouble(memberIndex);
        array.setDouble(index, gpDouble);
    }
}
