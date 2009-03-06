package org.esa.beam.smos.visat;

import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.CompoundData;

import java.io.IOException;

public interface DataProvider {

    CompoundType getBtDataType();

    SequenceData getBtDataList(int gridPointId) throws IOException;

    boolean isScience();

    boolean isFullPol();

    CompoundType getSnapshotInfoType();

    CompoundData getSnapshotInfo(int snapshotId) throws IOException;

    // additional method for level 2 grid points
}
