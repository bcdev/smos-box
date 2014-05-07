package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;

import java.io.IOException;

public interface GridPointList extends PointList {

    CompoundData getCompound(int i) throws IOException;

    CompoundType getCompoundType();

}
