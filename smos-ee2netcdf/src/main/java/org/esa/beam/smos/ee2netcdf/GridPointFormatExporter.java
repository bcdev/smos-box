package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class GridPointFormatExporter {

    public void write(Product product, File outputFile) throws IOException {
        // @todo 2 tb/tb check for reader type and throw exception if not appropriate tb 2014-04-05
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        final SmosFile explorerFile = (SmosFile) smosReader.getExplorerFile();
        final int gridPointCount = explorerFile.getGridPointCount();

        final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.create();

        nFileWriteable.close();
    }
}
