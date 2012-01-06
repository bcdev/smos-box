package org.esa.beam.dataio.smos;

import junit.framework.TestCase;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class SmosProductReaderAcceptanceTest extends TestCase {

    private SmosProductReader reader;

    public void testRead_BWSD1C_directory() throws IOException {
        final File file = getTestFileOrDirectory("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1/SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.HDR");
        assertTrue(file.isFile());

        final Product product = reader.readProductNodes(file, null);
        assertCorrect_BWSD1C_Product(product);
    }

    public void testRead_BWSD1C_plainZip() throws IOException {
        final File file = getTestFileOrDirectory("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.zip");
        assertTrue(file.isFile());

        final Product product = reader.readProductNodes(file, null);
        assertCorrect_BWSD1C_Product(product);
    }

    public void testRead_BWSD1C_zipWithDirectory() throws IOException {
        final File file = getTestFileOrDirectory("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1_with_dir.zip");
        assertTrue(file.isFile());

        final Product product = reader.readProductNodes(file, null);
        assertCorrect_BWSD1C_Product(product);
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void setUp() {
        reader = new SmosProductReader(new SmosProductReaderPlugIn());
    }

    private static File getTestFileOrDirectory(String file) {
        File testTgz = new File("./smos-reader/src/test/resources/org/esa/beam/dataio/smos/" + file);
        if (!testTgz.exists()) {
            testTgz = new File("./src/test/resources/org/esa/beam/dataio/smos/" + file);
        }
        return testTgz;
    }

    private void assertCorrect_BWSD1C_Product(Product product) {
        assertNotNull(product);
        assertEquals(11, product.getNumBands());
        assertEquals(8192, product.getSceneRasterHeight());
        assertEquals(16384, product.getSceneRasterWidth());
        assertEquals("01-FEB-2010 13:42:56.000000", product.getStartTime().format());
        assertEquals("01-FEB-2010 14:00:57.000000", product.getEndTime().format());
    }


}
