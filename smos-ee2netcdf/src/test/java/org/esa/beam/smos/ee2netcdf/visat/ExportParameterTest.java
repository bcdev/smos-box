package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.VectorDataNode;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;

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
}

