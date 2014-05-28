package org.esa.beam.smos.visat.export;

import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.smos.gui.BindingConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ExportParameterTest {

    private ExportParameter exportParameter;

    @Before
    public void setUp() {
        exportParameter = new ExportParameter();
    }

    @Test
    public void testSetIsUseSelectedProduct() {
        exportParameter.setUseSelectedProduct(true);
        assertTrue(exportParameter.isUseSelectedProduct());

        exportParameter.setUseSelectedProduct(false);
        assertFalse(exportParameter.isUseSelectedProduct());
    }

    @Test
    public void testUseSelectedProductAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.SELECTED_PRODUCT);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SELECTED_PRODUCT, parameter.alias());
    }

    @Test
    public void testSetGetSourceDirectory() {
        final File sourceDirectory = new File("where/ever/my/source");

        exportParameter.setSourceDirectory(sourceDirectory);
        assertEquals(sourceDirectory.getPath(), exportParameter.getSourceDirectory().getPath());
    }

    @Test
    public void testSourceDirectoryAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.SOURCE_DIRECTORY);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOURCE_DIRECTORY, parameter.alias());
    }

    @Test
    public void testSetIsOpenFileDialog() {
        exportParameter.setOpenFileDialog(true);
        assertTrue(exportParameter.isOpenFileDialog());

        exportParameter.setOpenFileDialog(false);
        assertFalse(exportParameter.isOpenFileDialog());
    }

    @Test
    public void testOpenFileDialogAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.OPEN_FILE_DIALOG);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.OPEN_FILE_DIALOG, parameter.alias());
    }

    @Test
    public void testSetIsRecursive() {
        exportParameter.setRecursive(true);
        assertTrue(exportParameter.isRecursive());

        exportParameter.setRecursive(false);
        assertFalse(exportParameter.isRecursive());
    }

    @Test
    public void testRecursiveAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(GridPointExportDialog.ALIAS_RECURSIVE);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_RECURSIVE, parameter.alias());
        assertEquals("false", parameter.defaultValue());
    }

    @Test
    public void testSetGetRoiType() {
        exportParameter.setRoiType(5);
        assertEquals(5, exportParameter.getRoiType());
    }

    @Test
    public void testRoiTypeAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.ROI_TYPE);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.ROI_TYPE, parameter.alias());
        assertEquals("2", parameter.defaultValue());
        assertArrayEquals(new String[]{"0", "1", "2"}, parameter.valueSet());
    }

    @Test
    public void testSetGetNorth() {
        exportParameter.setNorth(56.22);
        assertEquals(56.22, exportParameter.getNorth(), 1e-8);

        exportParameter.setNorth(-19.55);
        assertEquals(-19.55, exportParameter.getNorth(), 1e-8);
    }

    @Test
    public void testNorthAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.NORTH);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.NORTH, parameter.alias());
        assertEquals("90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetSouth() {
        exportParameter.setSouth(-22.65);
        assertEquals(-22.65, exportParameter.getSouth(), 1e-8);

        exportParameter.setSouth(3.018);
        assertEquals(3.018, exportParameter.getSouth(), 1e-8);
    }

    @Test
    public void testSouthAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.SOUTH);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOUTH, parameter.alias());
        assertEquals("-90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetEast() {
        exportParameter.setEast(29.01);
        assertEquals(29.01, exportParameter.getEast(), 1e-8);

        exportParameter.setEast(-11.5);
        assertEquals(-11.5, exportParameter.getEast(), 1e-8);
    }

    @Test
    public void testEastAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.EAST);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.EAST, parameter.alias());
        assertEquals("180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetWest() {
        exportParameter.setWest(30.02);
        assertEquals(30.02, exportParameter.getWest(), 1e-8);

        exportParameter.setWest(-12.6);
        assertEquals(-12.6, exportParameter.getWest(), 1e-8);
    }

    @Test
    public void testWestAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.WEST);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.WEST, parameter.alias());
        assertEquals("-180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetTargetFile() {
        final File targetFile = new File("target/file");

        exportParameter.setTargetFile(targetFile);
        assertEquals(targetFile.getPath(), exportParameter.getTargetFile().getPath());
    }

    @Test
    public void testTargetFileAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField("targetFile");
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_TARGET_FILE, parameter.alias());
        assertTrue(parameter.notNull());
        assertTrue(parameter.notEmpty());
    }

    @Test
    public void testSetGetExportFormat() {
        exportParameter.setExportFormat("wurst");
        assertEquals("wurst", exportParameter.getExportFormat());

        exportParameter.setExportFormat("xls");
        assertEquals("xls", exportParameter.getExportFormat());
    }

    @Test
    public void testExportFormatAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(GridPointExportDialog.ALIAS_EXPORT_FORMAT);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(GridPointExportDialog.ALIAS_EXPORT_FORMAT, parameter.alias());
        assertEquals(GridPointExportDialog.NAME_CSV, parameter.defaultValue());
        assertArrayEquals(new String[]{GridPointExportDialog.NAME_CSV, GridPointExportDialog.NAME_EEF}, parameter.valueSet());
    }

    @Test
    public void testGetClone() {
        final File sourceDirectory = new File("source/dir");
        final File targetFile = new File("target/file");

        exportParameter.setUseSelectedProduct(true);
        exportParameter.setSourceDirectory(sourceDirectory);
        exportParameter.setOpenFileDialog(true);
        exportParameter.setRecursive(true);
        exportParameter.setRoiType(6);
        exportParameter.setNorth(7.1);
        exportParameter.setSouth(8.2);
        exportParameter.setEast(9.3);
        exportParameter.setWest(10.4);
        exportParameter.setTargetFile(targetFile);
        exportParameter.setExportFormat("word");

        final ExportParameter clone = exportParameter.getClone();
        assertNotNull(clone);
        assertNotSame(clone, exportParameter);

        assertEquals(exportParameter.isUseSelectedProduct(), clone.isUseSelectedProduct());
        assertEquals(sourceDirectory.getPath(), clone.getSourceDirectory().getPath());
        assertEquals(exportParameter.isOpenFileDialog(), clone.isOpenFileDialog());
        assertEquals(exportParameter.isRecursive(), clone.isRecursive());
        assertEquals(exportParameter.getRoiType(), clone.getRoiType());
        assertEquals(exportParameter.getNorth(), clone.getNorth(), 1e-8);
        assertEquals(exportParameter.getSouth(), clone.getSouth(), 1e-8);
        assertEquals(exportParameter.getEast(), clone.getEast(), 1e-8);
        assertEquals(exportParameter.getWest(), clone.getWest(), 1e-8);
        assertEquals(targetFile.getPath(), clone.getTargetFile().getPath());
        assertEquals(exportParameter.getExportFormat(), clone.getExportFormat());
    }
}
