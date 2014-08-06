package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class ExporterUtilsTest {

    @Test
    public void testCreateInputFileSet_emptyList() {
        final TreeSet<File> inputFileSet = ExporterUtils.createInputFileSet(new String[0]);
        assertNotNull(inputFileSet);
        assertEquals(0, inputFileSet.size());
    }

    @Test
    public void testCreateInputFileSet_oneDir() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = ExporterUtils.createInputFileSet(new String[]{resourcePath + File.separator + "*"});
        assertNotNull(inputFileSet);
        assertEquals(4, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.zip", iterator.next().getName());
        assertEquals("SM_OPER_MIR_SMUDP2_20120514T163815_20120514T173133_551_001_1.zip", iterator.next().getName());
        assertEquals("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.zip", iterator.next().getName());
    }

    @Test
    public void testCreateInputFileSet_oneDir_wildcard() {
        final String resourcePath = getResourcePath();
        final TreeSet<File> inputFileSet = ExporterUtils.createInputFileSet(new String[]{resourcePath + File.separator + "*BWL*"});
        assertNotNull(inputFileSet);
        assertEquals(1, inputFileSet.size());
        final Iterator<File> iterator = inputFileSet.iterator();
        assertEquals("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip", iterator.next().getName());
    }

    @Test
    public void testEnsureNoDuplicateDblFiles_noDuplicates() {
        final TreeSet<File> files = new TreeSet<>();
        files.add(new File("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_002_1.zip"));
        files.add(new File("SM_OPER_MIR_OSUDP2_20120514T181819_20120514T191137_550_001_1.zip"));
        files.add(new File("SM_OPER_MIR_SCLF1C_20100119T002827_20100119T012226_323_001_1.zip"));

        final TreeSet<File> cleanedSet = ExporterUtils.ensureNoDuplicateDblFiles(files);
        assertEquals(3, cleanedSet.size());
    }

    @Test
    public void testEnsureNoDuplicateDblFiles_duplicatesRemoved() {
        final TreeSet<File> files = new TreeSet<>();
        files.add(new File("SM_OPER_MIR_BWSD1C_20100201T134256_20100201T140057_324_002_1.zip"));
        files.add(new File("SM_OPER_MIR_OSUDP2_20120514T181819_20120514T191137_550_001_1.hdr"));
        files.add(new File("SM_OPER_MIR_OSUDP2_20120514T181819_20120514T191137_550_001_1.dbl"));
        files.add(new File("SM_OPER_MIR_SCLF1C_20100119T002827_20100119T012226_323_001_1.zip"));

        final TreeSet<File> cleanedSet = ExporterUtils.ensureNoDuplicateDblFiles(files);
        assertEquals(3, cleanedSet.size());
    }

    @Test
    public void testGetSpecificProductHeader_noVariableHeader() {
        final Product product = new Product("hic", "haec", 2, 2);

        assertNull(ExporterUtils.getSpecificProductHeader(product));
    }

    @Test
    public void testGetSpecificProductHeader_noSpecificHeader() {
        final Product product = new Product("helge", "schneider", 2, 2);
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(new MetadataElement("Variable_Header"));

        assertNull(ExporterUtils.getSpecificProductHeader(product));
    }

    @Test
    public void testGetSpecificProductHeader() {
        final Product product = new Product("nasen", "mann", 2, 2);
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement variable_header = new MetadataElement("Variable_Header");
        final MetadataElement sph = new MetadataElement("Specific_Product_Header");
        variable_header.addElement(sph);
        metadataRoot.addElement(variable_header);

        final MetadataElement specificProductHeader = ExporterUtils.getSpecificProductHeader(product);
        assertSame(sph, specificProductHeader);
    }

    @Test
    public void testCorrectScaleFactor_variableNotPresent() {
        Map<String, VariableDescriptor> variableDescriptors = new HashMap<>();
        variableDescriptors.put("_its_here", new VariableDescriptor());

        ExporterUtils.correctScaleFactor(variableDescriptors, "the_missing", 2.8);
    }

    @Test
    public void testCorrectScaleFactor() {
        Map<String, VariableDescriptor> variableDescriptors = new HashMap<>();
        variableDescriptors.put("the_one", new VariableDescriptor());
        final VariableDescriptor corrected = new VariableDescriptor();
        corrected.setScaleFactor(2.8);
        variableDescriptors.put("corrected", corrected);

        ExporterUtils.correctScaleFactor(variableDescriptors, "corrected", 2);

        assertEquals(5.6, corrected.getScaleFactor(), 1e-8);
    }

    private String getResourcePath() {
        File testDir = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/");
        if (!testDir.exists()) {
            testDir = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/");
        }
        return testDir.getPath();
    }
}
