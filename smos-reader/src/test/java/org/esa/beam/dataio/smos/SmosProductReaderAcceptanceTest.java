package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SmosProductReaderAcceptanceTest {

    private SmosProductReader reader;

    @Before
    public void setUp() {
        reader = new SmosProductReader(new SmosProductReaderPlugIn());
    }

    @Test
    public void testRead_BWSD1C_directory() throws IOException {
        final String testFileName = "SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.DBL";
        final File file = getTestFileOrDirectory("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1/" + testFileName);
        assertTrue(file.isFile());

        Product product = null;
        try {
            product = reader.readProductNodes(file, null);
            final File fileLocation = product.getFileLocation();
            assertTrue(fileLocation.getPath().contains(testFileName));
            assertCorrect_BWSD1C_Product(product);
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testRead_BWSD1C_plainZip() throws IOException {
        final String testFilename = "SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.zip";
        final File file = getTestFileOrDirectory(testFilename);
        assertTrue(file.isFile());

        Product product = null;
        try {
            product = reader.readProductNodes(file, null);
            final File fileLocation = product.getFileLocation();
            assertTrue(fileLocation.getPath().contains(testFilename));
            assertCorrect_BWSD1C_Product(product);
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testRead_BWSD1C_zipWithDirectory() throws IOException {
        final String testFileName = "SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1_with_dir.zip";
        final File file = getTestFileOrDirectory(testFileName);
        assertTrue(file.isFile());

        Product product = null;
        try {
            product = reader.readProductNodes(file, null);
            final File fileLocation = product.getFileLocation();
            assertTrue(fileLocation.getPath().contains(testFileName));
            assertCorrect_BWSD1C_Product(product);
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    private static File getTestFileOrDirectory(String file) {
        File testTgz = new File("./smos-reader/src/test/resources/org/esa/beam/dataio/smos/" + file);
        if (!testTgz.exists()) {
            testTgz = new File("./src/test/resources/org/esa/beam/dataio/smos/" + file);
        }
        return testTgz;
    }

    private static void assertCorrect_BWSD1C_Product(Product product) {
        assertNotNull(product);
        assertEquals(11, product.getNumBands());
        assertEquals(8192, product.getSceneRasterHeight());
        assertEquals(16384, product.getSceneRasterWidth());
        assertEquals("01-FEB-2010 13:42:56.000000", product.getStartTime().format());
        assertEquals("01-FEB-2010 14:00:57.000000", product.getEndTime().format());
    }
}
