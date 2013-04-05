package org.esa.beam.smos.ee2netcdf.visat;

import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConverterSwingWorkerTest {

    private final boolean isGuiAvailable;

    public ConverterSwingWorkerTest() {
        isGuiAvailable = !GraphicsEnvironment.isHeadless();
    }

    @Test
    public void testCreateMap_sourceDirectory() {
        final ExportParameter exportParameter = new ExportParameter();

        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        final String sourceDirectory = (String) parameterMap.get("sourceProductPaths");
        assertEquals(expectedSourceDir.getAbsolutePath(), sourceDirectory);
    }

    @Test
    public void testCreateMap_sourceDirectory_NotAddedWhenSingleProductSelected() {
        final ExportParameter exportParameter = new ExportParameter();

        final File expectedSourceDir = new File("/home/tom");
        exportParameter.setSourceDirectory(expectedSourceDir);
        exportParameter.setUseSelectedProduct(true);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        assertFalse(parameterMap.containsKey("sourceProductPaths"));
    }

    @Test
    public void testCreateMap_targetDirectory() {
        final ExportParameter exportParameter = new ExportParameter();

        final File expectedTargteDir = new File("/out/put");
        exportParameter.setTargetDirectory(expectedTargteDir);

        final HashMap<String, Object> parameterMap = ConverterSwingWorker.createParameterMap(exportParameter);
        final File targetDirectory = (File) parameterMap.get("targetDirectory");
        assertEquals(expectedTargteDir.getAbsolutePath(), targetDirectory.getAbsolutePath());
    }
}
