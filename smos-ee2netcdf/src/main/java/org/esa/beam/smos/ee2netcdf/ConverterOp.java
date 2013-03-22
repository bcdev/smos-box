package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.experimental.Output;

import java.io.File;

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

        for (Product sourceProduct : sourceProducts) {
        }
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
