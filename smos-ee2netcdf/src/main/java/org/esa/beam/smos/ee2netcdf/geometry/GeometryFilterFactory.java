package org.esa.beam.smos.ee2netcdf.geometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryFilterFactory {

    public static GeometryFilter create(Geometry region) {
        if (region == null) {
            return new NoConstraintGeometryFilter();
        } else if (region instanceof Polygon) {
            return new PolygonGeometryFilter(region);
        }

        throw new IllegalArgumentException("Unsupported region geometry: " + region.toString());
    }
}
