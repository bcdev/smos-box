package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.experimental.Output;

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
                    type = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUPD2|MIR_SMUPD2")
    private Product[] sourceProducts;

    @Override
    public void initialize() throws OperatorException {
    }
}
