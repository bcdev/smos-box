package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

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
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUPD2|MIR_SMUPD2", sourceProducts.type());
        assertEquals("", sourceProducts.description());
        assertEquals(0, sourceProducts.bands().length);
    }

}
