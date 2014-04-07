package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.util.DiskCache;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class GridPointFormatExporterTest {

    private File targetDirectory;
    private GridPointFormatExporter gridPointFormatExporter;

    @Before
    public void setUp() {
        targetDirectory = new File("test_out");

        // need to move NetCDF cache dir to a directory that gets deleted  tb 2014-04-05
        DiskCache.setRootDirectory(targetDirectory.getAbsolutePath());
        DiskCache.setCachePolicy(true);

        gridPointFormatExporter = new GridPointFormatExporter();
    }

    @After
    public void tearDown() {
        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

    @Test
    public void testExportBWLF1C() throws IOException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final File outputFile = new File(targetDirectory, "BWLF1C.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertDimension("grid_point_count", 84045, targetFile);
            assertDimension("bt_data_count", 255, targetFile);

        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    @Test
    public void testExportOSUDP2() throws IOException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");
        final File outputFile = new File(targetDirectory, "OSUDP2.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertDimension("grid_point_count", 98564, targetFile);
            assertNoDimension("bt_data_count", targetFile);
        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    // @todo 1 tb/tb test for L1C files
    // Dimensions
    // - grid_point_count = unlimited
    // - snapshot_count = 4231
    // - bt_data_count = 300
    // - radiometric_accuracy_count = 2

    private static void assertNoDimension(String dimensionName, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();

        for (final Dimension dimension: dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                fail("Product contains dimension: '" + dimensionName + "' but shouldn't");
                return;
            }
        }
    }

    private static void assertDimension(String dimensionName, int dimensionLength, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();
        for (final Dimension dimension : dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                assertEquals(dimensionLength, dimension.getLength());
                return;
            }
        }
        fail("file does not contain dimension: " + dimensionName);
    }
}
