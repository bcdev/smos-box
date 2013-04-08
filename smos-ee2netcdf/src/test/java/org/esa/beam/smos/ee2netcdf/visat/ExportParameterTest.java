package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.smos.gui.BindingConstants;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

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
    public void testSetIsOpenFileDialog() {
        parameter.setOpenFileDialog(true);
        assertTrue(parameter.isOpenFileDialog());

        parameter.setOpenFileDialog(false);
        assertFalse(parameter.isOpenFileDialog());
    }

    @Test
    public void testSetGetGeometry() {
        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("sft");
        builder.add("CAPITAL", String.class);
        final SimpleFeatureType featureType = builder.buildFeatureType();

        final VectorDataNode dataNode = new VectorDataNode("test", featureType);

        parameter.setGeometry(dataNode);
        assertEquals(dataNode, parameter.getGeometry());
    }

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
    public void testSetGetNorth() {
        final double north_1 = 33.9;
        final double north_2 = -11.3;

        parameter.setNorth(north_1);
        assertEquals(north_1, parameter.getNorth(), 1e-8);

        parameter.setNorth(north_2);
        assertEquals(north_2, parameter.getNorth(), 1e-8);
    }

    @Test
    public void testNorthAnnotation() throws NoSuchFieldException {
        final Field northField = ExportParameter.class.getDeclaredField(BindingConstants.NORTH);
        final Parameter parameter = northField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.NORTH, parameter.alias());
        assertEquals("90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetEast() {
        final double east_1 = 109.6;
        final double east_2 = -2.5;

        parameter.setEast(east_1);
        assertEquals(east_1, parameter.getEast(), 1e-8);

        parameter.setEast(east_2);
        assertEquals(east_2, parameter.getEast(), 1e-8);
    }

    @Test
    public void testEastAnnotation() throws NoSuchFieldException {
        final Field eastField = ExportParameter.class.getDeclaredField(BindingConstants.EAST);
        final Parameter parameter = eastField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.EAST, parameter.alias());
        assertEquals("180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetSouth() {
        final double south_1 = 8.6;
        final double south_2 = -42.6;

        parameter.setSouth(south_1);
        assertEquals(south_1, parameter.getSouth(), 1e-8);

        parameter.setSouth(south_2);
        assertEquals(south_2, parameter.getSouth(), 1e-8);
    }

    @Test
    public void testSouthAnnotation() throws NoSuchFieldException {
        final Field southField = ExportParameter.class.getDeclaredField(BindingConstants.SOUTH);
        final Parameter parameter = southField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.SOUTH, parameter.alias());
        assertEquals("-90.0", parameter.defaultValue());
        assertEquals("[-90.0, 90.0]", parameter.interval());
    }

    @Test
    public void testSetGetWest() {
        final double west_1 = 18.6;
        final double west_2 = -72.6;

        parameter.setWest(west_1);
        assertEquals(west_1, parameter.getWest(), 1e-8);

        parameter.setWest(west_2);
        assertEquals(west_2, parameter.getWest(), 1e-8);
    }

    @Test
    public void testWestAnnotation() throws NoSuchFieldException {
        final Field westField = ExportParameter.class.getDeclaredField(BindingConstants.WEST);
        final Parameter parameter = westField.getAnnotation(Parameter.class);
        assertEquals(BindingConstants.WEST, parameter.alias());
        assertEquals("-180.0", parameter.defaultValue());
        assertEquals("[-180.0, 180.0]", parameter.interval());
    }

    @Test
    public void testSetGetTargetDirectory() {
        final File targetDirectory = new File("/test/directory");

        parameter.setTargetDirectory(targetDirectory);
        assertEquals(targetDirectory, parameter.getTargetDirectory());
    }

    @Test
    public void testConstruction() {
         assertEquals(90.0, parameter.getNorth(), 1e-8);
         assertEquals(-90.0, parameter.getSouth(), 1e-8);
         assertEquals(-180.0, parameter.getWest(), 1e-8);
         assertEquals(180.0, parameter.getEast(), 1e-8);
    }

    @Test
    public void testToAreaWKT() {
        parameter.setNorth(2.0);
        parameter.setEast(25.0);
        parameter.setSouth(1.0);
        parameter.setWest(24.0);

        assertEquals("POLYGON((24.0 2.0,25.0 2.0,25.0 1.0,24.0 1.0,24.0 2.0))", parameter.toAreaWKT());
    }
}

