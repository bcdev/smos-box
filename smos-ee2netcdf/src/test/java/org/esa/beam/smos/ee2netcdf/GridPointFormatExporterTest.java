package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.dataio.smos.util.DateTimeUtils;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.util.DiskCache;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
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
    @Ignore // @todo 1 tb/tb enable again tb 2014-04-08
    public void testExportBWLF1C() throws IOException, ParseException, InvalidRangeException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final File outputFile = new File(targetDirectory, "BWLF1C.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertCorrectGlobalAttributes(targetFile, 84045);

            assertDimension("grid_point_count", 84045, targetFile);
            assertDimension("bt_data_count", 255, targetFile);
            assertNoDimension("radiometric_accuracy_count", targetFile);
            assertNoDimension("snapshot_count", targetFile);

            final Variable gridPointIdVariable = getVariable("grid_point_id", targetFile);
            final Array array = gridPointIdVariable.read(new int[]{346}, new int[]{2});
            assertEquals(4098190, array.getInt(0));
            assertEquals(4098191, array.getInt(1));


        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    private Variable getVariable(String variableName, NetcdfFile targetFile) {
        final List<Variable> variables = targetFile.getVariables();
        for (final Variable variable : variables) {
            if (variable.getFullName().equals(variableName)) {
                return variable;
            }
        }
        fail("Variable '" + variableName + "' not in file");
        return null;
    }

    @Test
    public void testExportSCLF1C() throws IOException, ParseException {
        final File file = TestHelper.getResourceFile("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip");
        final File outputFile = new File(targetDirectory, "SCLF1C.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            final int numGridPoints = 42;
            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);
            assertCorrectGlobalAttributes(targetFile, numGridPoints);

            assertDimension("grid_point_count", numGridPoints, targetFile);
            assertDimension("bt_data_count", 300, targetFile);
            assertDimension("radiometric_accuracy_count", 2, targetFile);
            assertDimension("snapshot_count", 172, targetFile);

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
    public void testExportOSUDP2() throws IOException, ParseException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");
        final File outputFile = new File(targetDirectory, "OSUDP2.nc");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);
            gridPointFormatExporter.write(product, outputFile);

            assertTrue(outputFile.isFile());
            targetFile = NetcdfFileOpener.open(outputFile);

            final int numGridPoints = 98564;
            assertCorrectGlobalAttributes(targetFile, numGridPoints);

            assertDimension("grid_point_count", numGridPoints, targetFile);
            assertNoDimension("bt_data_count", targetFile);
            assertNoDimension("radiometric_accuracy_count", targetFile);
            assertNoDimension("snapshot_count", targetFile);
        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    private void assertCorrectGlobalAttributes(NetcdfFile targetFile, int numGridPoints) throws ParseException {
        assertGlobalAttribute("Conventions", "CF-1.6", targetFile);
        assertGlobalAttribute("title", "TBD", targetFile);
        assertGlobalAttribute("institution", "TBD", targetFile);
        assertGlobalAttribute("contact", "TBD", targetFile);
        assertCreationDateWithinLast5Minutes(targetFile);
        assertGlobalAttribute("total_number_of_grid_points", Integer.toString(numGridPoints), targetFile);
    }

    private static void assertCreationDateWithinLast5Minutes(NetcdfFile targetFile) throws ParseException {
        final List<Attribute> globalAttributes = targetFile.getGlobalAttributes();

        for (final Attribute globalAttribute : globalAttributes) {
            if (globalAttribute.getFullName().equals("creation_date")) {
                final Date dateFromFile = DateTimeUtils.fromFixedHeaderFormat(globalAttribute.getStringValue());
                final Date now = new Date();
                assertTrue((now.getTime() - dateFromFile.getTime() < 300000));
            }

        }
    }

    // @todo 1 tb/tb test for L1C files
    // Dimensions
    // - radiometric_accuracy_count = 2
    // - bt_data_count = 300
    // -  snapshot_count= 4231
    // - grid_point_count = unlimited

    private static void assertGlobalAttribute(String attributeName, String attributeValue, NetcdfFile targetFile) {
        final List<Attribute> globalAttributes = targetFile.getGlobalAttributes();
        for (final Attribute globalAttribute : globalAttributes) {
            if (globalAttribute.getFullName().equals(attributeName)) {
                assertEquals(attributeValue, globalAttribute.getStringValue());
                return;
            }
        }
        fail("Global attribute: '" + attributeName + "' not present");
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

    private static void assertNoDimension(String dimensionName, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();

        for (final Dimension dimension : dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                fail("Product contains dimension: '" + dimensionName + "' but shouldn't");
                return;
            }
        }
    }
}
