package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.smos.DggFile;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.smos.AcceptanceTestRunner;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.*;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(AcceptanceTestRunner.class)
public class ConverterOpAcceptanceTest {

    private final ConverterOpSpi spi;
    private final File targetDirectory;

    public ConverterOpAcceptanceTest() {
        spi = new ConverterOpSpi();
        targetDirectory = new File("test_out");
    }

    @Before
    public void setUp() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(spi);
    }

    @After
    public void tearDown() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(spi);

        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

    @Test
    public void testConvert_BWSD1C() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            GPF.createProduct("SmosEE2NetCDF",
                    createDefaultParameterMap(),
                    new Product[]{product});

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            // @todo 2 tb/tb more assertions 2013-03-25
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testConvert_OSUDP2_withRegion() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("region", "POLYGON((70 -9,85 -9,85 -12,70 -12,70 -9))");
            GPF.createProduct("SmosEE2NetCDF",
                    defaultParameterMap,
                    new Product[]{product});

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            // @todo 2 tb/tb more assertions 2013-03-25
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testGetDataBoundingRect() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);
            final SmosProductReader productReader = (SmosProductReader) product.getProductReader();
            final ExplorerFile explorerFile = productReader.getExplorerFile();
            final Area dataArea = DggFile.computeArea((DggFile) explorerFile);

            final Rectangle dataBoundingRect = ConverterOp.getDataBoundingRect(product, dataArea);
            System.out.println("dataBoundingRect = " + dataBoundingRect);
            assertNotNull(dataBoundingRect);
            assertEquals(4608, dataBoundingRect.x);
            assertEquals(0, dataBoundingRect.y);
            assertEquals(10240, dataBoundingRect.width);
            assertEquals(8192, dataBoundingRect.height);
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    private File getResourceFile(String filename) {
        File testFile = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/" + filename);
        if (!testFile.exists()) {
            testFile = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/" + filename);
        }
        return testFile;
    }

    private HashMap<String, Object> createDefaultParameterMap() {
        final HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("targetDirectory", targetDirectory);
        return parameterMap;
    }
}
