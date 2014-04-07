package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.NFileWriteable;

import java.io.IOException;

class BrowseProductExporter extends AbstractFormatExporter {

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.addDimension("bt_data_count", 255);
        nFileWriteable.addDimension("vector", 1);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {
        // @todo 1 tb/tb continue here tb 2014-04-07
        //nFileWriteable.addVariable("grid_point_id", DataType.INT, true, new Dimension(gridPointCount, 1), "grid_point_count vector");
    }
}
