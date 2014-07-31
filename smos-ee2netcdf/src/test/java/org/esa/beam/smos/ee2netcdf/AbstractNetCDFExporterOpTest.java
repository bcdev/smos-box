package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AbstractNetCDFExporterOpTest {

    @Test
    public void testParameterAnnotations_SourceProducts() throws NoSuchFieldException {
        final Field sourceProductsField = AbstractNetCDFExporterOp.class.getDeclaredField("sourceProducts");
        final SourceProducts sourceProducts = sourceProductsField.getAnnotation(SourceProducts.class);
        assertEquals(0, sourceProducts.count());
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2", sourceProducts.type());
        assertEquals(0, sourceProducts.bands().length);
        assertEquals("The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.",
                sourceProducts.description());
    }

    @Test
    public void testParameterAnnotation_sourceProductPaths() throws NoSuchFieldException {
        final Field targetDirectoryField = AbstractNetCDFExporterOp.class.getDeclaredField("sourceProductPaths");
        final Parameter sourceProductPaths = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals("Comma-separated list of file paths specifying the source products.\n" +
                "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
                "'*' (matches any character sequence in path names) and\n" +
                "'?' (matches any single character).", sourceProductPaths.description());
    }

    @Test
    public void testParameterAnnotation_targetDirectory() throws NoSuchFieldException {
        final Field targetDirectoryField = AbstractNetCDFExporterOp.class.getDeclaredField("targetDirectory");
        final Parameter targetDirectory = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals(".", targetDirectory.defaultValue());
        assertEquals("The target directory for the converted data. If not existing, directory will be created.", targetDirectory.description());
        assertTrue(targetDirectory.notEmpty());
        assertTrue(targetDirectory.notNull());
    }

    @Test
    public void testParameterAnnotations_OverwriteTarget() throws NoSuchFieldException {
        final Field regionField = AbstractNetCDFExporterOp.class.getDeclaredField("overwriteTarget");
        final Parameter overwriteTargetFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("false", overwriteTargetFieldAnnotation.defaultValue());
        assertEquals("Set true to overwrite already existing target files.", overwriteTargetFieldAnnotation.description());
    }

    @Test
    public void testParameterAnnotations_Region() throws NoSuchFieldException {
        final Field regionField = AbstractNetCDFExporterOp.class.getDeclaredField("region");
        final Parameter regionFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("", regionFieldAnnotation.defaultValue());
        assertEquals("Target geographical region as a geometry in well-known text format (WKT). The output product will be tailored according to the region.", regionFieldAnnotation.description());
        assertEquals(JtsGeometryConverter.class, regionFieldAnnotation.converter());
        assertFalse(regionFieldAnnotation.notEmpty());
        assertFalse(regionFieldAnnotation.notNull());
    }
}
