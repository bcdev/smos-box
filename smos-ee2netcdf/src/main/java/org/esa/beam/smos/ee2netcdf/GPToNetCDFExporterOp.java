package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.StringUtils;
import org.esa.beam.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

@OperatorMetadata(
        alias = GPToNetCDFExporterOp.ALIAS,
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2014 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF-GridPoint format.",
        autoWriteDisabled = true)
public class GPToNetCDFExporterOp extends Operator {

    public static final String ALIAS = "SmosGP2NetCDF";

    @SourceProducts(type = ExportParameter.PRODUCT_TYPE_REGEX,
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

    @Parameter(defaultValue = "",
            description = "Comma separated list of band names to export. If left empty, no band subsetting is applied.")
    private String outputBandNames;

    @Override
    public void initialize() throws OperatorException {
        final ExportParameter exportParameter = new ExportParameter();
        exportParameter.setTargetDirectory(targetDirectory);
        exportParameter.setInstitution(institution);
        exportParameter.setContact(contact);
        exportParameter.setOverwriteTarget(overwriteTarget);
        if (StringUtils.isNotNullAndNotEmpty(outputBandNames)) {
            final String[] bandNames = StringUtils.csvToArray(outputBandNames);
            final ArrayList<String> bandNamesList = new ArrayList<String>(bandNames.length);
            for (int i = 0; i < bandNames.length; i++) {
                bandNamesList.add(bandNames[i].trim());
            }
            exportParameter.setOutputBandNames(bandNamesList);
        }

        setDummyTargetProduct();

        final GPToNetCDFExporter gpToNetCDFExporter = new GPToNetCDFExporter(exportParameter);
        gpToNetCDFExporter.initialize();

        if (sourceProducts != null) {
            for (Product sourceProduct : sourceProducts) {
                gpToNetCDFExporter.exportProduct(sourceProduct, getLogger());
            }
        }

        if (sourceProductPaths != null) {
            final TreeSet<File> sourceFileSet = createInputFileSet(sourceProductPaths);

            for (File inputFile : sourceFileSet) {
                gpToNetCDFExporter.exportFile(inputFile, getLogger());
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
            super(GPToNetCDFExporterOp.class);
        }
    }
}
