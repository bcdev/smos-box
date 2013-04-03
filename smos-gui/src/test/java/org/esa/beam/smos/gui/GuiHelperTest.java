package org.esa.beam.smos.gui;

import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.junit.Test;

import java.awt.*;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class GuiHelperTest {

    private final boolean isGuiAvailable;

    public GuiHelperTest() {
        isGuiAvailable = !GraphicsEnvironment.isHeadless();
    }

    @Test
    public void testGetDefaultSourceDirectory_fromUserHome() {
        assumeTrue(isGuiAvailable);

        final DefaultAppContext appContext = new DefaultAppContext("bla");
        final String propertyString = appContext.getPreferences().getPropertyString("org.esa.beam.smos.export.sourceDir");
        assertEquals("", propertyString);

        final String expected = System.getProperty("user.home", ".");

        assertEquals(expected, GuiHelper.getDefaultSourceDirectory(appContext).getPath());
    }

    @Test
    public void testGetDefaultSourceDirectory_fromPreferences() {
        assumeTrue(isGuiAvailable);

        final String expected = File.separator + "another" + File.separator + "dir";
        final DefaultAppContext appContext = new DefaultAppContext("bla");

        appContext.getPreferences().setPropertyString("org.esa.beam.smos.export.sourceDir", expected);

        assertEquals(expected, GuiHelper.getDefaultSourceDirectory(appContext).getPath());
    }
}
