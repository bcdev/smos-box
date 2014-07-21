package org.esa.beam.smos.ee2netcdf.geometry;


import com.bc.ceres.binio.CompoundData;

import java.io.IOException;

public interface GeometryFilter {

    public boolean accept(CompoundData compoundData) throws IOException;
}
