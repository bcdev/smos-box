package org.esa.beam.smos.ee2netcdf;


import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
        // @todo 1 tb /tb continue here tb 2014-07-01
    }
}
