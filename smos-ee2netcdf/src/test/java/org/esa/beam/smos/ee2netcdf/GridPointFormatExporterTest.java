package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.DateTimeUtils;
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

            assertGridPointIdVariable(targetFile, 346, new int[]{4098190, 4098191});

            final Variable latVariable = getVariable("lat", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "degrees_north", latVariable);
            assertAttribute("_FillValue", -999.0, latVariable);
            assertAttribute("valid_min", -90.0, latVariable);
            assertAttribute("valid_max", 90.0, latVariable);
            assertAttribute("original_name", "Grid_Point_Latitude", latVariable);
            assertAttribute("standard_name", "latitude", latVariable);
            assertNoAttribute("flag_masks", latVariable);
            assertNoAttribute("flag_values", latVariable);
            assertNoAttribute("flag_meanings", latVariable);
            assertNoAttribute("scale_factor", latVariable);
            assertNoAttribute("_Unsigned", latVariable);
            Array array = latVariable.read(new int[]{467}, new int[]{2});
            assertEquals(78.56900024, array.getFloat(0), 1e-8);
            assertEquals(78.6760025, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariable("lon", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "degrees_east", lonVariable);
            assertAttribute("_FillValue", -999.0, lonVariable);
            assertAttribute("valid_min", -180.0, lonVariable);
            assertAttribute("valid_max", 180.0, lonVariable);
            assertAttribute("original_name", "Grid_Point_Longitude", lonVariable);
            assertAttribute("standard_name", "longitude", lonVariable);
            assertNoAttribute("flag_masks", lonVariable);
            assertNoAttribute("flag_values", lonVariable);
            assertNoAttribute("flag_meanings", lonVariable);
            assertNoAttribute("scale_factor", lonVariable);
            assertNoAttribute("_Unsigned", lonVariable);
            array = lonVariable.read(new int[]{582}, new int[]{2});
            assertEquals(101.25, array.getFloat(0), 1e-8);
            assertEquals(100.994003295, array.getFloat(1), 1e-8);

            final Variable altitudeVariable = getVariable("grid_point_altitude", targetFile);
            assertEquals(DataType.FLOAT, altitudeVariable.getDataType());
            assertAttribute("units", "m", altitudeVariable);
            assertAttribute("_FillValue", -999.0, altitudeVariable);
            assertNoAttribute("valid_min", altitudeVariable);
            assertNoAttribute("valid_max", altitudeVariable);
            assertNoAttribute("orignal_name", altitudeVariable);
            assertNoAttribute("standard_name", altitudeVariable);
            assertNoAttribute("flag_masks", altitudeVariable);
            assertNoAttribute("flag_values", altitudeVariable);
            assertNoAttribute("flag_meanings", altitudeVariable);
            assertNoAttribute("scale_factor", altitudeVariable);
            assertNoAttribute("_Unsigned", altitudeVariable);
            array = altitudeVariable.read(new int[]{619}, new int[]{2});
            assertEquals(-0.708, array.getFloat(0), 1e-8);
            assertEquals(0.0, array.getFloat(1), 1e-8);

            final Variable gridPointMaskVariable = getVariable("grid_point_mask", targetFile);
            assertEquals(DataType.BYTE, gridPointMaskVariable.getDataType());
            assertNoAttribute("units", gridPointMaskVariable);
            assertNoAttribute("_FillValue", gridPointMaskVariable);
            assertNoAttribute("valid_min", gridPointMaskVariable);
            assertNoAttribute("valid_max", gridPointMaskVariable);
            assertNoAttribute("orignal_name", gridPointMaskVariable);
            assertNoAttribute("standard_name", gridPointMaskVariable);
            assertNoAttribute("flag_masks", gridPointMaskVariable);
            assertNoAttribute("flag_values", gridPointMaskVariable);
            assertNoAttribute("flag_meanings", gridPointMaskVariable);
            assertNoAttribute("scale_factor", gridPointMaskVariable);
            assertAttribute("_Unsigned", "true", gridPointMaskVariable);
            array = gridPointMaskVariable.read(new int[]{743}, new int[]{2});
            assertEquals(-39, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(-39, array.getByte(1));

            final Variable btDataCountVariable = getVariable("bt_data_count", targetFile);
            assertEquals(DataType.BYTE, btDataCountVariable.getDataType());
            assertNoAttribute("units", btDataCountVariable);
            assertNoAttribute("_FillValue", btDataCountVariable);
            assertNoAttribute("valid_min", btDataCountVariable);
            assertNoAttribute("valid_max", btDataCountVariable);
            assertNoAttribute("orignal_name", btDataCountVariable);
            assertNoAttribute("standard_name", btDataCountVariable);
            assertNoAttribute("flag_masks", btDataCountVariable);
            assertNoAttribute("flag_values", btDataCountVariable);
            assertNoAttribute("flag_meanings", btDataCountVariable);
            assertNoAttribute("scale_factor", btDataCountVariable);
            assertAttribute("_Unsigned", "true", btDataCountVariable);
            array = btDataCountVariable.read(new int[]{833}, new int[]{2});
            assertEquals(4, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(4, array.getByte(1));

            final Variable flagsVariable = getVariable("flags", targetFile);
            assertEquals(DataType.SHORT, flagsVariable.getDataType());
            assertNoAttribute("units", flagsVariable);
            assertNoAttribute("_FillValue", flagsVariable);
            assertNoAttribute("valid_min", flagsVariable);
            assertNoAttribute("valid_max", flagsVariable);
            assertNoAttribute("orignal_name", flagsVariable);
            assertNoAttribute("standard_name", flagsVariable);
            assertAttribute("flag_masks", new short[]{3, 3, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768}, flagsVariable);
            assertAttribute("flag_values", new short[]{0, 1, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768}, flagsVariable);
            assertAttribute("flag_meanings", "pol_xx pol_yy sun_fov sun_glint_fov moon_glint_fov single_snapshot rfi_x sun_point sun_glint_area moon_point af_fov rfi_tails border_fov sun_tails rfi_y rfi_point_source", flagsVariable);
            assertNoAttribute("scale_factor", flagsVariable);
            assertAttribute("_Unsigned", "true", flagsVariable);
            array = flagsVariable.read(new int[]{945, 1}, new int[]{2, 1});
            assertEquals(1045, array.getShort(0));
            assertEquals(1045, array.getShort(1));

            final Variable btValueVariable = getVariable("bt_value", targetFile);
            assertEquals(DataType.FLOAT, btValueVariable.getDataType());
            assertAttribute("units", "K", btValueVariable);
            assertAttribute("_FillValue", -999.0, btValueVariable);
            assertNoAttribute("valid_min", btValueVariable);
            assertNoAttribute("valid_max", btValueVariable);
            assertNoAttribute("orignal_name", btValueVariable);
            assertNoAttribute("standard_name", btValueVariable);
            assertNoAttribute("flag_masks", btValueVariable);
            assertNoAttribute("flag_values", btValueVariable);
            assertNoAttribute("flag_meanings", btValueVariable);
            assertNoAttribute("scale_factor", btValueVariable);
            assertNoAttribute("_Unsigned", btValueVariable);
            array = btValueVariable.read(new int[]{1034, 2}, new int[]{2, 2});
            assertEquals(6.868230819702148, array.getFloat(0), 1e-8);
            assertEquals(0.9826292991638184, array.getFloat(1), 1e-8);
            assertEquals(6.454884052276611, array.getFloat(2), 1e-8);
            assertEquals(-0.10488655418157578, array.getFloat(3), 1e-8);

            final Variable radAccVariable = getVariable("pixel_radiometric_accuracy", targetFile);
            assertEquals(DataType.SHORT, radAccVariable.getDataType());
            assertAttribute("units", "K", radAccVariable);
            assertNoAttribute("_FillValue", radAccVariable);
            assertNoAttribute("valid_min", radAccVariable);
            assertNoAttribute("valid_max", radAccVariable);
            assertAttribute("original_name", "Radiometric_Accuracy_of_Pixel", radAccVariable);
            assertNoAttribute("standard_name", radAccVariable);
            assertNoAttribute("flag_masks", radAccVariable);
            assertNoAttribute("flag_values", radAccVariable);
            assertNoAttribute("flag_meanings", radAccVariable);
            assertAttribute("scale_factor", 0.000762939453125, radAccVariable);
            assertAttribute("_Unsigned", "true", radAccVariable);
            array = radAccVariable.read(new int[]{1175, 0}, new int[]{2, 2});
            assertEquals(3547, array.getShort(0));
            assertEquals(3704, array.getShort(1));
            assertEquals(3552, array.getShort(2));
            assertEquals(3642, array.getShort(3));

            final Variable azimuthAngleVariable = getVariable("azimuth_angle", targetFile);
            assertEquals(DataType.SHORT, azimuthAngleVariable.getDataType());
            assertAttribute("units", "degree", azimuthAngleVariable);
            assertNoAttribute("_FillValue", azimuthAngleVariable);
            assertNoAttribute("valid_min", azimuthAngleVariable);
            assertNoAttribute("valid_max", azimuthAngleVariable);
            assertNoAttribute("orignal_name", azimuthAngleVariable);
            assertNoAttribute("standard_name", azimuthAngleVariable);
            assertNoAttribute("flag_masks", azimuthAngleVariable);
            assertNoAttribute("flag_values", azimuthAngleVariable);
            assertNoAttribute("flag_meanings", azimuthAngleVariable);
            assertAttribute("scale_factor", 0.0054931640625, azimuthAngleVariable);
            assertAttribute("_Unsigned", "true", azimuthAngleVariable);
            array = azimuthAngleVariable.read(new int[]{1261, 1}, new int[]{2, 2});
            assertEquals(8169, array.getShort(0));
            assertEquals(8170, array.getShort(1));
            assertEquals(8377, array.getShort(2));
            assertEquals(8376, array.getShort(3));

            final Variable footAxis1Variable = getVariable("footprint_axis_1", targetFile);
            assertEquals(DataType.SHORT, footAxis1Variable.getDataType());
            assertAttribute("units", "km", footAxis1Variable);
            assertNoAttribute("_FillValue", footAxis1Variable);
            assertNoAttribute("valid_min", footAxis1Variable);
            assertNoAttribute("valid_max", footAxis1Variable);
            assertNoAttribute("orignal_name", footAxis1Variable);
            assertNoAttribute("standard_name", footAxis1Variable);
            assertNoAttribute("flag_masks", footAxis1Variable);
            assertNoAttribute("flag_values", footAxis1Variable);
            assertNoAttribute("flag_meanings", footAxis1Variable);
            assertAttribute("scale_factor", 0.00152587890625, footAxis1Variable);
            assertAttribute("_Unsigned", "true", footAxis1Variable);
            array = footAxis1Variable.read(new int[]{1394, 2}, new int[]{2, 2});
            assertEquals(18489, array.getShort(0));
            assertEquals(18489, array.getShort(1));
            assertEquals(18492, array.getShort(2));
            assertEquals(18492, array.getShort(3));

            final Variable footAxis2Variable = getVariable("footprint_axis_2", targetFile);
            assertEquals(DataType.SHORT, footAxis2Variable.getDataType());
            assertAttribute("units", "km", footAxis2Variable);
            assertNoAttribute("_FillValue", footAxis2Variable);
            assertNoAttribute("valid_min", footAxis2Variable);
            assertNoAttribute("valid_max", footAxis2Variable);
            assertNoAttribute("orignal_name", footAxis2Variable);
            assertNoAttribute("standard_name", footAxis2Variable);
            assertNoAttribute("flag_masks", footAxis2Variable);
            assertNoAttribute("flag_values", footAxis2Variable);
            assertNoAttribute("flag_meanings", footAxis2Variable);
            assertAttribute("scale_factor", 0.00152587890625, footAxis2Variable);
            assertAttribute("_Unsigned", "true", footAxis2Variable);
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
    public void testExportSCLF1C() throws IOException, ParseException, InvalidRangeException {
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

            assertGridPointIdVariable(targetFile, 32, new int[]{6247647, 6248159});

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
    public void testExportOSUDP2() throws IOException, ParseException, InvalidRangeException {
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

            assertGridPointIdVariable(targetFile, 584, new int[]{7188459, 7188465});

            final Variable latVariable = getVariable("lat", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "degrees_north", latVariable);
            assertAttribute("_FillValue", -999.0, latVariable);
            assertAttribute("valid_min", -90.0, latVariable);
            assertAttribute("valid_max", 90.0, latVariable);
            assertAttribute("original_name", "Latitude", latVariable);
            assertAttribute("standard_name", "latitude", latVariable);
            assertNoAttribute("_Unsigned", latVariable);
            Array array = latVariable.read(new int[]{672}, new int[]{2});
            assertEquals(-76.871002197, array.getFloat(0), 1e-8);
            assertEquals(-76.870002747, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariable("lon", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "degrees_east", lonVariable);
            assertAttribute("_FillValue", -999.0, lonVariable);
            assertAttribute("valid_min", -180.0, lonVariable);
            assertAttribute("valid_max", 180.0, lonVariable);
            assertAttribute("original_name", "Longitude", lonVariable);
            assertAttribute("standard_name", "longitude", lonVariable);
            assertNoAttribute("_Unsigned", lonVariable);
            array = lonVariable.read(new int[]{718}, new int[]{2});
            assertEquals(168.957000732, array.getFloat(0), 1e-8);
            assertEquals(160.537002563, array.getFloat(1), 1e-8);

            final Variable ftprtDiamVariable = getVariable("equiv_ftprt_diam", targetFile);
            assertEquals(DataType.FLOAT, ftprtDiamVariable.getDataType());
            assertAttribute("units", "m", ftprtDiamVariable);
            assertAttribute("_FillValue", -999.0, ftprtDiamVariable);
            array = ftprtDiamVariable.read(new int[]{837}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable acqTimeVariable = getVariable("mean_acq_time", targetFile);
            assertEquals(DataType.FLOAT, acqTimeVariable.getDataType());
            assertAttribute("units", "dd", acqTimeVariable);
            assertAttribute("_FillValue", -999.0, acqTimeVariable);
            array = acqTimeVariable.read(new int[]{923}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sss1Variable = getVariable("sss1", targetFile);
            assertEquals(DataType.FLOAT, sss1Variable.getDataType());
            assertAttribute("units", "psu", sss1Variable);
            assertAttribute("_FillValue", -999.0, sss1Variable);
            array = sss1Variable.read(new int[]{1062}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sigmaSss1Variable = getVariable("sigma_sss1", targetFile);
            assertEquals(DataType.FLOAT, sigmaSss1Variable.getDataType());
            assertAttribute("units", "psu", sigmaSss1Variable);
            assertAttribute("_FillValue", -999.0, sigmaSss1Variable);
            array = sigmaSss1Variable.read(new int[]{1173}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sss2Variable = getVariable("sss2", targetFile);
            assertEquals(DataType.FLOAT, sss2Variable.getDataType());
            assertAttribute("units", "psu", sss2Variable);
            assertAttribute("_FillValue", -999.0, sss2Variable);
            array = sss2Variable.read(new int[]{1216}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sigmaSss2Variable = getVariable("sigma_sss2", targetFile);
            assertEquals(DataType.FLOAT, sigmaSss2Variable.getDataType());
            assertAttribute("units", "psu", sigmaSss2Variable);
            assertAttribute("_FillValue", -999.0, sigmaSss2Variable);
            array = sigmaSss2Variable.read(new int[]{1382}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sss3Variable = getVariable("sss3", targetFile);
            assertEquals(DataType.FLOAT, sss3Variable.getDataType());
            assertAttribute("units", "psu", sss3Variable);
            assertAttribute("_FillValue", -999.0, sss3Variable);
            array = sss3Variable.read(new int[]{1428}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sigmaSss3Variable = getVariable("sigma_sss3", targetFile);
            assertEquals(DataType.FLOAT, sigmaSss3Variable.getDataType());
            assertAttribute("units", "psu", sigmaSss3Variable);
            assertAttribute("_FillValue", -999.0, sigmaSss3Variable);
            array = sigmaSss3Variable.read(new int[]{1527}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable aCardVariable = getVariable("a_card", targetFile);
            assertEquals(DataType.FLOAT, aCardVariable.getDataType());
            assertAttribute("_FillValue", -999.0, aCardVariable);
            array = aCardVariable.read(new int[]{1694}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable sigmaACardVariable = getVariable("sigma_a_card", targetFile);
            assertEquals(DataType.FLOAT, sigmaACardVariable.getDataType());
            assertAttribute("_FillValue", -999.0, sigmaACardVariable);
            array = sigmaACardVariable.read(new int[]{1762}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

        } finally {
            if (targetFile != null) {
                targetFile.close();
            }
            if (product != null) {
                product.dispose();
            }
        }
    }

//    @Test
//    public void testExportSMUDP2() throws IOException {
//        final File file = TestHelper.getResourceFile("SM_OPEB_MIR_SMUDP2_20140413T185915_20140413T195227_551_026_1.zip");
//        final File outputFile = new File(targetDirectory, "SMUDP2.nc");
//
//        Product product = null;
//        NetcdfFile targetFile = null;
//        try {
//            product = ProductIO.readProduct(file);
//            gridPointFormatExporter.write(product, outputFile);
//        } finally {
//            if (targetFile != null) {
//                targetFile.close();
//            }
//            if (product != null) {
//                product.dispose();
//            }
//        }
//    }

    private void assertGridPointIdVariable(NetcdfFile targetFile, int offset, int[] expected) throws IOException, InvalidRangeException {
        final Variable gridPointIdVariable = getVariable("grid_point_id", targetFile);
        assertEquals(DataType.INT, gridPointIdVariable.getDataType());
        assertAttribute("_Unsigned", "true", gridPointIdVariable);
        Array array = gridPointIdVariable.read(new int[]{offset}, new int[]{2});
        for (int i = 0; i < 2; i++) {
            assertEquals(expected[i], array.getInt(i));
        }
    }

    private void assertAttribute(String attributeName, String attributeValue, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
            if (attribute.getFullName().equals(attributeName)) {
                assertEquals(attributeValue, attribute.getStringValue());
                return;
            }
        }
        fail("attribute '" + attributeName + "' is missing at variable '" + variable.getFullName() + "'");
    }

    private void assertAttribute(String attributeName, short[] attributeValue, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
            if (attribute.getFullName().equals(attributeName)) {
                final Array values = attribute.getValues();
                assertArrayEquals(attributeValue, (short[]) values.get1DJavaArray(Short.class));
                return;
            }
        }
        fail("attribute '" + attributeName + "' is missing at variable '" + variable.getFullName() + "'");
    }

    private void assertAttribute(String attributeName, double attributeValue, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
            if (attribute.getFullName().equals(attributeName)) {
                assertEquals(attributeValue, attribute.getNumericValue().doubleValue(), 1e-8);
                return;
            }
        }
        fail("attribute '" + attributeName + "' is missing at variable '" + variable.getFullName() + "'");
    }

    private void assertNoAttribute(String attributeName, Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
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
