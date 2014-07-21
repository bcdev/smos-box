package org.esa.beam.smos.ee2netcdf.geometry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class NoConstraintGeometryFilterTest {

    private NoConstraintGeometryFilter geometryFilter;

    @Before
    public void setUp() {
        geometryFilter = new NoConstraintGeometryFilter();
    }

    @Test
    public void testInterfaceImplemented() {
        assertTrue(geometryFilter instanceof GeometryFilter);
    }

    @Test
    public void testAccept() {
        assertTrue(geometryFilter.accept(null));
    }
}
