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

    @Test
    public void testSetDefaultSourceDirectory_fromPreferences() {
        assumeTrue(isGuiAvailable);

        final DefaultAppContext appContext = new DefaultAppContext("bla");
        final File file = new File("/default/source/directory");

        GuiHelper.setDefaultSourceDirectory(file, appContext);

        final String propertyString = appContext.getPreferences().getPropertyString("org.esa.beam.smos.export.sourceDir");
        assertEquals(file.getPath(), propertyString);
    }

    @Test
    public void testGetDefaultTargetDirectory_fromUserHome() {
        assumeTrue(isGuiAvailable);

        final DefaultAppContext appContext = new DefaultAppContext("bla");
        final String propertyString = appContext.getPreferences().getPropertyString("org.esa.beam.smos.export.targetDir");
        assertEquals("", propertyString);

        final String expected = System.getProperty("user.home", ".");

        assertEquals(expected, GuiHelper.getDefaultTargetDirectory(appContext).getPath());
    }

    @Test
    public void testGetDefaultTargetDirectory_fromPreferences() {
        assumeTrue(isGuiAvailable);

        final String expected = File.separator + "target" + File.separator + "directory";
        final DefaultAppContext appContext = new DefaultAppContext("bla");

        appContext.getPreferences().setPropertyString("org.esa.beam.smos.export.targetDir", expected);

        assertEquals(expected, GuiHelper.getDefaultTargetDirectory(appContext).getPath());
    }

    @Test
    public void testSetDefaultTargetDirectory() {
        assumeTrue(isGuiAvailable);

        final DefaultAppContext appContext = new DefaultAppContext("schwafel");
        final File file = new File("/default/target/directory");

        GuiHelper.setDefaultTargetDirectory(file, appContext);

        final String propertyString = appContext.getPreferences().getPropertyString("org.esa.beam.smos.export.targetDir");
        assertEquals(file.getPath(), propertyString);
    }
}
