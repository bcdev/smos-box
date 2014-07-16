package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.smos.AcceptanceTestRunner;
import org.esa.beam.smos.DateTimeUtils;
import org.esa.beam.util.StringUtils;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AcceptanceTestRunner.class)
public class GPToNetCDFExporterOpIntegrationTest {

    private final GPToNetCDFExporterOp.Spi spi;
    private final File targetDirectory;

    public GPToNetCDFExporterOpIntegrationTest() {
        spi = new GPToNetCDFExporterOp.Spi();
        targetDirectory = new File("test_out");
    }

    @Before
    public void setUp() {
        if (!targetDirectory.mkdirs()) {
            fail("Unable to create test directory");
        }

        // need to move NetCDF cache dir to a directory that gets deleted  tb 2014-07-04
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
    public void testConvert_BWSD1C() throws IOException, InvalidRangeException, ParseException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);

            GPF.createProduct(GPToNetCDFExporterOp.ALIAS,
                    createDefaultParameterMap(),
                    new Product[]{product});

            final File outputFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(outputFile.isFile());
            assertEquals(4342778, outputFile.length());

            targetFile = NetcdfFileOpener.open(outputFile);
            final ExportParameter exportParameter = new ExportParameter();
            assertCorrectGlobalAttributes(targetFile, 84045, exportParameter);
            assertGlobalAttribute("Fixed_Header:File_Name", "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1", targetFile);
            assertGlobalAttribute("Fixed_Header:Validity_Period:Validity_Start", "UTC=2011-10-26T14:32:06", targetFile);
            assertGlobalAttribute("Variable_Header:Main_Product_Header:Ref_Doc", "SO-TN-IDR-GS-0005", targetFile);
            assertGlobalAttribute("Variable_Header:Main_Product_Header:Orbit_Information:Phase", "+001", targetFile);

            assertDimension("n_grid_points", 84045, targetFile);
            assertDimension("n_bt_data", 4, targetFile);
            assertNoDimension("n_radiometric_accuracy", targetFile);
            assertNoDimension("n_snapshots", targetFile);

            assertGridPointIdVariable(targetFile, 346, new int[]{4098190, 4098191});

