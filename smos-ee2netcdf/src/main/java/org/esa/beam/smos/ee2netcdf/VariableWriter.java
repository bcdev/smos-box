package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;

import java.io.IOException;

interface VariableWriter {

    void write(CompoundData gridPointData, SequenceData btDataList, int index) throws IOException;

    void close() throws IOException;
}
