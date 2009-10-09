package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;

import java.io.IOException;

interface GridPointHandler {

    void handleGridPoint(int id, CompoundData gridPointData) throws IOException;
}
