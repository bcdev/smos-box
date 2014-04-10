package org.esa.beam.smos.ee2netcdf.variable;

import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

abstract class AbstractVariableWriter implements VariableWriter {

    protected Array array;
    protected NVariable variable;

    @Override
    public void close() throws IOException {
        variable.writeFully(array);
    }
}
