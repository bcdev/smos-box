package org.esa.beam.smos.ee2netcdf.visat;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}

