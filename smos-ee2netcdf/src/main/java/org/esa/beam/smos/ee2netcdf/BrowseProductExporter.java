package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;

class BrowseProductExporter extends AbstractFormatExporter {

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.addDimension("bt_data_count", 255);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {
        // @todo 1 tb/tb continue here tb 2014-04-07
        nFileWriteable.addVariable("grid_point_id", DataType.INT, true, null, "grid_point_count");
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final NVariable va = nFileWriteable.findVariable("grid_point_id");
        final Array array = Array.factory(new int[gridPointCount]);

        for (int i = 0; i < gridPointCount; i++) {
            final CompoundData gridPointData = explorerFile.getGridPointData(i);
            final int grid_point_id = gridPointData.getInt("Grid_Point_ID");
            array.setInt(i, grid_point_id);
        }

        va.writeFully(array);
    }
}
