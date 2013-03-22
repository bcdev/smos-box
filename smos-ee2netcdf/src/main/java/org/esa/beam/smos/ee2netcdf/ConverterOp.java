package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductWriter;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.experimental.Output;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.IOException;

@OperatorMetadata(
        alias = "SmosEE2NetCDF",
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF format.")
public class ConverterOp extends Operator implements Output {

    // input product types:
    // MIR_SM_BWxD1C
    // MIR_SM_BWxF1C
    // MIR_SM_SCxD1C
    // MIR_SM_SCxF1C
    // MIR_SM_OSUDP2
    // MIR_SM_SMUDP2

    @SourceProducts(count = -1,
            type = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUPD2")
    private Product[] sourceProducts;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    private File targetDirectory;

    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();

        assertTargetDirectoryExists();

        try {
            // @todo 2 tb/tb change so that a single file failure does not kill the complete batch-run tb 2013-03-21
            convertProducts();
        } catch (IOException e) {
            throw new OperatorException(e.getMessage());
        }
    }

    private void convertProducts() throws IOException {
        final ProductWriter ncWriter = ProductIO.getProductWriter("NetCDF4-CF");

        for (Product sourceProduct : sourceProducts) {
            final SmosProductReader productReader = (SmosProductReader) sourceProduct.getProductReader();
            final ExplorerFile explorerFile = productReader.getExplorerFile();

            final File outFile = getOutputFile(explorerFile.getDblFile(), targetDirectory);
            outFile.createNewFile();

            ncWriter.writeProductNodes(sourceProduct, outFile);
        }
    }

    // package access for testing only - tb 2013-03-21
    static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc4");
        return outFile;
    }

    private void assertTargetDirectoryExists() {
        if (!targetDirectory.isDirectory()) {
            if (!targetDirectory.mkdirs()) {
                throw new OperatorException("Unable to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }
    }

    private void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }
}
