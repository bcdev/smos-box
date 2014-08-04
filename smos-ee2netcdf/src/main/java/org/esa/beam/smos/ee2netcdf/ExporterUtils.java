package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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

    static MetadataElement getSpecificProductHeader(Product product) {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement variableHeader = metadataRoot.getElement("Variable_Header");
        if (variableHeader == null) {
            return null;
        }

        final MetadataElement specificProductHeader = variableHeader.getElement("Specific_Product_Header");
        if (specificProductHeader == null) {
            return null;
        }
        return specificProductHeader;
    }

    static void correctScaleFactor(Map<String, VariableDescriptor> variableDescriptors, String variableName, double scaleCorrection) {
        final VariableDescriptor variableDescriptor = variableDescriptors.get(variableName);
        if (variableDescriptor != null) {
            final double originalScaleFactor = variableDescriptor.getScaleFactor();
            variableDescriptor.setScaleFactor(originalScaleFactor * scaleCorrection);
        }
    }
}
