package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

class FloatVariableGridPointWriter implements VariableWriter {

    private final String compoundName;
    private final Array array;
    private final NVariable variable;

    FloatVariableGridPointWriter(NVariable variable, String compoundName, int arraySize) {
        this.compoundName = compoundName;
        array = Array.factory(new float[arraySize]);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final float gpFloat = gridPointData.getFloat(compoundName);
        array.setFloat(index, gpFloat);

    }

    @Override
    public void close() throws IOException {
        variable.writeFully(array);
    }
}
