package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class IntVariableWriter {

    private final String compoundName;
    private final Array array;
    private final NVariable variable;

    IntVariableWriter(NVariable variable, String compoundName, int arraySize) {
        this.compoundName = compoundName;
        array = Array.factory(new int[arraySize]);
        this.variable = variable;
    }

    void write(CompoundData gridPointData, int index) throws IOException {
        final int gpInt = gridPointData.getInt(compoundName);
        array.setInt(index, gpInt);
    }

    void close() throws IOException {
        variable.writeFully(array);
    }
}
