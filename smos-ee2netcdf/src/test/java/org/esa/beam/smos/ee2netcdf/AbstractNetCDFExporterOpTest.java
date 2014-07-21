package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbstractNetCDFExporterOpTest {

    @Test
    public void testCreateInputFileSet_emptyList() {
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[0]);
        assertNotNull(inputFileSet);
        assertEquals(0, inputFileSet.size());
    }

    @Test
    public void testCreateInputFileSet_oneDir() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*"});
        assertNotNull(inputFileSet);
        assertEquals(4, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPEB_MIR_SMUDP2_20140413T185915_20140413T195227_551_026_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip", iterator.next().getName());
        assertEquals("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip", iterator.next().getName());
    }

    @Test
    public void testCreateInputFileSet_oneDir_wildcard() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = EEToNetCDFExporterOp.createInputFileSet(new String[]{resourcePath + File.separator + "*BWL*"});
        assertNotNull(inputFileSet);
        assertEquals(1, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
    }

    private String getResourcePath() {
        File testDir = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/");
        if (!testDir.exists()) {
            testDir = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/");
        }
        return testDir.getPath();
    }

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
}