            final Variable latVariable = getVariableVerified("Grid_Point_Latitude", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "deg", latVariable);
            assertAttribute("_FillValue", Double.NaN, latVariable);
            assertNoAttribute("flag_masks", latVariable);
            assertNoAttribute("flag_values", latVariable);
            assertNoAttribute("flag_meanings", latVariable);
            assertNoAttribute("scale_factor", latVariable);
            assertNoAttribute("_Unsigned", latVariable);
            Array array = latVariable.read(new int[]{467}, new int[]{2});
            assertEquals(78.56900024, array.getFloat(0), 1e-8);
            assertEquals(78.6760025, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariableVerified("Grid_Point_Longitude", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "deg", lonVariable);
            assertAttribute("_FillValue", Double.NaN, lonVariable);
            assertNoAttribute("flag_masks", lonVariable);
            assertNoAttribute("flag_values", lonVariable);
            assertNoAttribute("flag_meanings", lonVariable);
            assertNoAttribute("scale_factor", lonVariable);
            assertNoAttribute("_Unsigned", lonVariable);
            array = lonVariable.read(new int[]{582}, new int[]{2});
            assertEquals(101.25, array.getFloat(0), 1e-8);
            assertEquals(100.994003295, array.getFloat(1), 1e-8);

            final Variable altitudeVariable = getVariableVerified("Grid_Point_Altitude", targetFile);
            assertEquals(DataType.FLOAT, altitudeVariable.getDataType());
            assertAttribute("units", "m", altitudeVariable);
            assertAttribute("_FillValue", Double.NaN, altitudeVariable);
            assertNoAttribute("flag_masks", altitudeVariable);
            assertNoAttribute("flag_values", altitudeVariable);
            assertNoAttribute("flag_meanings", altitudeVariable);
            assertNoAttribute("scale_factor", altitudeVariable);
            assertNoAttribute("_Unsigned", altitudeVariable);
            array = altitudeVariable.read(new int[]{619}, new int[]{2});
            assertEquals(-0.708, array.getFloat(0), 1e-8);
            assertEquals(0.0, array.getFloat(1), 1e-8);

            final Variable gridPointMaskVariable = getVariableVerified("Grid_Point_Mask", targetFile);
            assertEquals(DataType.BYTE, gridPointMaskVariable.getDataType());
            assertNoAttribute("units", gridPointMaskVariable);
            assertAttribute("_FillValue", Double.NaN, gridPointMaskVariable);
            assertNoAttribute("flag_masks", gridPointMaskVariable);
            assertNoAttribute("flag_values", gridPointMaskVariable);
            assertNoAttribute("flag_meanings", gridPointMaskVariable);
            assertNoAttribute("scale_factor", gridPointMaskVariable);
            assertAttribute("_Unsigned", "true", gridPointMaskVariable);
            array = gridPointMaskVariable.read(new int[]{743}, new int[]{2});
            assertEquals(-39, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(-39, array.getByte(1));

            final Variable btDataCountVariable = getVariableVerified("BT_Data_Counter", targetFile);
            assertEquals(DataType.BYTE, btDataCountVariable.getDataType());
            assertNoAttribute("units", btDataCountVariable);
            assertAttribute("_FillValue", Double.NaN, gridPointMaskVariable);
            assertNoAttribute("flag_masks", btDataCountVariable);
            assertNoAttribute("flag_values", btDataCountVariable);
            assertNoAttribute("flag_meanings", btDataCountVariable);
            assertNoAttribute("scale_factor", btDataCountVariable);
            assertAttribute("_Unsigned", "true", btDataCountVariable);
            array = btDataCountVariable.read(new int[]{833}, new int[]{2});
            assertEquals(4, array.getByte(0)); // @todo 2 tb/tb these should be unsigned values - resolve problem tb 2014-04-09
            assertEquals(4, array.getByte(1));

            final Variable flagsVariable = getVariableVerified("Flags", targetFile);
            assertEquals(DataType.SHORT, flagsVariable.getDataType());
            assertNoAttribute("units", flagsVariable);
            assertAttribute("_FillValue", 0.0, flagsVariable);
            assertAttribute("flag_masks", new short[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768}, flagsVariable);
            assertAttribute("flag_values", new short[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768}, flagsVariable);
            assertAttribute("flag_meanings", "POL_FLAG_1 POL_FLAG_2 SUN_FOV SUN_GLINT_FOV MOON_GLINT_FOV SINGLE_SNAPSHOT FTT SUN_POINT SUN_GLINT_AREA MOON_POINT AF_FOV EAF_FOV BORDER_FOV SUN_TAILS RFI_1 RFI_2", flagsVariable);
            assertNoAttribute("scale_factor", flagsVariable);
            assertAttribute("_Unsigned", "true", flagsVariable);
            array = flagsVariable.read(new int[]{945, 1}, new int[]{2, 1});
            assertEquals(1045, array.getShort(0));
            assertEquals(1045, array.getShort(1));

            final Variable btValueVariable = getVariableVerified("BT_Value", targetFile);
            assertEquals(DataType.FLOAT, btValueVariable.getDataType());
            assertAttribute("units", "K", btValueVariable);
            assertAttribute("_FillValue", -999.0, btValueVariable);
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

            final Variable radAccVariable = getVariableVerified("Radiometric_Accuracy_of_Pixel", targetFile);
            assertEquals(DataType.SHORT, radAccVariable.getDataType());
            assertAttribute("units", "K", radAccVariable);
            assertAttribute("_FillValue", 0.0, radAccVariable);
            assertNoAttribute("flag_masks", radAccVariable);
            assertNoAttribute("flag_values", radAccVariable);
            assertNoAttribute("flag_meanings", radAccVariable);
            assertAttribute("scale_factor", 1.52587890625E-5, radAccVariable);
            assertAttribute("scale_offset", 0.0, radAccVariable);
            assertAttribute("_Unsigned", "true", radAccVariable);
            array = radAccVariable.read(new int[]{1175, 0}, new int[]{2, 2});
            assertEquals(3547, array.getShort(0));
            assertEquals(3704, array.getShort(1));
            assertEquals(3552, array.getShort(2));
            assertEquals(3642, array.getShort(3));

            final Variable azimuthAngleVariable = getVariableVerified("Azimuth_Angle", targetFile);
            assertEquals(DataType.SHORT, azimuthAngleVariable.getDataType());
            assertAttribute("units", "deg", azimuthAngleVariable);
            assertAttribute("_FillValue", 0.0, azimuthAngleVariable);
            assertNoAttribute("flag_masks", azimuthAngleVariable);
            assertNoAttribute("flag_values", azimuthAngleVariable);
            assertNoAttribute("flag_meanings", azimuthAngleVariable);
            assertAttribute("scale_factor", 0.0054931640625, azimuthAngleVariable);
            assertAttribute("scale_offset", 0.0, azimuthAngleVariable);
            assertAttribute("_Unsigned", "true", azimuthAngleVariable);
            array = azimuthAngleVariable.read(new int[]{1261, 1}, new int[]{2, 2});
            assertEquals(8169, array.getShort(0));
            assertEquals(8170, array.getShort(1));
            assertEquals(8377, array.getShort(2));
            assertEquals(8376, array.getShort(3));

            final Variable footAxis1Variable = getVariableVerified("Footprint_Axis1", targetFile);
            assertEquals(DataType.SHORT, footAxis1Variable.getDataType());
            assertAttribute("units", "km", footAxis1Variable);
            assertAttribute("_FillValue", 0.0, footAxis1Variable);
            assertNoAttribute("flag_masks", footAxis1Variable);
            assertNoAttribute("flag_values", footAxis1Variable);
            assertNoAttribute("flag_meanings", footAxis1Variable);
            assertAttribute("scale_factor", 1.52587890625E-5, footAxis1Variable);
            assertAttribute("scale_offset", 0.0, footAxis1Variable);
            assertAttribute("_Unsigned", "true", footAxis1Variable);
            array = footAxis1Variable.read(new int[]{1394, 2}, new int[]{2, 2});
            assertEquals(18489, array.getShort(0));
            assertEquals(18489, array.getShort(1));
            assertEquals(18492, array.getShort(2));
            assertEquals(18492, array.getShort(3));

            final Variable footAxis2Variable = getVariableVerified("Footprint_Axis2", targetFile);
            assertEquals(DataType.SHORT, footAxis2Variable.getDataType());
            assertAttribute("units", "km", footAxis2Variable);
            assertAttribute("_FillValue", 0.0, footAxis2Variable);
            assertNoAttribute("flag_masks", footAxis2Variable);
            assertNoAttribute("flag_values", footAxis2Variable);
            assertNoAttribute("flag_meanings", footAxis2Variable);
            assertAttribute("scale_factor", 1.52587890625E-5, footAxis2Variable);
            assertAttribute("scale_offset", 0.0, footAxis2Variable);
            assertAttribute("_Unsigned", "true", footAxis2Variable);
            array = footAxis2Variable.read(new int[]{1417, 0}, new int[]{2, 2});
            assertEquals(13625, array.getShort(0));
            assertEquals(13631, array.getShort(1));
            assertEquals(13652, array.getShort(2));
            assertEquals(13658, array.getShort(3));
        } finally {
            if (product != null) {
                product.dispose();
            }
            if (targetFile != null) {
                targetFile.close();
            }
        }
    }

