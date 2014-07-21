package org.esa.beam.smos.ee2netcdf.geometry;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GeometryFilterFactoryTest {

    @Test
    public void testCreate_noGeometrySupplied() {
        final GeometryFilter filter = GeometryFilterFactory.create(null);
        assertTrue(filter instanceof NoConstraintGeometryFilter);
    }
}
