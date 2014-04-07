package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.framework.datamodel.Product;

import java.io.IOException;

interface FormatExporter {
     void initialize(Product product);
     void addGlobalAttributes(NFileWriteable nFileWriteable) throws IOException;
     void addDimensions(NFileWriteable nFileWriteable) throws IOException;
}
