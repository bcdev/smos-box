package org.esa.beam.smos.ee2netcdf.geometry;

import com.bc.ceres.binio.CompoundData;

class NoConstraintGeometryFilter implements GeometryFilter{

    @Override
    public boolean accept(CompoundData compoundData) {
        return true;
    }
}
