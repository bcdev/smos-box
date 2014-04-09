package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

public class ByteVariableGridPointWriter implements VariableWriter {

    private final String compoundName;
    private final Array array;
    private final NVariable variable;

    public ByteVariableGridPointWriter(NVariable variable, String compoundName, int arraySize) {
        this.compoundName = compoundName;
        array = Array.factory(new byte[arraySize]);
        this.variable = variable;
    }

    @Override
    public void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException {
        final byte gpByte = gridPointData.getByte(compoundName);
        array.setByte(index, gpByte);
    }

    @Override
    public void close() throws IOException {
        variable.writeFully(array);
    }
}
