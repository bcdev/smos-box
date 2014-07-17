package org.esa.beam.smos.ee2netcdf.variable;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class FloatStructMemberWriter extends AbstractVariableWriter {

    private final int memberIndex;

    FloatStructMemberWriter(NVariable variable, int memberIndex, int arraySize) {
        this.memberIndex = memberIndex;
        array = Array.factory(new float[arraySize]);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final float gpFloat = gridPointData.getFloat(memberIndex);
        array.setFloat(index, gpFloat);
    }
}
