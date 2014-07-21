package org.esa.beam.smos.ee2netcdf.geometry;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryFilterFactory {

    public static GeometryFilter create(Geometry region) {
        return new NoConstraintGeometryFilter();
    }
}
