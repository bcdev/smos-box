package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Geometry;
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

    private final ConverterOp.Spi spi;
    private final File targetDirectory;

    public ConverterOpAcceptanceTest() {
        spi = new ConverterOp.Spi();
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

            GPF.createProduct(ConverterOp.ALIAS,
                    createDefaultParameterMap(),
                    new Product[]{product});

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            assertEquals(24079086, expectedOutputFile.length());
            // @todo 2 tb/tb more assertions 2013-03-25
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testConvert_BWSD1C_withRegion_andSourceProductPaths() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("sourceProductPaths", file.getParent() + File.separator + "*BWLF1C*");
            defaultParameterMap.put("region", "POLYGON((3 -70,5 -70,5 -71,3 -71,3 -70))");

            GPF.createProduct(ConverterOp.ALIAS,
                    defaultParameterMap);

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            assertEquals(255070, expectedOutputFile.length());

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
            GPF.createProduct(ConverterOp.ALIAS,
                    defaultParameterMap,
                    new Product[]{product});

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            assertEquals(1569057, expectedOutputFile.length());

            // @todo 2 tb/tb more assertions 2013-03-25
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testConvert_OSUDP2_withRegion_noIntersection() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("region", "POLYGON((100 30,105 30,105 32,100 32,100 30))");
            GPF.createProduct(ConverterOp.ALIAS,
                    defaultParameterMap,
                    new Product[]{product});

            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertFalse(expectedOutputFile.isFile());
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
            final Geometry polygon = ConverterOp.convertToPolygon(dataArea);

            final Rectangle dataBoundingRect = ConverterOp.getDataBoundingRect(product, polygon);
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

    @Test
    public void testConvertToPolygon() throws IOException {
        final File file = getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        Product product = null;
        try {
            product = ProductIO.readProduct(file);
            final SmosProductReader productReader = (SmosProductReader) product.getProductReader();
            final ExplorerFile explorerFile = productReader.getExplorerFile();
            final Area dataArea = DggFile.computeArea((DggFile) explorerFile);
            final Geometry polygon = ConverterOp.convertToPolygon(dataArea);
            assertEquals("MULTIPOLYGON (((-78.75 -90, -78.75 -78.75, -67.5 -78.75, -67.5 -67.5, 22.5 -67.5, 22.5 -56.25, 33.75 -56.25, 33.75 -45, 33.75 -33.75, 22.5 -33.75, 22.5 -22.5, 33.75 -22.5, 33.75 -11.25, 33.75 0, 33.75 11.25, 33.75 22.5, 33.75 33.75, 45 33.75, 45 45, 45 56.25, 45 67.5, 56.25 67.5, 56.25 78.75, 56.25 90, 123.75 90, 123.75 78.75, 146.25 78.75, 146.25 67.5, 90 67.5, 90 56.25, 78.75 56.25, 78.75 45, 67.5 45, 67.5 33.75, 67.5 22.5, 56.25 22.5, 56.25 11.25, 56.25 0, 56.25 -11.25, 56.25 -22.5, 56.25 -33.75, 45 -33.75, 45 -45, 45 -56.25, 45 -67.5, 45 -78.75, 33.75 -78.75, 33.75 -90, -78.75 -90)))",
                    polygon.toString());
        } finally {
            if (product != null) {
                product.dispose();
            }
        }

    }

    static File getResourceFile(String filename) {
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
