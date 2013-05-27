package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.smos.ee2netcdf.TestHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NetCDFExportDialogTest {

    @Test
    public void testGetTargetFiles_singleProduct() throws IOException {
        final File resourceFile = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final String filePath = resourceFile.getAbsolutePath();
        final File targetDir = new File("/home/tom/target");


        final List<File> targetFiles = NetCDFExportDialog.getTargetFiles(filePath, targetDir);
        assertNotNull(targetFiles);
        assertEquals(1, targetFiles.size());
        assertEquals("/home/tom/target/SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc", targetFiles.get(0).getAbsolutePath());
    }

    @Test
    public void testGetTargetFiles_directory() throws IOException {
        final File resourceDirectory = TestHelper.getResourceDirectory();
        final File targetDir = new File("/home/tom/target");

        final String wildCardpath = resourceDirectory.getAbsolutePath() + File.separator + "*";
        final List<File> targetFiles = NetCDFExportDialog.getTargetFiles(wildCardpath, targetDir);
        assertNotNull(targetFiles);
        assertEquals(2, targetFiles.size());
        assertEquals("/home/tom/target/SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.nc", targetFiles.get(0).getAbsolutePath());
        assertEquals("/home/tom/target/SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.nc", targetFiles.get(1).getAbsolutePath());
    }

    @Test
    public void testGetExistingFiles_noneExists() {
        final ArrayList<File> targetFiles = new ArrayList<File>();
        targetFiles.add(new File("/fantasy/location/target/file"));
        targetFiles.add(new File("/not/existing/file"));

        final List<File> existingFiles = NetCDFExportDialog.getExistingFiles(targetFiles);
        assertNotNull(existingFiles);
        assertEquals(0, existingFiles.size());
    }

    @Test
    public void testGetExistingFiles_oneExists() {
        final ArrayList<File> targetFiles = new ArrayList<File>();
        targetFiles.add(new File("/fantasy/location/target/file"));
        targetFiles.add(TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));

        final List<File> existingFiles = NetCDFExportDialog.getExistingFiles(targetFiles);
        assertNotNull(existingFiles);
        assertEquals(1, existingFiles.size());
    }

    @Test
    public void testGetExistingFiles_twoExists() {
        final ArrayList<File> targetFiles = new ArrayList<File>();
        targetFiles.add(TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip"));
        targetFiles.add(new File("/fantasy/location/target/file"));
        targetFiles.add(TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip"));

        final List<File> existingFiles = NetCDFExportDialog.getExistingFiles(targetFiles);
        assertNotNull(existingFiles);
        assertEquals(2, existingFiles.size());
    }

    @Test
    public void testListToString() {
        final File file_1 = TestHelper.getResourceFile("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        final File file_2 = TestHelper.getResourceFile("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip");

        final ArrayList<File> targetFiles = new ArrayList<File>();
        targetFiles.add(file_1);
        targetFiles.add(file_2);

        assertEquals(file_1.getAbsolutePath() + "\n" + file_2.getAbsolutePath() + "\n", NetCDFExportDialog.listToString(targetFiles));
    }

    @Test
    public void testListToString_ellipseAfterTenFiles() {

        final ArrayList<File> targetFiles = new ArrayList<File>();
        for (int i = 0; i < 14; i++) {
            targetFiles.add(new File("blabla_" + i));
        }

        assertTrue(NetCDFExportDialog.listToString(targetFiles).contains("..."));
    }
}

