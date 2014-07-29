package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

class ExporterUtils {

    static void assertTargetDirectoryExists(File targetDirectory) {
        if (!targetDirectory.isDirectory()) {
            if (!targetDirectory.mkdirs()) {
                throw new OperatorException("Unable to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }
    }

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
}
