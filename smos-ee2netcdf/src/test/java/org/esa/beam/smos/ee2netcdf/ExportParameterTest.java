package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.smos.gui.BindingConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ExportParameterTest {

    private ExportParameter parameter;

    @Before
    public void setUp() {
        parameter = new ExportParameter();
    }

    @Test
    public void testSetIsUseSelectedProduct() {
        parameter.setUseSelectedProduct(true);
        assertTrue(parameter.isUseSelectedProduct());

        parameter.setUseSelectedProduct(false);
        assertFalse(parameter.isUseSelectedProduct());
    }

    @Test
    public void testSelectedProductAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.SELECTED_PRODUCT);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SELECTED_PRODUCT, parameter.alias());
    }

    @Test
    public void testSetGetSourceDirectory() {
        final File file = new File("hoppla");

        parameter.setSourceDirectory(file);
        assertEquals(file.getPath(), parameter.getSourceDirectory().getPath());
    }

    @Test
    public void testSourceDirectoryAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.SOURCE_DIRECTORY);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOURCE_DIRECTORY, parameter.alias());
    }

    @Test
    public void testSetIsOpenFileDialog() {
        parameter.setOpenFileDialog(true);
        assertTrue(parameter.isOpenFileDialog());

        parameter.setOpenFileDialog(false);
        assertFalse(parameter.isOpenFileDialog());
    }

    @Test
    public void testOpenFileDialogAnnotation() throws NoSuchFieldException {
        final Field selectedProductField = ExportParameter.class.getDeclaredField(BindingConstants.OPEN_FILE_DIALOG);
        final Parameter parameter = selectedProductField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.OPEN_FILE_DIALOG, parameter.alias());
    }

    // @todo 1 tb/tb write test for geometry access 2013-04-08
//    @Test
//    public void testSetGetGeometry() {
//        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//        builder.setName("sft");
//        builder.add("CAPITAL", String.class);
//        final SimpleFeatureType featureType = builder.buildFeatureType();
//
//        final VectorDataNode dataNode = new VectorDataNode("test", featureType);
//
//        parameter.setGeometry(dataNode);
//        assertEquals(dataNode, parameter.getGeometry());
//    }


    @Test
    public void testSetGetRoiType() {
        final int type_1 = 1;
        final int type_2 = 2;

        parameter.setRoiType(type_1);
        assertEquals(type_1, parameter.getRoiType());

        parameter.setRoiType(type_2);
        assertEquals(type_2, parameter.getRoiType());
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
    public void testSetGetNorthBound() {
        final double northBound_1 = 43.9;
        final double northBound_2 = -21.3;

        parameter.setNorthBound(northBound_1);
        assertEquals(northBound_1, parameter.getNorthBound(), 1e-8);

        parameter.setNorthBound(northBound_2);
        assertEquals(northBound_2, parameter.getNorthBound(), 1e-8);
    }

    @Test
    public void testSetGetEastBound() {
        final double eastBound_1 = 119.6;
        final double eastBound_2 = -3.5;

        parameter.setEastBound(eastBound_1);
        assertEquals(eastBound_1, parameter.getEastBound(), 1e-8);

        parameter.setEastBound(eastBound_2);
        assertEquals(eastBound_2, parameter.getEastBound(), 1e-8);
    }

    @Test
    public void testSetGetSouthBound() {
        final double southBound_1 = 9.6;
        final double southBound_2 = -52.6;

        parameter.setSouthBound(southBound_1);
        assertEquals(southBound_1, parameter.getSouthBound(), 1e-8);

        parameter.setSouthBound(southBound_2);
        assertEquals(southBound_2, parameter.getSouthBound(), 1e-8);
    }

    @Test
    public void testSetGetWestBound() {
        final double westBound_1 = 28.6;
        final double westBound_2 = -82.6;

        parameter.setWestBound(westBound_1);
        assertEquals(westBound_1, parameter.getWestBound(), 1e-8);

        parameter.setWestBound(westBound_2);
        assertEquals(westBound_2, parameter.getWestBound(), 1e-8);
    }

    @Test
    public void testSetGetTargetDirectory() {
        final File targetDirectory = new File("/test/directory");

        parameter.setTargetDirectory(targetDirectory);
        assertEquals(targetDirectory, parameter.getTargetDirectory());
    }

    @Test
    public void testConstruction() {
        assertEquals(0.0, parameter.getNorthBound(), 1e-8);
        assertEquals(0.0, parameter.getSouthBound(), 1e-8);
        assertEquals(0.0, parameter.getWestBound(), 1e-8);
        assertEquals(0.0, parameter.getEastBound(), 1e-8);

        assertFalse(parameter.isOverwriteTarget());
        final String[] outputBandNames = parameter.getVariableNames();
        assertNotNull(outputBandNames);
        assertEquals(0, outputBandNames.length);

        assertEquals(6, parameter.getCompressionLevel());
    }

    @Test
    public void testToAreaWKT() {
        parameter.setNorthBound(2.0);
        parameter.setEastBound(25.0);
        parameter.setSouthBound(1.0);
        parameter.setWestBound(24.0);

        assertEquals("POLYGON((24.0 2.0,25.0 2.0,25.0 1.0,24.0 1.0,24.0 2.0))", parameter.toAreaWKT());
    }

    @Test
    public void testSetIsOverwriteTarget() {
        parameter.setOverwriteTarget(true);
        assertTrue(parameter.isOverwriteTarget());

        parameter.setOverwriteTarget(false);
        assertFalse(parameter.isOverwriteTarget());
    }

    @Test
    public void testSetGetContact() {
        final String contact_1 = "take the phone";
        final String contact_2 = "write a letter";

        parameter.setContact(contact_1);
        assertEquals(contact_1, parameter.getContact());

        parameter.setContact(contact_2);
        assertEquals(contact_2, parameter.getContact());
    }

    @Test
    public void testSetGetInstitution() {
        final String institution_1 = "the Lab";
        final String institution_2 = "University of somewhere";

        parameter.setInstitution(institution_1);
        assertEquals(institution_1, parameter.getInstitution());

        parameter.setInstitution(institution_2);
        assertEquals(institution_2, parameter.getInstitution());
    }

    @Test
    public void testSetGetOutputBandNames() {
        final String[] bandNames = new String[3];
        bandNames[0] = "band_1";
        bandNames[1] = "band_2";
        bandNames[1] = "band_3";

        parameter.setVariableNames(bandNames);
        final String[] actualNames = parameter.getVariableNames();
        assertArrayEquals(bandNames, actualNames);
    }

    @Test
    public void testSetGetCompressionLevel() {
        final int level_1 = 1;
        final int level_2 = 7;

        parameter.setCompressionLevel(level_1);
        assertEquals(level_1, parameter.getCompressionLevel());

        parameter.setCompressionLevel(level_2);
        assertEquals(level_2, parameter.getCompressionLevel());
    }
}

