package org.esa.beam.smos.ee2netcdf.variable;

import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;

import java.io.IOException;

abstract class AbstractVariableWriter implements VariableWriter {

    protected Array array;
    protected NVariable variable;

    @Override
    public void close() throws IOException {
        try {
            variable.writeFully(array);
        } catch (Exception e) {
            System.out.println("variable = " + variable.getName());
            throw new IOException(e);
        }
    }
}
