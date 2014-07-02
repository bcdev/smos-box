package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbstractFormatExporterTest {

    private MetadataElement metadataRoot;

    @Before
    public void setUp() {
        metadataRoot = new MetadataElement("root");
    }

    @Test
    public void testExtractMetadata_noMetadata() {
        final Properties metaProperties = AbstractFormatExporter.extractMetadata(metadataRoot);

        assertNotNull(metaProperties);
        assertEquals(0, metaProperties.size());
    }

    @Test
    public void testExtractMetadata_firstLevel() {
        metadataRoot.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        metadataRoot.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));

        final Properties properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_1", properties.getProperty("attribute_1"));
    }

    @Test
    public void testExtractMetadata_secondLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        secondary.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        secondary.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        metadataRoot.addElement(secondary);

        final Properties properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_2", properties.getProperty("secondary:attribute_2"));
    }

    @Test
    public void testExtractMetadata_thirdLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third = new MetadataElement("third");
        third.addAttribute(new MetadataAttribute("attribute_1", ProductData.ASCII.createInstance("hoppla_1"), true));
        third.addAttribute(new MetadataAttribute("attribute_2", ProductData.ASCII.createInstance("hoppla_2"), true));
        secondary.addElement(third);
        metadataRoot.addElement(secondary);

        final Properties properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("hoppla_1", properties.getProperty("secondary:third:attribute_1"));
    }

    @Test
    public void testExtractMetadata_mixedLevel() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third = new MetadataElement("third");
        third.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("yeah_3"), true));
        third.addAttribute(new MetadataAttribute("att_3_2", ProductData.ASCII.createInstance("yeah_4"), true));
        secondary.addElement(third);
        secondary.addAttribute(new MetadataAttribute("att_2", ProductData.ASCII.createInstance("yeah_5"), true));
        metadataRoot.addElement(secondary);
        metadataRoot.addAttribute(new MetadataAttribute("root_1", ProductData.ASCII.createInstance("yeah_6"), true));
        metadataRoot.addAttribute(new MetadataAttribute("root_2", ProductData.ASCII.createInstance("yeah_7"), true));

        final Properties properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(5, properties.size());
        assertEquals("yeah_3", properties.getProperty("secondary:third:att_3_1"));
        assertEquals("yeah_4", properties.getProperty("secondary:third:att_3_2"));
        assertEquals("yeah_5", properties.getProperty("secondary:att_2"));
        assertEquals("yeah_6", properties.getProperty("root_1"));
        assertEquals("yeah_7", properties.getProperty("root_2"));
    }

    @Test
    public void testExtractMetadata_withDuplicateNamedElements() {
        final MetadataElement secondary = new MetadataElement("secondary");
        final MetadataElement third_1 = new MetadataElement("third");
        third_1.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("Wilhelm"), true));
        final MetadataElement third_2 = new MetadataElement("third");
        third_2.addAttribute(new MetadataAttribute("att_3_1", ProductData.ASCII.createInstance("Busch"), true));
        secondary.addElement(third_1);
        secondary.addElement(third_2);
        metadataRoot.addElement(secondary);

        final Properties properties = AbstractFormatExporter.extractMetadata(metadataRoot);
        assertEquals(2, properties.size());
        assertEquals("Wilhelm", properties.getProperty("secondary:third_0:att_3_1"));
        assertEquals("Busch", properties.getProperty("secondary:third_1:att_3_1"));
    }
}
