package org.esa.beam.smos.ee2netcdf.geometry;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binio.CompoundData;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class PolygonGeometryFilterTest {

    private PolygonGeometryFilter geometryFilter;

    @Before
    public void setUp() throws ConversionException {
        final JtsGeometryConverter jtsGeometryConverter = new JtsGeometryConverter();
        final Geometry geometry = jtsGeometryConverter.parse("POLYGON((10 0, 10 10, 20 10, 20 0, 10 0))");
        geometryFilter = new PolygonGeometryFilter(geometry);
    }

    @Test
    public void testInterfaceImplemented() {
        assertTrue(geometryFilter instanceof GeometryFilter);
    }

    @Test
    public void testAccept_insidePolygon() throws IOException {
        final CompoundData compoundData = mock(CompoundData.class);

        when(compoundData.getFloat(1)).thenReturn(0.01f);   // lat
        when(compoundData.getFloat(2)).thenReturn(10.02f);  // lon
        assertTrue(geometryFilter.accept(compoundData));

        when(compoundData.getFloat(1)).thenReturn(2.65f);   // lat
        when(compoundData.getFloat(2)).thenReturn(11.78f);  // lon
        assertTrue(geometryFilter.accept(compoundData));

        when(compoundData.getFloat(1)).thenReturn(9.98f);   // lat
        when(compoundData.getFloat(2)).thenReturn(19.78f);  // lon
        assertTrue(geometryFilter.accept(compoundData));
    }

    @Test
    public void testAccept_outsidePolygon() throws IOException {
        final CompoundData compoundData = mock(CompoundData.class);

        when(compoundData.getFloat(1)).thenReturn(-0.01f);   // lat
        when(compoundData.getFloat(2)).thenReturn(9.99f);  // lon
        assertFalse(geometryFilter.accept(compoundData));

        when(compoundData.getFloat(1)).thenReturn(10.01f);   // lat
        when(compoundData.getFloat(2)).thenReturn(20.02f);  // lon
        assertFalse(geometryFilter.accept(compoundData));

        when(compoundData.getFloat(1)).thenReturn(22.65f);   // lat
        when(compoundData.getFloat(2)).thenReturn(41.78f);  // lon
        assertFalse(geometryFilter.accept(compoundData));
    }
}
