package org.esa.beam.smos;


import junit.framework.TestCase;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SmosUtilsTest extends TestCase {

    public void testFindDblFile() {
        File[] inputFiles = new File[0];
        assertNull(SmosUtils.findDblFile(inputFiles));

        inputFiles = new File[]{new File("murks.txt"), new File("blärks.dot")};
        assertNull(SmosUtils.findDblFile(inputFiles));

        inputFiles = new File[]{new File("murks.DBL"), new File("blärks.dot")};
        File dblFile = SmosUtils.findDblFile(inputFiles);
        assertEquals("murks.DBL", dblFile.getName());

        inputFiles = new File[]{new File("murks.txt"), new File("blärks.dbl")};
        dblFile = SmosUtils.findDblFile(inputFiles);
        assertEquals("blärks.dbl", dblFile.getName());
    }

    public void testGetSensingTimesFromFilename() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        final Date startTime = SmosUtils.getSensingStartTimeFromFilename("SM_OPER_MIR_SMUDP2_20101019T050111_20101019T055510_309_002_1.zip");
        final Date stopTime = SmosUtils.getSensingStopTimeFromFilename("SM_OPER_MIR_SMUDP2_20101019T050111_20101019T055510_309_002_1.zip");

        assertEquals("2010-10-19T05:01:11", df.format(startTime));
        assertEquals("2010-10-19T05:55:10", df.format(stopTime));
    }

    public void testGetSensingTimesFromTooShortFilename() {
        assertNull(SmosUtils.getSensingStartTimeFromFilename("SM_OPER_MIR_SMUDP2_20101019T0501"));
        assertNull(SmosUtils.getSensingStopTimeFromFilename("SM_OPER_MIR_SMUDP2_20101019T050111_20101019T0555"));
    }

    public void testGetStartDateFromFilename() {
        final Calendar cal = GregorianCalendar.getInstance();

        cal.set(2007, 0, 1);
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_PATT99_20070101T000000_20781231T235959_00000006.EEF")));
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_BWGHT__20070101T000000_20781231T235959_00000003.EEF")));
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_RFI____20070101T000000_20781231T235959_00000001.EEF")));
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_RFI____20070101T235959_20781231T235959_00000001.EEF")));

        cal.set(2007, 1, 23);
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_VTEC___20070223T000000_20070224T000000_00000001.EEF")));

        cal.set(2012, 10, 18);
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip")));

        cal.set(2012, 10, 20);
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_OPER_MIR_UNCU1A_20121120T010256_20121120T014313_116_001_1.zip")));

        cal.set(2005, 0, 1);
        assertTrue(isSameDay(cal.getTime(), SmosUtils.getSensingStartDayFromFilename("SM_TEST_AUX_MOONT__20050101T000000_20500101T000000_001_001_4.zip")));
    }

    public void testGetProductType() {
        assertEquals("AUX_DATA", SmosUtils.getProductType("SM_TEST_AUX_LSMASK_20070101T000000_20781231T235959_00000001.EEF"));
        assertEquals("AUX_DATA", SmosUtils.getProductType("SM_TEST_AUX_BFP____20070101T000000_20781231T235959_00000004.EEF"));
        assertEquals("AUX_DATA", SmosUtils.getProductType("SM_TEST_AUX_SUNT___20070101T000000_20781231T235959_00000001.EEF"));
        assertEquals("MIR_GMATD_", SmosUtils.getProductType("SM_TEST_MIR_GMATD__20121117T020130_20781231T235959_115_001_3.tgz"));
        assertEquals("AUX_DATA", SmosUtils.getProductType("SM_TEST_AUX_CNFL1P_20120101T000000_20500101T000000_101_001_3.EEF"));

        assertEquals("MIR_CRSD1A", SmosUtils.getProductType("SM_OPER_MIR_CRSD1a_20121117T025249_20121117T043300_116_001_1.zip"));

        assertEquals("RQC_RQD", SmosUtils.getProductType("SM_OPER_RQC_VTEC_C_20090512T230000_20090514T010000_311_001_1.EEF"));
        assertEquals("RQC_RQD", SmosUtils.getProductType("SM_TEST_RQD_SCND1C_20121117T120515_20121117T122512_311_001_1.EEF"));
    }

    public void testIsDBLFileName() {
        assertFalse(SmosUtils.isDblFileName("plupsi.txt"));
        assertFalse(SmosUtils.isDblFileName("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
        assertFalse(SmosUtils.isDblFileName("noExtensionFile"));

        assertTrue(SmosUtils.isDblFileName("SM_xxxx_MIR_CORN0__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isDblFileName("SM_BLAH_MIR_TARD1A_20070223T112710_20070223T121514_001_001_0.DBL"));
    }

    public void testIsHDRFileName() {
        assertFalse(SmosUtils.isHdrFileName("plupsi.txt"));
        assertFalse(SmosUtils.isHdrFileName("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
        assertFalse(SmosUtils.isHdrFileName("noExtensionFile"));

        assertTrue(SmosUtils.isHdrFileName("SM_xxxx_MIR_CORN0__20070223T061024_20070223T062500_001_001_0.HDR"));
        assertTrue(SmosUtils.isHdrFileName("SM_BLAH_MIR_TARD1A_20070223T112710_20070223T121514_001_001_0.hdr"));
    }

    public void testIsL0FileType() {
        assertTrue(SmosUtils.isL0Type("SM_HELP_MIR_SC_F0__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL0Type("SM_xxxx_MIR_UNCU0__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL0Type("SM_xxxx_MIR_CORN0__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL0Type("SM_TEST_MIR_SC_F0__20070223T112710_20070223T121514_001_001_0.DBL"));

        assertFalse(SmosUtils.isL0Type("SM_TEST_AUX_IGRF___20080102T010000_20080102T025959_105_001_0.zip"));
        assertFalse(SmosUtils.isL0Type("SM_TEST_AUX_PATT99_20070101T000000_20781231T235959_00000006.EEF"));
        assertFalse(SmosUtils.isL0Type("SM_TEST_AUX_IGRF___20080102T010000_20080102T025959_105_001_0.zip"));
    }

    public void testIsL1aFileType() {
        assertTrue(SmosUtils.isL1aType("SM_OPER_MIR_TARD1A_20121117T102025_20121117T105144_203_001_1.EEF"));
        assertTrue(SmosUtils.isL1aType("SM_OPER_MIR_CRSD1A_20121117T025249_20121117T043300_116_001_1.zip"));
        assertTrue(SmosUtils.isL1aType("SM_TEST_MIR_AFWU1A_20121117T022130_20121117T024030_203_001_1.EEF"));
        assertTrue(SmosUtils.isL1aType("SM_TEST_MIR_ANIR1A_20121119T213500_20121119T231156_306_001_3.zip"));

        assertFalse(SmosUtils.isL1aType("SM_TEST_MIR_GMATD__20121117T020130_20781231T235959_115_001_3.tgz"));
        assertFalse(SmosUtils.isL1aType("SM_OPER_RQD_BWSF1C_20121120T015956_20121120T023316_203_001_1.EEF"));
        assertFalse(SmosUtils.isL1aType("SM_OPER_AUX_ORBRES_20121118T000000_20121125T000000_240_028_1.EEF"));
    }

    public void testIsL1bFileType() {
        assertTrue(SmosUtils.isL1bType("SM_OPER_MIR_SC_D1B_20121118T002704_20121118T012104_116_001_1.zip"));
        assertTrue(SmosUtils.isL1bType("SM_OPER_MIR_TARD1B_20121117T102025_20121117T105144_116_001_1.zip"));
        assertTrue(SmosUtils.isL1bType("SM_TEST_MIR_GMATU__20121117T022130_20781231T235959_115_001_3.tgz"));
        assertTrue(SmosUtils.isL1bType("SM_TEST_MIR_FTTD___20091228T124638_20091228T125235_307_002_3.zip"));
        assertTrue(SmosUtils.isL1bType("SM_TEST_MIR_FTTF___20121120T005052_20121120T005310_308_001_1.zip"));

        assertFalse(SmosUtils.isL1bType("M_OPER_MIR_CRSD1A_20121117T025249_20121117T043300_116_001_1.zip"));
        assertFalse(SmosUtils.isL1bType("SM_OPER_RQD_BWSF1C_20121120T015956_20121120T023316_203_001_1.EEF"));
        assertFalse(SmosUtils.isL1bType("SM_OPER_AUX_ORBRES_20121118T000000_20121125T000000_240_028_1.EEF"));
    }

    public void testIsL1cFileType() {
        assertTrue(SmosUtils.isL1cType("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.DBL"));
        assertTrue(SmosUtils.isL1cType("SM_TEST_MIR_BWLD1C_20070223T061024_20070223T070437_141_000_0.HDR"));
        assertTrue(SmosUtils.isL1cType("SM_TEST_MIR_BWLF1C_20070223T112729_20070223T121644_141_000_0.DBL"));

        assertFalse(SmosUtils.isL1cType("SM_TEST_AUX_RFI____20070101T000000_20781231T235959_00000001.EEF"));
        assertFalse(SmosUtils.isL1cType("SM_TEST_AUX_RFI____20070101T000000_20781231T235959_00000001.EEF"));
        assertFalse(SmosUtils.isL1cType("SM_BLAH_MIR_TARD1A_20070223T112710_20070223T121514_001_001_0.DBL"));
    }

    public void testIsL2FileType() {
        assertTrue(SmosUtils.isL2Type("SO_GNAT_MIR_TSM_2__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL2Type("SO_GNOT_MIR_TOS_2__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL2Type("SO_GNAT_MIR_SM__2__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL2Type("SO_GNOT_MIR_OS__2__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertTrue(SmosUtils.isL2Type("SM_TEST_MIR_SMUDP2_20121118T143742_20121118T153047_303_002_1.zip"));
        assertTrue(SmosUtils.isL2Type("SM_TEST_MIR_SMDAP2_20121118T135052_20121118T144140_303_007_1.zip"));
        assertTrue(SmosUtils.isL2Type("SM_TEST_MIR_OSUDP2_20121118T143742_20121118T153047_306_002_1.zip"));
        assertTrue(SmosUtils.isL2Type("SM_TEST_MIR_OSDAP2_20121118T143742_20121118T153047_306_002_1.zip"));

        assertFalse(SmosUtils.isL2Type("SM_TEST_AUX_IGRF___20080102T010000_20080102T025959_105_001_0.zip"));
        assertFalse(SmosUtils.isL2Type("SM_TEST_AUX_PATT99_20070101T000000_20781231T235959_00000006.EEF"));
        assertFalse(SmosUtils.isL2Type("SM_TEST_MIR_SC_F0__20070223T112710_20070223T121514_001_001_0.DBL"));
    }

    public void testIsAuxFileType() {
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ORBPRE_20121117T000000_20121124T000000_240_034_1.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_APOD01_20070101T000000_20781231T235959_00000002.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_BFP____20070101T000000_20781231T235959_00000003.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_BSCAT__20070101T000000_20781231T235959_00000002.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_BWGHT__20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_CNFL1P_20120101T000000_20500101T000000_101_001_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGG____20050101T000000_20500101T000000_001_002_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGG____20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_FAIL___20070101T000000_20781231T235959_00000002.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_FLATT__20070101T000000_20781231T235959_00000014.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_GALAXY_20050101T000000_20500101T000000_001_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_GLXY___20070101T000000_20781231T235959_000000007.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_GALNIR_20050101T000000_20500101T000000_300_001_5.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_LCF____20070101T000000_20781231T235959_00000004.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_MASK___20050101T000000_20500101T000000_001_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_MASK___20070101T000000_20781231T235959_00000006.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_MOONT__20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_NIR____20070101T000000_20781231T235959_00000006.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_PLM____20070101T000000_20781231T235959_0000000012.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_PMS____20070101T000000_20781231T235959_00000005.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_VTEC___20080102T010000_20080102T025959_105_001_0.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_RFI____20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_IGRF___20080102T010000_20080102T025959_105_001_0.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_SPAR___20070101T000000_20781231T235959_00000006.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_SUNT___20050101T000000_20500101T000000_001_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_SUNT___20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_SGLINT_20050101T000000_20500101T000000_001_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_TIME___20050101T000000_20500101T000000_001_001_4.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_VTEC_C_20080102T010000_20080102T025959_105_001_0.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_VTEC_P_20080414T010000_20080414T025959_105_001_0.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_PATT99_20070101T000000_20781231T235959_00000006.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_PATT12_20070101T000000_20781231T235959_00000004.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ORBPRE_20121117T000000_20121124T000000_240_034_1.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_APDS00_20050101T000000_20500101T000000_001_001_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_LSMASK_20070101T000000_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ECMWF__20090408T005820_20090408T020050_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DFFFRA_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DFFXYZ_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DFFLAI_20090509T000000_20090608T000000_302_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DFFLMX_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DGGXYZ_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGGTLV_20121129T010000_20121130T030000_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGGTFO_20121129T010000_20121130T030000_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGGROU_20121129T010000_20121130T030000_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGGRFI_20121129T010000_20121130T030000_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_DGGFLO_20121129T010000_20121130T030000_301_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_WEF____20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_MN_WEF_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_SOIL_P_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_GAL_SM_20050101T000000_20500101T000000_001_001_9.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_LANDCL_20050101T000000_20500101T000000_001_001_9.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_CNFSMD_20050101T000000_20500101T000000_001_001_9.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_CNFSMF_20050101T000000_20500101T000000_001_001_9.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_FLTSEA_20050101T000000_20500101T000000_001_010_8.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_RGHNS1_20050101T000000_20500101T000000_001_011_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_RGHNS2_20050101T000000_20500101T000000_001_010_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_RGHNS3_20050101T000000_20500101T000000_001_010_8.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_GAL_OS_20050101T000000_20500101T000000_001_004_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_GAL2OS_20050101T000000_20500101T000000_001_010_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_FOAM___20050101T000000_20500101T000000_001_010_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ATMOS__20050101T000000_20500101T000000_001_010_8.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_DISTAN_20050101T000000_20500101T000000_001_010_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_SSS____20050101T000000_20500101T000000_001_010_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_CNFOSD_20050101T000000_20500101T000000_001_010_8.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_CNFOSF_20050101T000000_20500101T000000_001_010_8.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_AGDPT__20050101T000000_20500101T000000_001_003_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_ECOLAI_20050101T000000_20500101T000000_001_003_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_BNDLST_20050101T000000_20500101T000000_001_003_8.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_MISP___20050101T000000_20500101T000000_300_001_5.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_APDL___20050101T000000_20500101T000000_300_001_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_TEST_AUX_APDS___20050101T000000_20500101T000000_300_001_5.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ORBRES_20091105T000000_20091106T000000_280_009_1.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_CNFFAR_20050101T000000_20500101T000000_100_002_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT1D__20050101T000000_20500101T000000_001_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT1F__20050101T000000_20500101T000000_001_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT2D__20050101T000000_20500101T000000_001_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT3F__20050101T000000_20500101T000000_001_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT2F__20050101T000000_20500101T000000_001_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_OTT3D__20050101T000000_20500101T000000_001_004_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_BULL_B_20101202T000000_20500101T000000_100_002_3.zip"));

        // first files delivered, needed to adapt code - testdata did not follow file name specs ....
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_VTEC_R_20091117T230000_20091119T010000_306_001_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_PLM____20050101T000000_20500101T000000_300_006_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_BFP____20050101T000000_20500101T000000_300_001_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_BSCAT__20050101T000000_20500101T000000_300_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_BWGHT__20050101T000000_20500101T000000_300_003_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_FAIL___20050101T000000_20500101T000000_300_001_4.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_LCF____20050101T000000_20500101T000000_300_005_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_MOONT__20050101T000000_20500101T000000_300_001_4.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_NIR____20050101T000000_20500101T000000_300_002_4.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_PMS____20050101T000000_20500101T000000_300_005_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_RFI____20050101T000000_20500101T000000_300_002_3.zip"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_SPAR___20050101T000000_20500101T000000_320_001_3.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_ORBRES_20091119T000000_20091120T000000_280_023_1.EEF"));
        assertTrue(SmosUtils.isAuxFileType("SM_OPER_AUX_CNFL0P_20050101T000000_20500101T000000_001_002_3.EEF"));

        assertFalse(SmosUtils.isAuxFileType("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
        assertFalse(SmosUtils.isAuxFileType("SM_TEST_MIR_GMATD__20121117T020130_20781231T235959_115_001_3.tgz"));
    }

    public void testIsQualityControlType() {
        assertTrue(SmosUtils.isQualityControlType("SM_OPER_RQC_ECMWF__20090405T011500_20090405T021820_301_001_1.EEF"));
        assertTrue(SmosUtils.isQualityControlType("SM_TEST_RQD_OSDAP2_20121117T065617_20121117T075017_311_001_1.EEF"));

        assertFalse(SmosUtils.isQualityControlType("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
        assertFalse(SmosUtils.isQualityControlType("SM_xxxx_MIR_CORN0__20070223T061024_20070223T062500_001_001_0.DBL"));
    }

    public void testIsMirasPlanType() {
        assertTrue(SmosUtils.isMirasPlanType("SM_TEST_MPL_ORBSCT_20070223T060002_20781231T235959_00000001.EEF"));
        assertTrue(SmosUtils.isMirasPlanType("SM_OPER_MPL_PROTEV_20091120T101808_20091221T101153_280_023_1.EEF"));
        assertTrue(SmosUtils.isMirasPlanType("SM_OPER_MPL_XBDOWN_20091123T000000_20091124T000000_331_001_1.EEF"));
        assertTrue(SmosUtils.isMirasPlanType("SM_OPER_MPL_APIDPL_20091120T000000_20091123T000000_331_001_1.EEF"));
        assertTrue(SmosUtils.isMirasPlanType("SM_OPER_MPL_HLPLAN_20091120T000000_20091123T000000_331_001_1.EEF"));
        assertTrue(SmosUtils.isMirasPlanType("SM_OPER_MPL_XBDPRE_20091123T000000_20091130T000000_110_001_7.EEF"));

        assertFalse(SmosUtils.isMirasPlanType("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
        assertFalse(SmosUtils.isMirasPlanType("SM_TEST_AUX_IGRF___20080102T010000_20080102T025959_105_001_0.zip"));
        assertFalse(SmosUtils.isMirasPlanType("TheNewSensoir.txt"));
    }

    public void testIsAuxECMWFType() {
        assertTrue(SmosUtils.isAuxECMWFType("SM_OPER_AUX_ECMWF__20091113T030500_20091113T040730_306_001_3.HDR"));
        assertTrue(SmosUtils.isAuxECMWFType("SM_OPER_AUX_ECMWF__20110227T182140_20110227T192410_310_001_3.zip"));

        assertFalse(SmosUtils.isAuxECMWFType("SM_xxxx_MIR_CORN0__20070223T061024_20070223T062500_001_001_0.DBL"));
        assertFalse(SmosUtils.isAuxECMWFType("SM_TEST_MIR_OSUDP2_20121118T143742_20121118T153047_306_002_1.zip"));
        assertFalse(SmosUtils.isAuxECMWFType("SM_OPER_MIR_BWLD1C_20121118T002733_20121118T012104_116_001_1.zip"));
    }

    public void testIsSmUserFormat() {
        assertTrue(SmosUtils.isSmUserFormat("SM_TEST_MIR_SMUDP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosUtils.isSmUserFormat("SM_OPER_MIR_SCSF1C_20100315T144805_20100315T154207_330_001_1"));
    }

    public void testIsSmAnalysisFormat() {
        assertTrue(SmosUtils.isSmAnalysisFormat("SM_TEST_MIR_SMDAP2_20121117T183648_20121117T193048_304_001_1.zip"));
        assertFalse(SmosUtils.isSmAnalysisFormat("SM_OPER_MIR_SCSF1C_20100315T144805_20100315T154207_330_001_1"));
    }

    public void testIsOsUserFormat() {
        assertTrue(SmosUtils.isOsUserFormat("SM_TEST_MIR_OSUDP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosUtils.isOsUserFormat("SM_TEST_MIR_BWSF1C_20070223T112729_20070223T121644_141_000_0.zip"));
    }

    public void testIsOsAnalysisFormat() {
        assertTrue(SmosUtils.isOsAnalysisFormat("SM_TEST_MIR_OSDAP2_20070225T041815_20070225T050750_306_001_8.DBL"));
        assertFalse(SmosUtils.isOsAnalysisFormat("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));
    }

    public void testIsDualPolBrowseFormat() {
        assertTrue(SmosUtils.isDualPolBrowseFormat("SM_OPER_MIR_BWLD1C_20100405T143038_20100405T152439_330_001_1.HDR"));
        assertTrue(SmosUtils.isDualPolBrowseFormat("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_001_1.HDR"));

        assertFalse(SmosUtils.isDualPolBrowseFormat("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));
        assertFalse(SmosUtils.isDualPolBrowseFormat("SM_OPER_MIR_SMDAP2_20111130T141947_20111130T151305_500_001_1.DBL"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ////////////////////////////////////////////////////////////////////////////////

    static boolean isSameDay(Date date_1, Date date_2) {
        if (date_1 == null || date_2 == null) {
            return false;
        }

        final Calendar cal_1 = GregorianCalendar.getInstance();
        cal_1.setTime(date_1);
        final Calendar cal_2 = GregorianCalendar.getInstance();
        cal_2.setTime(date_2);

        //noinspection RedundantIfStatement
        if (cal_1.get(Calendar.YEAR) == cal_2.get(Calendar.YEAR)
                && cal_1.get(Calendar.MONTH) == cal_2.get(Calendar.MONTH)
                && cal_1.get(Calendar.DAY_OF_MONTH) == cal_2.get(Calendar.DAY_OF_MONTH)) {
            return true;

        }
        return false;
    }
}
