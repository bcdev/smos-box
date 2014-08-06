package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.dataio.smos.DggFile;
import org.esa.beam.dataio.smos.DggUtils;
import org.esa.beam.dataio.smos.ProductFile;
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
import ucar.nc2.util.DiskCache;

import java.awt.*;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(AcceptanceTestRunner.class)
public class EEToNetCDFExporterOpIntegrationTest {

    private final EEToNetCDFExporterOp.Spi spi;
    private final File targetDirectory;

    public EEToNetCDFExporterOpIntegrationTest() {
        spi = new EEToNetCDFExporterOp.Spi();
        targetDirectory = new File("test_out");
    }

    @Before
    public void setUp() {
        // need to move NetCDF cache dir to a directory that gets deleted  tb 2013-09-04
        DiskCache.setRootDirectory(targetDirectory.getAbsolutePath());
        DiskCache.setCachePolicy(true);

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
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            GPF.createProduct(EEToNetCDFExporterOp.ALIAS,
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
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("sourceProductPaths", file.getParent() + File.separator + "*BWLF1C*");
            defaultParameterMap.put("region", "POLYGON((3 -70,5 -70,5 -71,3 -71,3 -70))");

            GPF.createProduct(EEToNetCDFExporterOp.ALIAS,
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
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        Product product = null;
        Product targetProduct = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("region", "POLYGON((70 -9,85 -9,85 -12,70 -12,70 -9))");
            GPF.createProduct(EEToNetCDFExporterOp.ALIAS,
                    defaultParameterMap,
                    new Product[]{product});

            assertTrue(targetDirectory.isDirectory());
            final File expectedOutputFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertTrue(expectedOutputFile.isFile());
            assertEquals(1569057, expectedOutputFile.length());

            // @todo 2 tb/tb more assertions 2013-03-25
            targetProduct = ProductIO.readProduct(expectedOutputFile);
            assertNotNull(targetProduct);
            assertEquals(684, targetProduct.getSceneRasterWidth());
            assertEquals(138, targetProduct.getSceneRasterHeight());

        } finally {
            if (product != null) {
                product.dispose();
            }
            if (targetProduct != null) {
                targetProduct.dispose();
            }
        }
    }

    @Test
    public void testConvert_OSUDP2_withRegion_noIntersection() throws IOException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> defaultParameterMap = createDefaultParameterMap();
            defaultParameterMap.put("region", "POLYGON((100 30,105 30,105 32,100 32,100 30))");
            GPF.createProduct(EEToNetCDFExporterOp.ALIAS,
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
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        try {
            product = ProductIO.readProduct(file);
            final SmosProductReader productReader = (SmosProductReader) product.getProductReader();
            final ProductFile productFile = productReader.getProductFile();
            assertTrue(productFile instanceof DggFile);
            final Area dataArea = DggUtils.computeArea(((DggFile) productFile).getGridPointList());
            final Geometry polygon = EEToNetCDFExporterOp.convertToPolygon(dataArea);

            final Rectangle dataBoundingRect = EEToNetCDFExporterOp.getDataBoundingRect(product, polygon);
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
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        Product product = null;
        try {
            product = ProductIO.readProduct(file);
            final SmosProductReader productReader = (SmosProductReader) product.getProductReader();
            final ProductFile productFile = productReader.getProductFile();
            assertTrue(productFile instanceof DggFile);
            final Area dataArea = DggUtils.computeArea(((DggFile) productFile).getGridPointList());
            final Geometry polygon = EEToNetCDFExporterOp.convertToPolygon(dataArea);
            assertEquals("MULTIPOLYGON (((-78.75 -90, -78.75 -78.75, -67.5 -78.75, -67.5 -67.5, 22.5 -67.5, 22.5 -56.25, 33.75 -56.25, 33.75 -45, 33.75 -33.75, 22.5 -33.75, 22.5 -22.5, 33.75 -22.5, 33.75 -11.25, 33.75 0, 33.75 11.25, 33.75 22.5, 33.75 33.75, 45 33.75, 45 45, 45 56.25, 45 67.5, 56.25 67.5, 56.25 78.75, 56.25 90, 123.75 90, 123.75 78.75, 146.25 78.75, 146.25 67.5, 90 67.5, 90 56.25, 78.75 56.25, 78.75 45, 67.5 45, 67.5 33.75, 67.5 22.5, 56.25 22.5, 56.25 11.25, 56.25 0, 56.25 -11.25, 56.25 -22.5, 56.25 -33.75, 45 -33.75, 45 -45, 45 -56.25, 45 -67.5, 45 -78.75, 33.75 -78.75, 33.75 -90, -78.75 -90)))",
                    polygon.toString());
        } finally {
            if (product != null) {
                product.dispose();
            }
        }

    }

    private HashMap<String, Object> createDefaultParameterMap() {
        final HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("targetDirectory", targetDirectory);
        return parameterMap;
    }
}
