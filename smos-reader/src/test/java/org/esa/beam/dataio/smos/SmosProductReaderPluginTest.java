package org.esa.beam.dataio.smos;

import org.esa.beam.util.io.BeamFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SmosProductReaderPluginTest {

    private SmosProductReaderPlugIn plugIn;

    @Before
    public void setUp(){
        plugIn = new SmosProductReaderPlugIn();
    }

    @Test
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();
        assertEquals(2, inputTypes.length);
        assertEquals(File.class, inputTypes[0]);
        assertEquals(String.class, inputTypes[1]);
    }

    @Test
    public void testGetDefaultFileExtensions() {
        final String[] extensions = plugIn.getDefaultFileExtensions();
        assertEquals(5, extensions.length);

        assertEquals(".HDR", extensions[0]);
        assertEquals(".DBL", extensions[1]);
        assertEquals(".zip", extensions[2]);
        assertEquals(".ZIP", extensions[3]);
        assertEquals(".bin", extensions[4]);
    }

    @Test
    public void testGetDescription() {
        final String description = plugIn.getDescription(null);
        assertEquals("SMOS Data Products", description);
    }

    @Test
    public void testGetFormatNames(){
        final String[] formatNames = plugIn.getFormatNames();
        assertEquals(2, formatNames.length);
        assertEquals("SMOS-EEF", formatNames[0]);
        assertEquals("SMOS BUFR/CDM", formatNames[1]);
    }

    @Test
    public void testGetProductFileFilter() {
        final BeamFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertArrayEquals(plugIn.getDefaultFileExtensions(), productFileFilter.getExtensions());
        assertEquals(plugIn.getFormatNames()[0], productFileFilter.getFormatName());
        assertEquals("SMOS Data Products (*.HDR,*.DBL,*.zip,*.ZIP,*.bin)", productFileFilter.getDescription());
    }
}
