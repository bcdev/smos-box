package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.smos.SmosFormats;

import java.awt.Shape;
import java.io.IOException;

class RegionalFilter implements GridPointFilter {

    private final Shape region;

    RegionalFilter(Shape region) {
        this.region = region;
    }

    @Override
    public boolean accept(int id, CompoundData gridPointData) throws IOException {
        final double lat = gridPointData.getDouble(SmosFormats.GRID_POINT_LAT_NAME);
        final double lon = gridPointData.getDouble(SmosFormats.GRID_POINT_LON_NAME);

        return region.contains(lon, lat);
    }
}
