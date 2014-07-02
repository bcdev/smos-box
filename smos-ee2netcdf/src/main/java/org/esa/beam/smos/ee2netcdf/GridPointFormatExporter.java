package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class GridPointFormatExporter {

    public void write(Product product, File outputFile, ExportParameter exportParameter) throws IOException {
        final FormatExporter exporter = FormatExporterFactory.create(product.getFileLocation().getName());
        exporter.initialize(product);

        final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());

        exporter.addGlobalAttributes(nFileWriteable, product.getMetadataRoot(), exportParameter);
        exporter.addDimensions(nFileWriteable);
        exporter.addVariables(nFileWriteable);

        nFileWriteable.create();

        exporter.writeData(nFileWriteable);

        nFileWriteable.close();
    }

    public static SmosFile getSmosFile(Product product) {
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        return (SmosFile) smosReader.getProductFile();
    }
}
