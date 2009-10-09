package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;

import java.io.IOException;

interface GridPointFilter {

    boolean accept(int id, CompoundData gridPointData) throws IOException;
}
