package org.esa.beam.smos.ee2netcdf.geometry;


import com.bc.ceres.binio.CompoundData;

public interface GeometryFilter {

    public boolean accept(CompoundData compoundData);
}
