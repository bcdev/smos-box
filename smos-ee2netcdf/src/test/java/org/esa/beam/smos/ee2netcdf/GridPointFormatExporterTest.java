package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.dataio.smos.util.DateTimeUtils;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
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

            assertDimension("n_grid_points", 84045, targetFile);
            assertDimension("n_bt_data", 4, targetFile);
            assertNoDimension("n_radiometric_accuracy", targetFile);
            assertNoDimension("n_snapshots", targetFile);

            final Variable gridPointIdVariable = getVariable("grid_point_id", targetFile);
            assertEquals(DataType.INT, gridPointIdVariable.getDataType());
            assertNoAttribute("units", gridPointIdVariable);
            Array array = gridPointIdVariable.read(new int[]{346}, new int[]{2});
            assertEquals(4098190, array.getInt(0));
            assertEquals(4098191, array.getInt(1));

            final Variable latVariable = getVariable("lat", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "degrees_north", latVariable);
            array = latVariable.read(new int[]{467}, new int[]{2});
            assertEquals(78.56900024, array.getFloat(0), 1e-8);
            assertEquals(78.6760025, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariable("lon", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "degrees_north", lonVariable);
            array = lonVariable.read(new int[]{582}, new int[]{2});
            assertEquals(101.25, array.getFloat(0), 1e-8);
            assertEquals(100.994003295, array.getFloat(1), 1e-8);

            final Variable altitudeVariable = getVariable("grid_point_altitude", targetFile);
            assertEquals(DataType.FLOAT, altitudeVariable.getDataType());
            assertAttribute("units", "m", altitudeVariable);
            array = altitudeVariable.read(new int[]{619}, new int[]{2});
            assertEquals(-0.708, array.getFloat(0), 1e-8);
            assertEquals(0.0, array.getFloat(1), 1e-8);

            final Variable gridPointMaskVariable = getVariable("grid_point_mask", targetFile);
            assertEquals(DataType.BYTE, gridPointMaskVariable.getDataType());
            assertNoAttribute("units", gridPointMaskVariable);
            array = gridPointMaskVariable.read(new int[]{743}, new int[]{2});
            assertEquals(-39, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(-39, array.getByte(1));

            final Variable btDataCountVariable = getVariable("bt_data_count", targetFile);
            assertEquals(DataType.BYTE, btDataCountVariable.getDataType());
            assertNoAttribute("units", btDataCountVariable);
            array = btDataCountVariable.read(new int[]{833}, new int[]{2});
            assertEquals(4, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(4, array.getByte(1));

            final Variable flagsVariable = getVariable("flags", targetFile);
            assertEquals(DataType.SHORT, flagsVariable.getDataType());
            assertNoAttribute("units", flagsVariable);
            array = flagsVariable.read(new int[]{945, 1}, new int[]{2, 1});
            assertEquals(1045, array.getShort(0));
            assertEquals(1045, array.getShort(1));

            final Variable btValueVariable = getVariable("bt_value", targetFile);
            assertEquals(DataType.FLOAT, btValueVariable.getDataType());
            assertAttribute("units", "K", btValueVariable);
            array = btValueVariable.read(new int[]{1034, 2}, new int[]{2, 2});
            assertEquals(6.868230819702148, array.getFloat(0), 1e-8);
            assertEquals(0.9826292991638184, array.getFloat(1), 1e-8);
            assertEquals(6.454884052276611, array.getFloat(2), 1e-8);
            assertEquals(-0.10488655418157578, array.getFloat(3), 1e-8);

            final Variable radAccVariable = getVariable("pixel_radiometric_accuracy", targetFile);
            assertEquals(DataType.SHORT, radAccVariable.getDataType());
            assertAttribute("units", "K", radAccVariable);
            array = radAccVariable.read(new int[]{1175, 0}, new int[]{2, 2});
            assertEquals(3547, array.getShort(0));
            assertEquals(3704, array.getShort(1));
            assertEquals(3552, array.getShort(2));
            assertEquals(3642, array.getShort(3));

            final Variable azimuthAngleVariable = getVariable("azimuth_angle", targetFile);
            assertEquals(DataType.SHORT, azimuthAngleVariable.getDataType());
            assertAttribute("units", "degree", azimuthAngleVariable);
            array = azimuthAngleVariable.read(new int[]{1261, 1}, new int[]{2, 2});
            assertEquals(8169, array.getShort(0));
            assertEquals(8170, array.getShort(1));
            assertEquals(8377, array.getShort(2));
            assertEquals(8376, array.getShort(3));

            final Variable footAxis1Variable = getVariable("footprint_axis_1", targetFile);
            assertEquals(DataType.SHORT, footAxis1Variable.getDataType());
            assertAttribute("units", "km", footAxis1Variable);
            array = footAxis1Variable.read(new int[]{1394, 2}, new int[]{2, 2});
            assertEquals(18489, array.getShort(0));
            assertEquals(18489, array.getShort(1));
            assertEquals(18492, array.getShort(2));
            assertEquals(18492, array.getShort(3));

            final Variable footAxis2Variable = getVariable("footprint_axis_2", targetFile);
            assertEquals(DataType.SHORT, footAxis2Variable.getDataType());
            assertAttribute("units", "km", footAxis2Variable);
            array = footAxis2Variable.read(new int[]{1417, 0}, new int[]{2, 2});
            assertEquals(13625, array.getShort(0));
            assertEquals(13631, array.getShort(1));
            assertEquals(13652, array.getShort(2));
            assertEquals(13658, array.getShort(3));
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

            assertDimension("n_grid_points", numGridPoints, targetFile);
            assertDimension("n_bt_data", 300, targetFile);
            assertDimension("n_radiometric_accuracy", 2, targetFile);
            assertDimension("n_snapshots", 172, targetFile);

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

            assertDimension("n_grid_points", numGridPoints, targetFile);
            assertNoDimension("n_bt_data", targetFile);
            assertNoDimension("n_radiometric_accuracy", targetFile);
            assertNoDimension("n_snapshots", targetFile);
        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

    private void assertAttribute(String attributeName, String attributeValue, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for(final Attribute attribute: attributes) {
            if (attribute.getFullName().equals(attributeName)) {
                assertEquals(attributeValue, attribute.getStringValue());
                return;
            }
        }
        fail("attribute '" + attributeName + "' is missing at variable '" + variable.getFullName() + "'");
    }

    private void assertNoAttribute(String attributeName, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for(final Attribute attribute: attributes) {
            if (attribute.getFullName().equals(attributeName)) {
                fail("attribute '" + attributeName + "' is present at variable '" + variable.getFullName() + "' but should not");
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
