package org.esa.beam.smos.gui;

import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GuiHelperTest {

    @Test
    public void testGetDefaultSourceDirectory_fromUserHome() {
        final DefaultAppContext appContext = new DefaultAppContext("bla");

        final String propertyString = appContext.getPreferences().getPropertyString("org.esa.beam.smos.export.sourceDir");
        assertEquals("", propertyString);

        final String expected = System.getProperty("user.home", ".");

        assertEquals(expected, GuiHelper.getDefaultSourceDirectory(appContext).getPath());
    }

    @Test
    public void testGetDefaultSourceDirectory_fromPreferences() {
        final String expected = "/a/dir";
        final DefaultAppContext appContext = new DefaultAppContext("bla");

        appContext.getPreferences().setPropertyString("org.esa.beam.smos.export.sourceDir", expected);

        assertEquals(expected, GuiHelper.getDefaultSourceDirectory(appContext).getPath());
    }
}
