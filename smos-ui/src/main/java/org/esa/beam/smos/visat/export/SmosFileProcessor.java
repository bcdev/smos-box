package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosConstants;

import java.awt.Shape;
import java.io.IOException;

public class SmosFileProcessor {

    private final GridPointFilterStream filterStream;
    private final Shape targetRegion;

    public SmosFileProcessor(GridPointFilterStream filterStream, Shape targetRegion) {
        this.filterStream = filterStream;
        this.targetRegion = targetRegion;
    }

   public void process(SmosFile smosFile, ProgressMonitor pm) throws IOException {
        final CompoundType gridPointType = smosFile.getGridPointType();
        final int latIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        final int lonIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);

        filterStream.startFile(smosFile);
        final int gridPointCount = smosFile.getGridPointCount();

        try {
            pm.beginTask("Processing grid cells...", gridPointCount);
            for (int i = 0; i < gridPointCount; i++) {
                CompoundData gridPointData = smosFile.getGridPointData(i);
                double lat = gridPointData.getDouble(latIndex);
                double lon = gridPointData.getDouble(lonIndex);
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0) {
                    lon = lon - 360.0;
                }
                if (targetRegion.contains(lon, lat)) {
                    filterStream.handleGridPoint(i, gridPointData);
                }
                pm.worked(1);
                if (pm.isCanceled()) {
                    throw new IOException("Processing was cancelled by the user.");
                }
            }
        } finally {
            pm.done();
            filterStream.stopFile(smosFile);
        }
    }

}
