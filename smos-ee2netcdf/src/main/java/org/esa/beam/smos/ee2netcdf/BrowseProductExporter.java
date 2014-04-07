package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.util.DateTimeUtils;
import org.esa.beam.framework.datamodel.Product;

import java.io.IOException;
import java.util.Date;

class BrowseProductExporter implements FormatExporter{

    private int gridPointCount;

    @Override
    public void initialize(Product product) {
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        final SmosFile explorerFile = (SmosFile) smosReader.getExplorerFile();
        gridPointCount = explorerFile.getGridPointCount();
    }

    @Override
    public void addGlobalAttributes(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addGlobalAttribute("Conventions", "CF-1.6");
        nFileWriteable.addGlobalAttribute("title", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("institution", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("contact", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("creation_date", DateTimeUtils.toFixedHeaderFormat(new Date()));
        nFileWriteable.addGlobalAttribute("total_number_of_grid_points", Integer.toString(gridPointCount));
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.addDimension("bt_data_count", 255);
    }
}
