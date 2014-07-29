package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.SmosProductReaderPlugIn;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

class GPToNetCDFExporter {

    private final ExportParameter parameter;

    GPToNetCDFExporter(ExportParameter parameter) {
        this.parameter = parameter;
    }

    void initialize() {
        ExporterUtils.assertTargetDirectoryExists(parameter.getTargetDirectory());
    }

    void exportProduct(Product product, Logger logger) {
        final File fileLocation = product.getFileLocation();

        NFileWriteable nFileWriteable = null;
        try {
            logger.info("Converting product: " + fileLocation.getPath() + " ...");
            final FormatExporter exporter = FormatExporterFactory.create(fileLocation.getName());
            exporter.initialize(product, parameter);

            final File outputFile = getOutputFile(fileLocation, parameter.getTargetDirectory());
            if (outputFile.isFile()) {
                if (parameter.isOverwriteTarget()) {
                    if (!outputFile.delete()) {
                        throw new IOException("Unable to delete already existing product: " + outputFile.getAbsolutePath());
                    }
                } else {
                    logger.warning("output file '" + outputFile.getPath() + "' exists. Output will not be overwritten.");
                    return;
                }
            }

            nFileWriteable = N4FileWriteable.create(outputFile.getPath());

            exporter.prepareGeographicSubset(nFileWriteable, parameter);
            exporter.addGlobalAttributes(nFileWriteable, product.getMetadataRoot(), parameter);
            exporter.addDimensions(nFileWriteable);
            exporter.addVariables(nFileWriteable, parameter);

            nFileWriteable.create();

            exporter.writeData(nFileWriteable);
            logger.info("Success. Wrote target product: " + outputFile.getPath());
        } catch (Exception e) {
            logger.severe("Failed to convert file: " + fileLocation.getAbsolutePath());
            logger.severe(e.getMessage());
        } finally {
            if (nFileWriteable != null) {
                try {
                    nFileWriteable.close();
                } catch (IOException e) {
                    logger.severe("Failed to close file: " + fileLocation.getAbsolutePath());
                    logger.severe(e.getMessage());
                }
            }
        }
    }

    void exportFile(File file, Logger logger) {
        Product product = null;
        try {
            final SmosProductReaderPlugIn readerPlugIn = new SmosProductReaderPlugIn();
            final DecodeQualification decodeQualification = readerPlugIn.getDecodeQualification(file);
            if (decodeQualification == DecodeQualification.INTENDED) {
                product = readerPlugIn.createReaderInstance().readProductNodes(file, null);
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
