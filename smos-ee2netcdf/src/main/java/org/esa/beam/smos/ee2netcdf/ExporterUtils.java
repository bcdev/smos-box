package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.OperatorException;

import java.io.File;

class ExporterUtils {

    static void assertTargetDirectoryExists(File targetDirectory) {
        if (!targetDirectory.isDirectory()) {
            if (!targetDirectory.mkdirs()) {
                throw new OperatorException("Unable to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }
    }
}
