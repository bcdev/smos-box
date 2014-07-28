package org.esa.beam.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class IntStructMemberWriter extends AbstractVariableWriter {

    private final int memberIndex;

    IntStructMemberWriter(NVariable variable, int memberIndex, int arraySize, int fillValue) {
        this.memberIndex = memberIndex;
        final int[] intVector = VariableHelper.getIntVector(arraySize, fillValue);
        array = Array.factory(intVector);
        this.variable = variable;
    }

    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final int gpInt = gridPointData.getInt(memberIndex);
        array.setInt(index, gpInt);
    }
}
