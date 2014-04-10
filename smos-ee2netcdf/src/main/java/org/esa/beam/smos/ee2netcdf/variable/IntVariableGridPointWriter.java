package org.esa.beam.smos.ee2netcdf.variable;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

public class IntVariableGridPointWriter extends AbstractVariableWriter {

    private final String compoundName;

    public IntVariableGridPointWriter(NVariable variable, String compoundName, int arraySize) {
        this.compoundName = compoundName;
        array = Array.factory(new int[arraySize]);
        this.variable = variable;
    }

    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final int gpInt = gridPointData.getInt(compoundName);
        array.setInt(index, gpInt);
    }
}
