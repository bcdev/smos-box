package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.NFileWriteable;

import java.io.IOException;

class L2FormatExporter extends AbstractFormatExporter {


    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {

    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {

    }
}
