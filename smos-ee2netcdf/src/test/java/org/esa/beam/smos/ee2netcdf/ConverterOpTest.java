package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.junit.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConverterOpTest {

    @Test
    public void testOperatorAnnotations() {
        final Annotation[] declaredAnnotations = ConverterOp.class.getDeclaredAnnotations();

        assertEquals(1, declaredAnnotations.length);
        final OperatorMetadata operatorMetadata = (OperatorMetadata) declaredAnnotations[0];
        assertEquals("SmosEE2NetCDF", operatorMetadata.alias());
        assertEquals("0.1", operatorMetadata.version());
        assertEquals("Tom Block", operatorMetadata.authors());
        assertEquals("(c) 2013 by Brockmann Consult", operatorMetadata.copyright());
        assertEquals("Converts SMOS EE Products to NetCDF format.", operatorMetadata.description());
    }

    @Test
    public void testParameterAnnotations_SourceProducts() throws NoSuchFieldException {
        final Field sourceProductsField = ConverterOp.class.getDeclaredField("sourceProducts");
        final SourceProducts sourceProducts = sourceProductsField.getAnnotation(SourceProducts.class);
        assertEquals(-1, sourceProducts.count());
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUPD2", sourceProducts.type());
        assertEquals("", sourceProducts.description());
        assertEquals(0, sourceProducts.bands().length);
    }

    @Test
    public void testParameterAnnotation_targetDirectory() throws NoSuchFieldException {
        final Field targetDirectoryField = ConverterOp.class.getDeclaredField("targetDirectory");
        final Parameter targetDirectory = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals(".", targetDirectory.defaultValue());
        assertEquals("The target directory for the converted data. If not existing, directory will be created.", targetDirectory.description());
        assertTrue(targetDirectory.notEmpty());
        assertTrue(targetDirectory.notNull());
    }

    @Test
    public void testGetOutputFile() {
        final File input = new File("bla/bla/change_my_name.zip");
        final File targetDir = new File("/target/di/rectory");

        final File outputFile = ConverterOp.getOutputFile(input, targetDir);
        assertEquals("change_my_name.nc4", outputFile.getName());
        assertEquals(targetDir.getAbsolutePath(), outputFile.getParentFile().getAbsolutePath());
    }

}
