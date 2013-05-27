package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Coordinate;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.*;

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
        assertEquals(0, sourceProducts.count());
        assertEquals("MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2", sourceProducts.type());
        assertEquals(0, sourceProducts.bands().length);
        assertEquals("The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.",
                sourceProducts.description());
    }

    @Test
    public void testParameterAnnotation_sourceProductPaths() throws NoSuchFieldException {
        final Field targetDirectoryField = ConverterOp.class.getDeclaredField("sourceProductPaths");
        final Parameter sourceProductPaths = targetDirectoryField.getAnnotation(Parameter.class);
        assertEquals("Comma-separated list of file paths specifying the source products.\n" +
                "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
                "'*' (matches any character sequence in path names) and\n" +
                "'?' (matches any single character).", sourceProductPaths.description());
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
    public void testParameterAnnotations_Region() throws NoSuchFieldException {
        final Field regionField = ConverterOp.class.getDeclaredField("region");
        final Parameter regionFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("", regionFieldAnnotation.defaultValue());
        assertEquals("The geographical region as a geometry in well-known text format (WKT).", regionFieldAnnotation.description());
        assertEquals(JtsGeometryConverter.class, regionFieldAnnotation.converter());
        assertFalse(regionFieldAnnotation.notEmpty());
        assertFalse(regionFieldAnnotation.notNull());
    }

    @Test
    public void testParameterAnnotations_OverwriteTarget() throws NoSuchFieldException {
        final Field regionField = ConverterOp.class.getDeclaredField("overwriteTarget");
        final Parameter overwriteTargetFieldAnnotation = regionField.getAnnotation(Parameter.class);
        assertEquals("false", overwriteTargetFieldAnnotation.defaultValue());
        assertEquals("Set true to overwrite already existing target files.", overwriteTargetFieldAnnotation.description());
    }

    @Test
    public void testGetOutputFile() {
        final File input = new File("bla/bla/change_my_name.zip");
        final File targetDir = new File("/target/di/rectory");

        final File outputFile = ConverterOp.getOutputFile(input, targetDir);
        assertEquals("change_my_name.nc", outputFile.getName());
        assertEquals(targetDir.getAbsolutePath(), outputFile.getParentFile().getAbsolutePath());
    }

    @Test
    public void testCreateSubsetDef() {
        final Rectangle rectangle = new Rectangle(0, 1, 3, 4);

        final ProductSubsetDef subsetDef = ConverterOp.createSubsetDef(rectangle);
        assertNotNull(subsetDef);

        final Rectangle subsetDefRegion = subsetDef.getRegion();
        assertEquals(rectangle.toString(), subsetDefRegion.toString());

        assertNull(subsetDef.getNodeNames());
        assertEquals(1, subsetDef.getSubSamplingX());
        assertEquals(1, subsetDef.getSubSamplingY());
    }

    @Test
    public void testConvert() {
        final ArrayList<double[]> rawCoords = new ArrayList<double[]>();
        rawCoords.add(new double[]{1.0, 2.0});
        rawCoords.add(new double[]{3.0, 4.0});
        rawCoords.add(new double[]{5.0, 6.0});

        final Coordinate[] coordinates = ConverterOp.convert(rawCoords);
        assertNotNull(coordinates);
        assertEquals(3, coordinates.length);
        assertEquals(1.0, coordinates[0].x, 1e-8);
        assertEquals(2.0, coordinates[0].y, 1e-8);
        assertEquals(3.0, coordinates[1].x, 1e-8);
        assertEquals(4.0, coordinates[1].y, 1e-8);
        assertEquals(5.0, coordinates[2].x, 1e-8);
        assertEquals(6.0, coordinates[2].y, 1e-8);
    }

    @Test
    public void testCreateInputFileSet_emptyList() {
        final TreeSet<File> inputFileSet = ConverterOp.createInputFileSet(new String[0]);
        assertNotNull(inputFileSet);
        assertEquals(0, inputFileSet.size());
    }

    @Test
    public void testCreateInputFileSet_oneDir() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = ConverterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*"});
        assertNotNull(inputFileSet);
        assertEquals(2, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip", iterator.next().getName());
    }

    @Test
    public void testCreateInputFileSet_oneDir_wildcard() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = ConverterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*BWL*"});
        assertNotNull(inputFileSet);
        assertEquals(1, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
    }

    @Test
    public void testAlias() {
         assertEquals("SmosEE2NetCDF", ConverterOp.ALIAS);
    }

    private String getResourcePath() {
        File testDir = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/");
        if (!testDir.exists()) {
            testDir = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/");
        }
        return testDir.getPath();
    }
}
