package org.esa.beam.dataio.smos;

import junit.framework.TestCase;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class SmosHeaderTest extends TestCase {

    public void testSCLD1C() throws IOException {
        final SmosHeader smosHeader = loadHeader("/headers/SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0.HDR");

        final EeFixedHeader fixedHeader = smosHeader.getFixedHeader();
        assertNotNull(fixedHeader);
        assertEquals("SM_TEST_MIR_SCLD1C_20070223T061024_20070223T070437_141_000_0", fixedHeader.getFileName());
        assertEquals("MIR_SCLD1C", fixedHeader.getFileType());
        assertEquals("TEST", fixedHeader.getFileClass());
        assertEquals("0001", fixedHeader.getFileVersion());
        assertEquals("Level 1C Dual Polarization Land Science measurements product", fixedHeader.getFileDescription());
        assertEquals("SMOS", fixedHeader.getMission());
        assertEquals("x", fixedHeader.getNotes());
        
        final SmosDsDescriptor[] dsDescriptors = smosHeader.getDsDescriptors();
        assertNotNull(dsDescriptors);
        assertEquals(13, dsDescriptors.length);

        SmosDsDescriptor dsd;

        dsd = dsDescriptors[0];
        assertNotNull(dsd);
        assertEquals("SNAPSHOT_LIST", dsd.getDsName());
        assertEquals('M', dsd.getDsType());
        assertEquals(434704L, dsd.getDsSize());
        assertEquals(0L, dsd.getDsOffset());
        assertEquals("", dsd.getRefFilename());
        assertEquals(2700, dsd.getDsrCount());
        assertEquals(161, dsd.getDsrSize());
        assertEquals(ByteOrder.LITTLE_ENDIAN, dsd.getByteOrder());

        dsd = dsDescriptors[1];
        assertNotNull(dsd);
        assertEquals("TEMP_SWATH_DUAL", dsd.getDsName());
        assertEquals('M', dsd.getDsType());
        assertEquals(91723978L, dsd.getDsSize());
        assertEquals(0L, dsd.getDsOffset());
        assertEquals("", dsd.getRefFilename());
        assertEquals(42831, dsd.getDsrCount());
        assertEquals(-1, dsd.getDsrSize());
        assertEquals(ByteOrder.LITTLE_ENDIAN, dsd.getByteOrder());
    }

    public void testSCLF1C() throws IOException {
        final SmosHeader smosHeader = loadHeader("/headers/SM_TEST_MIR_SCLF1C_20070223T112729_20070223T121644_141_000_0.HDR");


        final EeFixedHeader fixedHeader = smosHeader.getFixedHeader();
        assertNotNull(fixedHeader);
        assertEquals("SM_TEST_MIR_SCLF1C_20070223T112729_20070223T121644_141_000_0", fixedHeader.getFileName());
        assertEquals("MIR_SCLF1C", fixedHeader.getFileType());
        assertEquals("TEST", fixedHeader.getFileClass());
        assertEquals("0001", fixedHeader.getFileVersion());
        assertEquals("Level 1C Full Polarization Land Science measurements product", fixedHeader.getFileDescription());
        assertEquals("SMOS", fixedHeader.getMission());
        assertEquals("x", fixedHeader.getNotes());


        final SmosDsDescriptor[] dsDescriptors = smosHeader.getDsDescriptors();
        assertNotNull(dsDescriptors);
        assertEquals(13, dsDescriptors.length);

        SmosDsDescriptor dsd;

        dsd = dsDescriptors[0];
        assertNotNull(dsd);
        assertEquals("SNAPSHOT_LIST", dsd.getDsName());
        assertEquals('M', dsd.getDsType());
        assertEquals(384633L, dsd.getDsSize());
        assertEquals(0L, dsd.getDsOffset());
        assertEquals("", dsd.getRefFilename());
        assertEquals(2389, dsd.getDsrCount());
        assertEquals(161, dsd.getDsrSize());
        assertEquals(ByteOrder.LITTLE_ENDIAN, dsd.getByteOrder());

        dsd = dsDescriptors[1];
        assertNotNull(dsd);
        assertEquals("TEMP_SWATH_FULL", dsd.getDsName());
        assertEquals('M', dsd.getDsType());
        assertEquals(316891396L, dsd.getDsSize());
        assertEquals(0L, dsd.getDsOffset());
        assertEquals("", dsd.getRefFilename());
        assertEquals(82738, dsd.getDsrCount());
        assertEquals(-1, dsd.getDsrSize());
        assertEquals(ByteOrder.LITTLE_ENDIAN, dsd.getByteOrder());
    }

    private SmosHeader loadHeader(String path) throws IOException {
        final InputStream stream = getClass().getResourceAsStream(path);
        final SmosHeader smosHeader = new SmosHeader(stream);
        stream.close();
        return smosHeader;
    }
}