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

import java.io.IOException;
import java.text.MessageFormat;

public class SmosFileProcessor {

    private final GridPointFilterStream filterStream;
    private final GridPointFilter gridPointFilter;

    public SmosFileProcessor(GridPointFilterStream filterStream, GridPointFilter gridPointFilter) {
        this.filterStream = filterStream;
        this.gridPointFilter = gridPointFilter;
    }

    public void process(SmosFile smosFile, ProgressMonitor pm) throws IOException {
        final CompoundType gridPointType = smosFile.getGridPointType();
        final int idIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_ID_NAME);

        filterStream.startFile(smosFile);
        final int gridPointCount = smosFile.getGridPointCount();

        pm.beginTask(MessageFormat.format(
                "Processing file ''{0}''...", smosFile.getDataFile().getName()), gridPointCount);
        try {
            for (int i = 0; i < gridPointCount; i++) {
                final CompoundData gridPointData = smosFile.getGridPointData(i);
                final int id = gridPointData.getInt(idIndex);
                if (gridPointFilter.accept(id, gridPointData)) {
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
