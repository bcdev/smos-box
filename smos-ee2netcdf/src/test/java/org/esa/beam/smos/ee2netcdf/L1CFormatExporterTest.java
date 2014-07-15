package org.esa.beam.smos.ee2netcdf;


import org.junit.Test;

import java.awt.*;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class L1CFormatExporterTest {

    @Test
    public void testExtractDimensions() {
        final HashMap<String, Integer> dimensionMap = new HashMap<>();
        dimensionMap.put("width", 34);
        dimensionMap.put("height", 17);

        final Dimension dimension = L1CFormatExporter.extractDimensions("width height", dimensionMap);
        assertNotNull(dimension);
        assertEquals(34, dimension.width);
        assertEquals(17, dimension.height);
    }

    @Test
    public void testExtractDimensions_noHeight() {
        final HashMap<String, Integer> dimensionMap = new HashMap<>();
        dimensionMap.put("width", 34);
        dimensionMap.put("height", 17);

        final Dimension dimension = L1CFormatExporter.extractDimensions("width", dimensionMap);
        assertNotNull(dimension);
        assertEquals(34, dimension.width);
        assertEquals(-1, dimension.height);
    }
}
