package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;

import java.io.IOException;

interface FormatExporter {
    void initialize(Product product, ExportParameter exportParameter) throws IOException;

    void addGlobalAttributes(NFileWriteable nFileWriteable, MetadataElement metadataRoot, ExportParameter exportParameter) throws IOException;

    void addDimensions(NFileWriteable nFileWriteable) throws IOException;

    void addVariables(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException;

    void writeData(NFileWriteable nFileWriteable) throws IOException;
}
