package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.smos.gui.BindingConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;

public class ConverterSwingWorkerTest {

    private ExportParameter exportParameter;

    @Before
    public void setUp() throws Exception {
        exportParameter = new ExportParameter();
    }

    @Test
    public void testCreateMap_sourceDirectory() {
        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        final String sourceDirectory = (String) parameterMap.get("sourceProductPaths");
        final String absolutePath = expectedSourceDir.getAbsolutePath();
        assertEquals(absolutePath + File.separator + "*.zip," + absolutePath + File.separator + "*.dbl", sourceDirectory);
    }

    @Test
    public void testCreateMap_sourceDirectory_NotAddedWhenSingleProductSelected() {
        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);
        exportParameter.setUseSelectedProduct(true);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        assertFalse(parameterMap.containsKey("sourceProductPaths"));
    }

    @Test
    public void testCreateMap_targetDirectory() {
        final File expectedTargetDir = new File("/out/put");
        exportParameter.setTargetDirectory(expectedTargetDir);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        final File targetDirectory = (File) parameterMap.get("targetDirectory");
        assertEquals(expectedTargetDir.getAbsolutePath(), targetDirectory.getAbsolutePath());
    }

    @Test
    public void testCreateMap_area() {
        exportParameter.setNorthBound(22.9);
        exportParameter.setEastBound(100.6);
        exportParameter.setSouthBound(11.8);
        exportParameter.setWestBound(98.06);
        exportParameter.setRoiType(BindingConstants.ROI_TYPE_AREA);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        assertEquals("POLYGON((98.06 22.9,100.6 22.9,100.6 11.8,98.06 11.8,98.06 22.9))", parameterMap.get("region"));
    }

    @Test
    public void testCreateMap_wholeProduct() {
        exportParameter.setRoiType(BindingConstants.ROI_TYPE_PRODUCT);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        assertNull(parameterMap.get("region"));
    }

    @Test
    public void testCreateInputPathWildcards() {
        final File inputDir = new File("data");
        final String pathWildcards = ConverterSwingWorker.createInputPathWildcards(inputDir);

        final String absolutePath = inputDir.getAbsolutePath();
        final StringBuilder expected = new StringBuilder();
        expected.append(absolutePath);
        expected.append(File.separator);
        expected.append("*.zip,");
        expected.append(absolutePath);
        expected.append(File.separator);
        expected.append("*.dbl");
        assertEquals(expected.toString(), pathWildcards);
    }
}
