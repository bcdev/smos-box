package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.N4FileWriteable;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

@OperatorMetadata(
        alias = GPtoNetCDFExporterOp.ALIAS,
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2014 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF-GridPoint format.",
        autoWriteDisabled = true)
public class GPtoNetCDFExporterOp extends Operator {

    public static final String ALIAS = "SmosGP2NetCDF";

    /**
     * Valid source product types are
     * <p/>
     * MIR_SM_SMUDP2
     * MIR_SM_OSUDP2
     * MIR_SM_SCxF1C
     * MIR_SM_SCxD1C
     * MIR_SM_BWxF1C
     * MIR_SM_BWxD1C
     */
    public static final String PRODUCT_TYPE_REGEX = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2";

    @SourceProducts(type = PRODUCT_TYPE_REGEX,
            description = "The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.")
    private Product[] sourceProducts;

    @Parameter(description = "Comma-separated list of file paths specifying the source products.\n" +
            "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
            "'*' (matches any character sequence in path names) and\n" +
            "'?' (matches any single character).")
    private String[] sourceProductPaths;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    private File targetDirectory;

    @Parameter(defaultValue = "false",
            description = "Set true to overwrite already existing target files.")
    private boolean overwriteTarget;

    @Parameter(description = "Set institution field for file metadata. If left empty, no institution metadata is written to output file.")
    private String institution;

    @Parameter(description = "Set contact field for file metadata. If left empty, no contact information is written to output file.")
    private String contact;

    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();

        assertTargetDirectoryExists();

        // @todo 1 tb/tb fill with values from annotated fields
        final ExportParameter exportParameter = new ExportParameter();

        if (sourceProducts != null) {
            for (Product sourceProduct : sourceProducts) {
                exportProduct(sourceProduct, exportParameter);
            }
        }

        if (sourceProductPaths != null) {
            final TreeSet<File> sourceFileSet = createInputFileSet(sourceProductPaths);

            for (File inputFile : sourceFileSet) {
                exportFile(inputFile, exportParameter);
            }
        }
    }

    // @todo 2 tb/tb duplicated code, extract exporter baseclass tb 2014-07-04
    private void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }

    // @todo 2 tb/tb duplicated code, extract exporter baseclass tb 2014-07-04
    private void assertTargetDirectoryExists() {
        if (!targetDirectory.isDirectory()) {
            if (!targetDirectory.mkdirs()) {
                throw new OperatorException("Unable to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }
    }

    // @todo 2 tb/tb duplicated code, extract exporter baseclass tb 2014-07-04
    public static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc");
        return outFile;
    }

    private void exportProduct(Product sourceProduct, ExportParameter exportParameter) {
        try {
            final FormatExporter exporter = FormatExporterFactory.create(sourceProduct.getFileLocation().getName());
            exporter.initialize(sourceProduct);

            final File outputFile = getOutputFile(sourceProduct.getFileLocation(), targetDirectory);
            final NFileWriteable nFileWriteable = N4FileWriteable.create(outputFile.getPath());

            exporter.addGlobalAttributes(nFileWriteable, sourceProduct.getMetadataRoot(), exportParameter);
            exporter.addDimensions(nFileWriteable);
            exporter.addVariables(nFileWriteable);

            nFileWriteable.create();

            exporter.writeData(nFileWriteable);

            nFileWriteable.close();
        } catch (IOException e) {

        }
        // @todo 1 tb/tb check if we need a finally block here tb 2014-07-04
    }

    private void exportFile(File inputFile, ExportParameter exportParameter) {

    }

    // @todo 2 tb/tb duplicated code, extract exporter baseclass tb 2014-07-04
    static TreeSet<File> createInputFileSet(String[] sourceProductPaths) {
        final TreeSet<File> sourceFileSet = new TreeSet<>();
        try {
            for (String sourceProductPath : sourceProductPaths) {
                sourceProductPath = sourceProductPath.trim();
                WildcardMatcher.glob(sourceProductPath, sourceFileSet);
            }
        } catch (IOException e) {
            throw new OperatorException(e.getMessage());
        }
        return sourceFileSet;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(GPtoNetCDFExporterOp.class);
        }
    }
}