    @Test
    public void testConvert_BWSD1C_withBandSubset() throws IOException, InvalidRangeException, ParseException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> parameterMap = createDefaultParameterMap();
            parameterMap.put("outputBandNames", "Grid_Point_Latitude,Grid_Point_Longitude,BT_Value,Azimuth_Angle");
            GPF.createProduct(GPToNetCDFExporterOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File outputFile = new File(targetDirectory, "SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc");
            assertTrue(outputFile.isFile());
            assertEquals(2519783, outputFile.length());

            targetFile = NetcdfFileOpener.open(outputFile);
            getVariableVerified("Grid_Point_Latitude", targetFile);
            getVariableVerified("Grid_Point_Longitude", targetFile);
            getVariableVerified("BT_Value", targetFile);
            getVariableVerified("Azimuth_Angle", targetFile);

            final Variable gridPointAltitude = getVariable("Grid_Point_Altitude", targetFile);
            assertNull(gridPointAltitude);

            final Variable footprintAxis1 = getVariable("Footprint_Axis1", targetFile);
            assertNull(footprintAxis1);

        } finally {
            if (product != null) {
                product.dispose();
            }

            if (targetFile != null) {
                targetFile.close();
            }
        }
    }

    @Test
//    @Ignore
    public void testExportSCLF1C_withSourceProductPaths() throws IOException, ParseException, InvalidRangeException {
        final File file = TestHelper.getResourceFile("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> parameterMap = createDefaultParameterMap();
            parameterMap.put("sourceProductPaths", file.getParent() + File.separator + "*SCLF1C*");
            GPF.createProduct(GPToNetCDFExporterOp.ALIAS,
                    parameterMap);

            final File outputFile = new File(targetDirectory, "SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.nc");
            assertTrue(outputFile.isFile());
            assertEquals(376963, outputFile.length());

            final ExportParameter exportParameter = new ExportParameter();
            targetFile = NetcdfFileOpener.open(outputFile);
            final int numGridPoints = 42;
            assertCorrectGlobalAttributes(targetFile, numGridPoints, exportParameter);
            assertGlobalAttribute("Fixed_Header:File_Description", "Level 1C Full Polarization Land Science measurements product", targetFile);
            assertGlobalAttribute("Fixed_Header:Source:System", "DPGS", targetFile);
            assertGlobalAttribute("Variable_Header:Main_Product_Header:Acquisition_Station", "SVLD", targetFile);
            assertGlobalAttribute("Variable_Header:Main_Product_Header:Orbit_Information:Cycle", "+019", targetFile);
            assertGlobalAttribute("Variable_Header:Specific_Product_Header:List_of_Data_Sets:Data_Set_6:DS_Name", "LAND_SEA_MASK_FILE", targetFile);

            assertDimension("n_grid_points", numGridPoints, targetFile);
            assertDimension("n_bt_data", 300, targetFile);
            assertDimension("n_radiometric_accuracy", 2, targetFile);
            assertDimension("n_snapshots", 172, targetFile);

            //assertGridPointIdVariable(targetFile, 32, new int[]{6247647, 6248159});
        } finally {
            if (product != null) {
                product.dispose();
            }

            if (targetFile != null) {
                targetFile.close();
            }
        }
    }

    @Test
    public void testExportOSUDP2_withAdditionalMetadata() throws IOException, ParseException, InvalidRangeException {
        final File file = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> parameterMap = createDefaultParameterMap();
            parameterMap.put("institution", "BC");
            parameterMap.put("contact", "Tom");
            GPF.createProduct(GPToNetCDFExporterOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File outputFile = new File(targetDirectory, "SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc");
            assertTrue(outputFile.isFile());
            assertEquals(7245774, outputFile.length());

            targetFile = NetcdfFileOpener.open(outputFile);

            final int numGridPoints = 98564;
            final ExportParameter exportParameter = new ExportParameter();
            exportParameter.setInstitution("BC");
            exportParameter.setContact("Tom");
            assertCorrectGlobalAttributes(targetFile, numGridPoints, exportParameter);

            assertDimension("n_grid_points", numGridPoints, targetFile);
            assertNoDimension("n_bt_data", targetFile);
            assertNoDimension("n_radiometric_accuracy", targetFile);
            assertNoDimension("n_snapshots", targetFile);

            assertGridPointIdVariable(targetFile, 584, new int[]{7188459, 7188465});

            final Variable latVariable = getVariableVerified("Latitude", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "deg", latVariable);
            assertAttribute("_FillValue", -999.0, latVariable);
            assertNoAttribute("_Unsigned", latVariable);
            Array array = latVariable.read(new int[]{672}, new int[]{2});
            assertEquals(-76.871002197, array.getFloat(0), 1e-8);
            assertEquals(-76.870002747, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariableVerified("Longitude", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "deg", lonVariable);
            assertAttribute("_FillValue", -999.0, lonVariable);
            assertNoAttribute("_Unsigned", lonVariable);
            array = lonVariable.read(new int[]{718}, new int[]{2});
            assertEquals(168.957000732, array.getFloat(0), 1e-8);
            assertEquals(160.537002563, array.getFloat(1), 1e-8);

            final Variable equiv_ftprt_diam = getVariableVerified("Equiv_ftprt_diam", targetFile);
            assertEquals(DataType.FLOAT, equiv_ftprt_diam.getDataType());
            assertAttribute("units", "m", equiv_ftprt_diam);
            assertAttribute("_FillValue", -999.0, equiv_ftprt_diam);
            array = equiv_ftprt_diam.read(new int[]{819}, new int[]{2});
            assertEquals(-999.0, array.getFloat(0), 1e-8);
            assertEquals(-999.0, array.getFloat(1), 1e-8);

            final Variable mean_acq_time = getVariableVerified("Mean_acq_time", targetFile);
            assertEquals(DataType.FLOAT, mean_acq_time.getDataType());
            assertAttribute("units", "dd", mean_acq_time);
            assertAttribute("_FillValue", -999.0, mean_acq_time);
            array = mean_acq_time.read(new int[]{920}, new int[]{2});
            assertEquals(3625.015869140625, array.getFloat(0), 1e-8);
            assertEquals(3625.015869140625, array.getFloat(1), 1e-8);

            final Variable sss1 = getVariableVerified("SSS1", targetFile);
            assertEquals(DataType.FLOAT, sss1.getDataType());
            assertAttribute("units", "psu", sss1);
            assertAttribute("_FillValue", -999.0, sss1);
            array = sss1.read(new int[]{10021}, new int[]{2});
            assertEquals(69.771240234375, array.getFloat(0), 1e-8);
            assertEquals(-4.7435197830200195, array.getFloat(1), 1e-8);

            final Variable sigma_sss1 = getVariableVerified("Sigma_SSS1", targetFile);
            assertEquals(DataType.FLOAT, sigma_sss1.getDataType());
            assertAttribute("units", "psu", sigma_sss1);
            assertAttribute("_FillValue", -999.0, sigma_sss1);
            array = sigma_sss1.read(new int[]{10022}, new int[]{2});
            assertEquals(7.676535129547119, array.getFloat(0), 1e-8);
            assertEquals(66.9095687866211, array.getFloat(1), 1e-8);

        } finally {
            if (product != null) {
                product.dispose();
            }

            if (targetFile != null) {
                targetFile.close();
            }
        }
    }

    @Test
    public void testExportSMUDP2() throws IOException, ParseException, InvalidRangeException {
        final File file = TestHelper.getResourceFile("SM_OPEB_MIR_SMUDP2_20140413T185915_20140413T195227_551_026_1.zip");

        Product product = null;
        NetcdfFile targetFile = null;
        try {
            product = ProductIO.readProduct(file);

            final HashMap<String, Object> parameterMap = createDefaultParameterMap();
            GPF.createProduct(GPToNetCDFExporterOp.ALIAS,
                    parameterMap,
                    new Product[]{product});

            final File outputFile = new File(targetDirectory, "SM_OPEB_MIR_SMUDP2_20140413T185915_20140413T195227_551_026_1.nc");
            assertTrue(outputFile.isFile());
            assertEquals(676066, outputFile.length());

            targetFile = NetcdfFileOpener.open(outputFile);

            final int numGridPoints = 157;
            final ExportParameter exportParameter = new ExportParameter();
            assertCorrectGlobalAttributes(targetFile, numGridPoints, exportParameter);

            assertDimension("n_grid_points", numGridPoints, targetFile);
            assertNoDimension("n_bt_data", targetFile);
            assertNoDimension("n_radiometric_accuracy", targetFile);
            assertNoDimension("n_snapshots", targetFile);

            assertGridPointIdVariable(targetFile, 131, new int[]{2002428, 2002941});

            final Variable latVariable = getVariableVerified("Latitude", targetFile);
            assertEquals(DataType.FLOAT, latVariable.getDataType());
            assertAttribute("units", "deg", latVariable);
            assertAttribute("_FillValue", -999.0, latVariable);
            Array array = latVariable.read(new int[]{130}, new int[]{2});
            assertEquals(43.858001708984375, array.getFloat(0), 1e-8);
            assertEquals(43.86000061035156, array.getFloat(1), 1e-8);

            final Variable lonVariable = getVariableVerified("Longitude", targetFile);
            assertEquals(DataType.FLOAT, lonVariable.getDataType());
            assertAttribute("units", "deg", lonVariable);
            assertAttribute("_FillValue", -999.0, lonVariable);
            array = lonVariable.read(new int[]{129}, new int[]{2});
            assertEquals(-1.8890000581741333, array.getFloat(0), 1e-8);
            assertEquals(-1.6920000314712524, array.getFloat(1), 1e-8);

        } finally {
            if (product != null) {
                product.dispose();
            }

            if (targetFile != null) {
                targetFile.close();
            }
        }
    }

    private HashMap<String, Object> createDefaultParameterMap() {
        final HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("targetDirectory", targetDirectory);
        return parameterMap;
    }

    private void assertCorrectGlobalAttributes(NetcdfFile targetFile, int numGridPoints, ExportParameter exportParameter) throws ParseException {
        final String institution = exportParameter.getInstitution();
        if (StringUtils.isNotNullAndNotEmpty(institution)) {
            assertGlobalAttribute("institution", institution, targetFile);
        } else {
            assertNoGlobalAttribute("institution", targetFile);
        }

        final String contact = exportParameter.getContact();
        if (StringUtils.isNotNullAndNotEmpty(contact)) {
            assertGlobalAttribute("contact", contact, targetFile);
        } else {
            assertNoGlobalAttribute("contact", targetFile);
        }

        assertCreationDateWithinLast5Minutes(targetFile);
        assertGlobalAttribute("total_number_of_grid_points", Integer.toString(numGridPoints), targetFile);
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

    private static void assertNoGlobalAttribute(String attributeName, NetcdfFile targetFile) {
        final List<Attribute> globalAttributes = targetFile.getGlobalAttributes();
        for (final Attribute globalAttribute : globalAttributes) {
            if (globalAttribute.getFullName().equals(attributeName)) {
                fail("Global attribute: '" + attributeName + "' present but should not");
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

    private static void assertNoDimension(String dimensionName, NetcdfFile targetFile) {
        final List<Dimension> dimensions = targetFile.getDimensions();

        for (final Dimension dimension : dimensions) {
            if (dimension.getFullName().equals(dimensionName)) {
                fail("Product contains dimension: '" + dimensionName + "' but shouldn't");
                return;
            }
        }
    }

    private void assertGridPointIdVariable(NetcdfFile targetFile, int offset, int[] expected) throws IOException, InvalidRangeException {
        final Variable gridPointIdVariable = getVariableVerified("Grid_Point_ID", targetFile);
        assertEquals(DataType.INT, gridPointIdVariable.getDataType());
        assertAttribute("_Unsigned", "true", gridPointIdVariable);
        Array array = gridPointIdVariable.read(new int[]{offset}, new int[]{2});
        for (int i = 0; i < 2; i++) {
            assertEquals(expected[i], array.getInt(i));
        }
    }

    private Variable getVariableVerified(String variableName, NetcdfFile targetFile) {
        final Variable variable = getVariable(variableName, targetFile);
        if (variable == null) {
            fail("Variable '" + variableName + "' not in file");
        }
        return variable;
    }

    private Variable getVariable(String variableName, NetcdfFile targetFile) {
        final List<Variable> variables = targetFile.getVariables();
        for (final Variable variable : variables) {
            if (variable.getFullName().equals(variableName)) {
                return variable;
            }
        }
        return null;
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
}
