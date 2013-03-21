package org.esa.beam.smos.ee2netdf;

import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.experimental.Output;

@OperatorMetadata(
        alias = "SmosEE2NetCDF",
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF format.")
public class ConverterOp extends Operator implements Output {

    @Override
    public void initialize() throws OperatorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
