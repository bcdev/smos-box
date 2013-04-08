package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.smos.gui.BindingConstants;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConverterSwingWorkerTest {

    //private final boolean isGuiAvailable;
    private ExportParameter exportParameter;

//    public ConverterSwingWorkerTest() {
//        isGuiAvailable = !GraphicsEnvironment.isHeadless();
//    }

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
        assertEquals(expectedSourceDir.getAbsolutePath(), sourceDirectory);
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
        final File expectedTargteDir = new File("/out/put");
        exportParameter.setTargetDirectory(expectedTargteDir);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        final File targetDirectory = (File) parameterMap.get("targetDirectory");
        assertEquals(expectedTargteDir.getAbsolutePath(), targetDirectory.getAbsolutePath());
    }

    @Test
    public void testCreateMap_Area() {
        exportParameter.setNorth(22.9);
        exportParameter.setEast(100.6);
        exportParameter.setSouth(11.8);
        exportParameter.setWest(98.06);
        exportParameter.setRoiType(BindingConstants.ROI_TYPE_AREA);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        assertEquals("POLYGON((98.06 22.9,100.6 22.9,100.6 11.8,98.06 11.8,98.06 22.9))", parameterMap.get("region"));
    }
}
