package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.smos.SmosUtils;

import java.io.IOException;

class FormatExporterFactory {

    public static FormatExporter create(String fileName) throws IOException {
        if (SmosUtils.isBrowseFormat(fileName)) {
            return new BrowseFormatExporter();
        } else if (SmosUtils.isL1cType(fileName)) {
            return new L1CFormatExporter();
        } else if (SmosUtils.isOsUserFormat(fileName) || SmosUtils.isSmUserFormat(fileName)) {
            return new L2FormatExporter();
        }
        throw new IOException("Unsupported export for file:" + fileName);
    }
}
