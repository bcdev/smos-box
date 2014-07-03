package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GPtoNetCDFExporterOpTest {

    @Test
    public void testOperatorAnnotations() {
        final Annotation[] declaredAnnotations = GPtoNetCDFExporterOp.class.getDeclaredAnnotations();

        assertEquals(1, declaredAnnotations.length);
        final OperatorMetadata operatorMetadata = (OperatorMetadata) declaredAnnotations[0];
        assertEquals("SmosGP2NetCDF", operatorMetadata.alias());
        assertEquals("0.1", operatorMetadata.version());
        assertEquals("Tom Block", operatorMetadata.authors());
        assertEquals("(c) 2014 by Brockmann Consult", operatorMetadata.copyright());
        assertEquals("Converts SMOS EE Products to NetCDF-GridPoint format.", operatorMetadata.description());
    }

    @Test
    public void testParameterAnnotations_SourceProducts() throws NoSuchFieldException {
        final Field sourceProductsField = GPtoNetCDFExporterOp.class.getDeclaredField("sourceProducts");
        final SourceProducts sourceProducts = sourceProductsField.getAnnotation(SourceProducts.class);
        assertEquals(0, sourceProducts.count());
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2", sourceProducts.type());
        assertEquals(0, sourceProducts.bands().length);
        assertEquals("The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.",
                sourceProducts.description());
    }

    @Test
    public void testParameterAnnotation_sourceProductPaths() throws NoSuchFieldException {
        final Field targetDirectoryField = GPtoNetCDFExporterOp.class.getDeclaredField("sourceProductPaths");
        final Parameter sourceProductPaths = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals("Comma-separated list of file paths specifying the source products.\n" +
                "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
                "'*' (matches any character sequence in path names) and\n" +
                "'?' (matches any single character).", sourceProductPaths.description());
    }

    @Test
    public void testParameterAnnotation_targetDirectory() throws NoSuchFieldException {
        final Field targetDirectoryField = GPtoNetCDFExporterOp.class.getDeclaredField("targetDirectory");
        final Parameter targetDirectory = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals(".", targetDirectory.defaultValue());
        assertEquals("The target directory for the converted data. If not existing, directory will be created.", targetDirectory.description());
        assertTrue(targetDirectory.notEmpty());
        assertTrue(targetDirectory.notNull());
    }

    @Test
    public void testParameterAnnotations_OverwriteTarget() throws NoSuchFieldException {
        final Field overwriteTargetField = GPtoNetCDFExporterOp.class.getDeclaredField("overwriteTarget");
        final Parameter overwriteTargetFieldAnnotation = overwriteTargetField.getAnnotation(Parameter.class);
        assertEquals("false", overwriteTargetFieldAnnotation.defaultValue());
        assertEquals("Set true to overwrite already existing target files.", overwriteTargetFieldAnnotation.description());
    }

    @Test
    public void testParameterAnnotations_Institution() throws NoSuchFieldException {
        final Field institutionField = GPtoNetCDFExporterOp.class.getDeclaredField("institution");
        final Parameter institutionFieldAnnotation = institutionField.getAnnotation(Parameter.class);
        assertEquals("", institutionFieldAnnotation.defaultValue());
        assertEquals("Set institution field for file metadata. If left empty, no institution metadata is written to output file.", institutionFieldAnnotation.description());
    }

    @Test
    public void testParameterAnnotations_Contact() throws NoSuchFieldException {
        final Field contactField = GPtoNetCDFExporterOp.class.getDeclaredField("contact");
        final Parameter contactFieldAnnotation = contactField.getAnnotation(Parameter.class);
        assertEquals("", contactFieldAnnotation.defaultValue());
        assertEquals("Set contact field for file metadata. If left empty, no contact information is written to output file.", contactFieldAnnotation.description());
    }
}
