package org.esa.beam.dataio.smos.dddb;


import org.esa.beam.util.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Before
    public void setUp() {
        resourceHandler = new ResourceHandler();
    }

    @Test
    public void testBuildPath() {
        String path = ResourceHandler.buildPath("DBL_SM_XXXX_MIR_SMDAP2_0300", "flags", ".csv");
        assertEquals("flags/MIR_/SMDAP2/DBL_SM_XXXX_MIR_SMDAP2_0300.csv", path);

        path = ResourceHandler.buildPath("DBL_SM_XXXX_MIR_BWLF1C_0300", "schemas", ".binXschema.xml");
        assertEquals("schemas/MIR_/BWLF1C/DBL_SM_XXXX_MIR_BWLF1C_0300.binXschema.xml", path);
    }

    @Test
    public void testGetResourceStream_fromJar() throws IOException {
        System.clearProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME);

        final InputStream resourceStream = resourceHandler.getResourceStream("bands/AUX_/DFFLAI/DBL_SM_XXXX_AUX_DFFLAI_0200.csv");
        assertNotNull(resourceStream);

        resourceStream.close();
    }

    @Test
    public void testGetResourceStream_fromDirectory() throws IOException {
        final String testFileName = "test_me";
        File targetDirectory = null;
        InputStream resourceStream = null;

        try {
            targetDirectory = createTestDirectory();
            System.setProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME, targetDirectory.getAbsolutePath());
            createTestFile(targetDirectory, testFileName);

            resourceStream = resourceHandler.getResourceStream(testFileName);
            assertNotNull(resourceStream);
        } finally {
            if (resourceStream != null) {
                resourceStream.close();
            }
            System.clearProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME);

            if (targetDirectory != null && targetDirectory.isDirectory()) {
                if (!FileUtils.deleteTree(targetDirectory)) {
                    fail("Unable to delete test directory");
                }
            }
        }
    }

    @Test
    public void testGetResourceURL_fromJar() throws IOException {
        System.clearProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME);
        final String resourcePath = "bands/AUX_/DGGFLO/DBL_SM_XXXX_AUX_DGGFLO_0400.csv";

        final URL resourceUrl = resourceHandler.getResourceUrl(resourcePath);
        assertNotNull(resourceUrl);
        assertThat(resourceUrl.getPath(), containsString(resourcePath));
        assertEquals("file", resourceUrl.getProtocol());
    }

    @Test
    public void testGetResourceURL_fromDirectory() throws MalformedURLException {
        File targetDirectory = null;

        try {
            targetDirectory = createTestDirectory();
            System.setProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME, targetDirectory.getAbsolutePath());

            final URL resourceUrl = resourceHandler.getResourceUrl("wurst");
            assertNotNull(resourceUrl);
            assertEquals(targetDirectory.getAbsolutePath() + File.separator + "wurst", resourceUrl.getPath());
            assertEquals("file", resourceUrl.getProtocol());

        } finally {
            System.clearProperty(ResourceHandler.SMOS_DDDB_DIR_PROPERTY_NAME);

            if (targetDirectory != null && targetDirectory.isDirectory()) {
                if (!FileUtils.deleteTree(targetDirectory)) {
                    fail("Unable to delete test directory");
                }
            }
        }
    }

    private void createTestFile(File targetDirectory, String testFileName) throws IOException {
        if (!new File(targetDirectory, testFileName).createNewFile()) {
            fail("unable to create test file.");
        }
    }

    private File createTestDirectory() {
        File targetDirectory;
        targetDirectory = new File("test_out");
        if (!targetDirectory.mkdirs()) {
            fail("unable to create test directory.");
        }
        return targetDirectory;
    }
}
