/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.SmosFile;

import java.awt.Shape;
import java.io.IOException;
import java.text.MessageFormat;

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

        pm.beginTask(MessageFormat.format(
                "Processing file ''{0}''...", smosFile.getDblFile().getName()), gridPointCount);
        try {
            for (int i = 0; i < gridPointCount; i++) {
                final CompoundData gridPointData = smosFile.getGridPointData(i);
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
            filterStream.stopFile(smosFile);
            pm.done();
        }
    }
}
