/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.dataio.smos.dddb;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class DddbTest {

    private static final String DBL_SM_XXXX_AUX_ECMWF_0200 = "DBL_SM_XXXX_AUX_ECMWF__0200";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0200 = "DBL_SM_XXXX_MIR_SMDAP2_0200";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0201 = "DBL_SM_XXXX_MIR_SMDAP2_0201";
    private static final String DBL_SM_XXXX_MIR_SMDAP2_0300 = "DBL_SM_XXXX_MIR_SMDAP2_0300";
    private static final String DBL_SM_XXXX_MIR_OSDAP2_0200 = "DBL_SM_XXXX_MIR_OSDAP2_0200";
    private static final String DBL_SM_XXXX_MIR_OSDAP2_0300 = "DBL_SM_XXXX_MIR_OSDAP2_0300";
    private static final String DBL_SM_XXXX_MIR_OSUDP2_0300 = "DBL_SM_XXXX_MIR_OSUDP2_0300";
    private static final String DBL_SM_XXXX_MIR_SMUDP2_0200 = "DBL_SM_XXXX_MIR_SMUDP2_0200";
    private Dddb dddb;

    @Before
    public void setUp() {
        dddb = Dddb.getInstance();
    }

    @Test
    public void testGetBandDescriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_ECMWF_0200);
        assertEquals(57, descriptors.asList().size());

        final BandDescriptor descriptor = descriptors.getMember("RR");
        assertNotNull(descriptor);

        assertEquals("RR", descriptor.getBandName());
        assertEquals("Rain_Rate", descriptor.getMemberName());
        assertTrue(descriptor.hasTypicalMin());
        assertTrue(descriptor.hasTypicalMax());
        assertFalse(descriptor.isCyclic());
        assertTrue(descriptor.hasFillValue());
        assertFalse(descriptor.getValidPixelExpression().isEmpty());
        assertEquals("RR.raw != -99999.0 && RR.raw != -99998.0", descriptor.getValidPixelExpression());
        assertFalse(descriptor.getUnit().isEmpty());
        assertFalse(descriptor.getDescription().isEmpty());
        assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertEquals("m 3h-1", descriptor.getUnit());
    }

    @Test
    public void testGetBandDescriptors_BUFR() throws Exception {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors("BUFR");
        assertEquals(27, descriptors.asList().size());

        final BandDescriptor flagsBandDescriptor = descriptors.getMember("Flags");
        assertNotNull(flagsBandDescriptor);
        assertEquals("Flags", flagsBandDescriptor.getFlagCodingName());
        assertNotNull("Flags", flagsBandDescriptor.getFlagDescriptors());
    }

    @Test
    public void testGetFlagDescriptors_BUFR() throws Exception {
        final Family<FlagDescriptor> descriptors = dddb.getFlagDescriptors("BUFR_flags");
        assertEquals(13, descriptors.asList().size());
    }

    @Test
    public void testGetFlagDescriptors() {
        final Family<FlagDescriptor> descriptors = dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF_0200 + "_flags1");
        assertEquals(21, descriptors.asList().size());

        FlagDescriptor descriptor;
        descriptor = descriptors.getMember("RR_FLAG");
        assertNotNull(descriptor);

        assertEquals("RR_FLAG", descriptor.getFlagName());
        assertEquals(0x00000080, descriptor.getMask());
        assertNull(descriptor.getColor());
        assertEquals(0.5, descriptor.getTransparency(), 0.0);
        assertFalse(descriptor.getDescription().isEmpty());
    }

    @Test
    public void testGetFlagDescriptorsFromBandDescriptor() {
        final Family<BandDescriptor> bandDescriptor = dddb.getBandDescriptors(DBL_SM_XXXX_AUX_ECMWF_0200);
        final Family<FlagDescriptor> flagDescriptors = bandDescriptor.getMember("F1").getFlagDescriptors();
        assertNotNull(flagDescriptors);

        assertNotNull(dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF_0200 + "_flags1"));
        assertSame(flagDescriptors, dddb.getFlagDescriptors(DBL_SM_XXXX_AUX_ECMWF_0200 + "_flags1"));
    }

    @Test
    public void testFindBandDescriptorForMember() {
        final BandDescriptor descriptor = dddb.findBandDescriptorForMember(DBL_SM_XXXX_MIR_OSUDP2_0300, "Sigma_Tb_42.5Y");
        assertNotNull(descriptor);
        assertEquals("Sigma_TBY", descriptor.getBandName());
    }

    @Test
    public void testFindBandDescriptorForMember_unknownFormatName() {
        assertNull(dddb.findBandDescriptorForMember("schnick-schnack-for-mat", "Sigma_Tb_42.5Y"));
    }

    @Test
    public void testFindBandDescriptorForMember_unknownBandName() {
        assertNull(dddb.findBandDescriptorForMember(DBL_SM_XXXX_MIR_OSUDP2_0300, "rubber-band"));
    }

    @Test
    public void testGetSMDAP2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0200);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor x_swath = descriptors.getMember("X_Swath");
        assertNotNull(x_swath);
    }

    @Test
    public void testGetSMDAP2_v0201Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0201);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor tSurf_init_std = descriptors.getMember("TSurf_Init_Std");
        assertNotNull(tSurf_init_std);
    }

    @Test
    public void testGetSMDAP2_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMDAP2_0300);
        assertEquals(70, descriptors.asList().size());

        final BandDescriptor hr_in_dqx = descriptors.getMember("HR_IN_DQX");
        assertNotNull(hr_in_dqx);
    }

    @Test
    public void testGetOSDAP2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_OSDAP2_0200);
        assertEquals(133, descriptors.asList().size());

        final BandDescriptor param2_sigma_m3 = descriptors.getMember("Param2_sigma_M3");
        assertNotNull(param2_sigma_m3);

        final BandDescriptor param6_sigma_m2 = descriptors.getMember("Param6_sigma_M2");
        assertNotNull(param6_sigma_m2);
    }

    @Test
    public void testGetOSDAP2_v0300Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_OSDAP2_0300);
        assertEquals(133, descriptors.asList().size());

        final BandDescriptor out_of_lut_flags_4 = descriptors.getMember("Out_of_LUT_flags_4");
        assertNotNull(out_of_lut_flags_4);

        final BandDescriptor diff_tb_1 = descriptors.getMember("Diff_TB_1");
        assertNotNull(diff_tb_1);
    }

    @Test
    public void testGetSMUPD2_v0200Descriptors() {
        final Family<BandDescriptor> descriptors = dddb.getBandDescriptors(DBL_SM_XXXX_MIR_SMUDP2_0200);
        assertEquals(66, descriptors.asList().size());

        final BandDescriptor n_instrument_error = descriptors.getMember("N_Instrument_Error");
        assertNotNull(n_instrument_error);
    }

    @Test
    public void testGetMemberDescriptors_BWLF1C() throws IOException {
        final File hdrFile = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.HDR");

        final Family<MemberDescriptor> memberDescriptors = dddb.getMemberDescriptors(hdrFile);
        assertNotNull(memberDescriptors);
        assertEquals(12, memberDescriptors.asList().size());

        final MemberDescriptor flagsDescriptor = memberDescriptors.getMember("Flags");
        assertNotNull(flagsDescriptor);
        assertFalse(flagsDescriptor.isGridPointData());
        assertEquals("ushort", flagsDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", flagsDescriptor.getDimensionNames());
        assertEquals(0, flagsDescriptor.getMemberIndex());
        final short[] flagMasks = flagsDescriptor.getFlagMasks();
        assertNotNull(flagMasks);
        assertEquals(16, flagMasks.length);
        final short[] flagValues = flagsDescriptor.getFlagValues();
        assertNotNull(flagValues);
        assertEquals(16, flagValues.length);
        assertEquals("POL_FLAG_1 POL_FLAG_2 SUN_FOV SUN_GLINT_FOV MOON_GLINT_FOV SINGLE_SNAPSHOT FTT SUN_POINT SUN_GLINT_AREA MOON_POINT AF_FOV EAF_FOV BORDER_FOV SUN_TAILS RFI_1 RFI_2", flagsDescriptor.getFlagMeanings());
        assertEquals("", flagsDescriptor.getUnit());

        final MemberDescriptor btValueDescriptor = memberDescriptors.getMember("BT_Value");
        assertNotNull(btValueDescriptor);
        assertFalse(btValueDescriptor.isGridPointData());
        assertEquals("float", btValueDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", btValueDescriptor.getDimensionNames());
        assertEquals(1, btValueDescriptor.getMemberIndex());
        assertNull(btValueDescriptor.getFlagMasks());
        assertEquals("K", btValueDescriptor.getUnit());

        final MemberDescriptor radiometricAccuracyOfPixelDescriptor = memberDescriptors.getMember("Radiometric_Accuracy_of_Pixel");
        assertNotNull(radiometricAccuracyOfPixelDescriptor);
        assertFalse(radiometricAccuracyOfPixelDescriptor.isGridPointData());
        assertEquals("ushort", radiometricAccuracyOfPixelDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", radiometricAccuracyOfPixelDescriptor.getDimensionNames());
        assertEquals(2, radiometricAccuracyOfPixelDescriptor.getMemberIndex());
        assertNull(radiometricAccuracyOfPixelDescriptor.getFlagValues());
        assertEquals("K", radiometricAccuracyOfPixelDescriptor.getUnit());

        final MemberDescriptor azimuthAngleDescriptor = memberDescriptors.getMember("Azimuth_Angle");
        assertNotNull(azimuthAngleDescriptor);
        assertFalse(azimuthAngleDescriptor.isGridPointData());
        assertEquals("ushort", azimuthAngleDescriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", azimuthAngleDescriptor.getDimensionNames());
        assertEquals(3, azimuthAngleDescriptor.getMemberIndex());
        assertEquals("deg", azimuthAngleDescriptor.getUnit());

        final MemberDescriptor footprintAxis1Descriptor = memberDescriptors.getMember("Footprint_Axis1");
        assertNotNull(footprintAxis1Descriptor);
        assertFalse(footprintAxis1Descriptor.isGridPointData());
        assertEquals("ushort", footprintAxis1Descriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", footprintAxis1Descriptor.getDimensionNames());
        assertEquals(4, footprintAxis1Descriptor.getMemberIndex());
        assertEquals("km", footprintAxis1Descriptor.getUnit());

        final MemberDescriptor footprintAxis2Descriptor = memberDescriptors.getMember("Footprint_Axis2");
        assertNotNull(footprintAxis2Descriptor);
        assertFalse(footprintAxis2Descriptor.isGridPointData());
        assertEquals("ushort", footprintAxis2Descriptor.getDataTypeName());
        assertEquals("n_grid_points n_bt_data", footprintAxis2Descriptor.getDimensionNames());
        assertEquals(5, footprintAxis2Descriptor.getMemberIndex());
        assertEquals("km", footprintAxis2Descriptor.getUnit());

        final MemberDescriptor gridPointIdDescriptor = memberDescriptors.getMember("Grid_Point_ID");
        assertNotNull(gridPointIdDescriptor);
        assertTrue(gridPointIdDescriptor.isGridPointData());
        assertEquals("uint", gridPointIdDescriptor.getDataTypeName());
        assertEquals("n_grid_points", gridPointIdDescriptor.getDimensionNames());
        assertEquals(-1, gridPointIdDescriptor.getMemberIndex());
        assertEquals("", gridPointIdDescriptor.getUnit());

        final MemberDescriptor latitudeDescriptor = memberDescriptors.getMember("Grid_Point_Latitude");
        assertNotNull(latitudeDescriptor);
        assertTrue(latitudeDescriptor.isGridPointData());
        assertEquals("float", latitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", latitudeDescriptor.getDimensionNames());
        assertEquals(-1, latitudeDescriptor.getMemberIndex());
        assertEquals("deg", latitudeDescriptor.getUnit());

        final MemberDescriptor longitudeDescriptor = memberDescriptors.getMember("Grid_Point_Longitude");
        assertNotNull(longitudeDescriptor);
        assertTrue(longitudeDescriptor.isGridPointData());
        assertEquals("float", longitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", longitudeDescriptor.getDimensionNames());
        assertEquals(-1, longitudeDescriptor.getMemberIndex());
        assertEquals("deg", longitudeDescriptor.getUnit());

        final MemberDescriptor altitudeDescriptor = memberDescriptors.getMember("Grid_Point_Altitude");
        assertNotNull(altitudeDescriptor);
        assertTrue(altitudeDescriptor.isGridPointData());
        assertEquals("float", altitudeDescriptor.getDataTypeName());
        assertEquals("n_grid_points", altitudeDescriptor.getDimensionNames());
        assertEquals(-1, altitudeDescriptor.getMemberIndex());
        assertEquals("m", altitudeDescriptor.getUnit());

        final MemberDescriptor maskDescriptor = memberDescriptors.getMember("Grid_Point_Mask");
        assertNotNull(maskDescriptor);
        assertTrue(maskDescriptor.isGridPointData());
        assertEquals("ubyte", maskDescriptor.getDataTypeName());
        assertEquals("n_grid_points", maskDescriptor.getDimensionNames());
        assertEquals(-1, maskDescriptor.getMemberIndex());
        assertEquals("", maskDescriptor.getUnit());

        final MemberDescriptor btCountDescriptor = memberDescriptors.getMember("BT_Data_Counter");
        assertNotNull(btCountDescriptor);
        assertTrue(btCountDescriptor.isGridPointData());
        assertEquals("ubyte", btCountDescriptor.getDataTypeName());
        assertEquals("n_grid_points", btCountDescriptor.getDimensionNames());
        assertEquals(-1, btCountDescriptor.getMemberIndex());
        assertEquals("", btCountDescriptor.getUnit());
    }

    @Test
    public void testGetOriginalName() {
        final Properties properties = new Properties();
        properties.setProperty("a_key", "a_value");
        properties.setProperty("another_key", "another_value");

        final String key = Dddb.findOriginalName(properties, "another_value");
        assertEquals("another_key", key);
    }

    @Test
    public void testGetOriginalName_nameNotInProperties() {
        final Properties properties = new Properties();
        properties.setProperty("a_key", "a_value");
        properties.setProperty("another_key", "another_value");

        final String key = Dddb.findOriginalName(properties, "the Werner Value");
        assertNull(key);
    }
}
