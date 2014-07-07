package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

class GpToNetCDFExporter {

    private final ExportParameter parameter;

    GpToNetCDFExporter(ExportParameter parameter) {
        this.parameter = parameter;
    }

    void initialize() {
        ExporterUtils.assertTargetDirectoryExists(parameter.getTargetDirectory());
    }

    void exportProduct(Product product, Logger logger) {
        final File fileLocation = product.getFileLocation();

        try {
            final FormatExporter exporter = FormatExporterFactory.create(fileLocation.getName());
            exporter.initialize(product, parameter);

            final File outputFile = getOutputFile(fileLocation, parameter.getTargetDirectory());
            // @todo 2 tb/tb extract method and move to helper class tb 2014-07-07
            if (outputFile.isFile() && parameter.isOverwriteTarget()) {
                if (!outputFile.delete()) {
                    throw new IOException("Unable to delete already existing product: " + outputFile.getAbsolutePath());
                }
            }
            final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());

            exporter.addGlobalAttributes(nFileWriteable, product.getMetadataRoot(), parameter);
            exporter.addDimensions(nFileWriteable);
            exporter.addVariables(nFileWriteable, parameter);

            nFileWriteable.create();

            exporter.writeData(nFileWriteable);

            nFileWriteable.close();
        } catch (IOException e) {
            logger.severe("Failed to convert file: " + fileLocation.getAbsolutePath());
            logger.severe(e.getMessage());
        }
    }

    void exportFile(File file, Logger logger ) {
        Product product = null;
        try {
            product = ProductIO.readProduct(file);
            if (product != null) {
                final String productType = product.getProductType();
                if (productType.matches(ExportParameter.PRODUCT_TYPE_REGEX)) {
                    exportProduct(product, logger);
                } else {
                    logger.info("Unable to convert file: " + file.getAbsolutePath());
                    logger.info("Unsupported product of type: " + productType);
                }
            } else {
                logger.warning("Unable to open file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.severe("Failed to convert file: " + file.getAbsolutePath());
            logger.severe(e.getMessage());
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    // @todo 2 tb/tb duplicated code, extract exporter baseclass tb 2014-07-04
    private static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc");
        return outFile;
    }
}
