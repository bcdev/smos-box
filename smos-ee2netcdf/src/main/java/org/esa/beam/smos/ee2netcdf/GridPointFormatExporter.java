package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class GridPointFormatExporter {

    public void write(Product product, File outputFile) throws IOException {
        final FormatExporter exporter = FormatExporterFactory.create(product.getFileLocation().getName());
        exporter.initialize(product);

        final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());

        exporter.addGlobalAttributes(nFileWriteable);
        exporter.addDimensions(nFileWriteable);

        nFileWriteable.create();

        nFileWriteable.close();
    }
}
