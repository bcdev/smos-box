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

import com.bc.ceres.binio.CollectionData;
import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

class EEExportGridPointHandler implements GridPointHandler {

    private final DataContext targetContext;
    private final GridPointFilter targetFilter;
    private final HashMap<Long, Date> snapshotIdTimeMap;
    private final TimeTracker timeTracker;
    private final GeometryTracker geometryTracker;
    private boolean level2;

    private long gridPointCount;
    private long gridPointDataPosition;
    private int latIndex;
    private int lonIndex;

    EEExportGridPointHandler(DataContext targetContext) {
        this(targetContext, new GridPointFilter() {
            @Override
            public boolean accept(int id, CompoundData gridPointData) throws IOException {
                return true;
            }
        });
    }

    EEExportGridPointHandler(DataContext targetContext, GridPointFilter targetFilter) {
        this.targetContext = targetContext;
        this.targetFilter = targetFilter;
        snapshotIdTimeMap = new HashMap<Long, Date>();
        timeTracker = new TimeTracker();
        geometryTracker = new GeometryTracker();

        final String formatName = targetContext.getFormat().getName();
        level2 = SmosProductReader.isSmUserFormat(formatName) ||
                 SmosProductReader.isSmAnalysisFormat(formatName) ||
                 SmosProductReader.isOsUserFormat(formatName) ||
                 SmosProductReader.isOsAnalysisFormat(formatName);
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        if (gridPointCount == 0) {
            init(gridPointData);
        }
        if (targetFilter.accept(id, gridPointData)) {
            trackSensingTime(gridPointData);
            trackGeometry(gridPointData);

            targetContext.getData().setLong(SmosConstants.GRID_POINT_COUNTER_NAME, ++gridPointCount);
            // ATTENTION: flush must occur <em>before</em> grid point data is written (rq-20091008)
            targetContext.getData().flush();

            gridPointData.resolveSize();
            final long size = gridPointData.getSize();
            final byte[] bytes = new byte[(int) size];

            get(gridPointData, bytes);
            put(targetContext, bytes, gridPointDataPosition);
            gridPointDataPosition += size;
        }
    }

    boolean hasValidPeriod() {
        return timeTracker.hasValidPeriod();
    }

    Date getSensingStart() {
        return timeTracker.getIntervalStart();
    }

    Date getSensingStop() {
        return timeTracker.getIntervalStop();
    }

    boolean hasValidArea() {
        return geometryTracker.hasValidArea();
    }

    Rectangle2D getArea() {
        return geometryTracker.getArea();
    }

    long getGridPointCount() {
        return gridPointCount;
    }

    private void trackSensingTime(CompoundData gridPointData) throws IOException {
        final CompoundType type = gridPointData.getType();
        final String typeName = type.getName();
        if (typeName.contains("ECMWF")) {
            return; // no sensing time information in ECMWF auxiliary files
        }
        if (level2) {
            int index = gridPointData.getType().getMemberIndex("Mean_acq_time");
            if (index < 0) {
                return; // we have a data analysis product - no timing information stored in there
            }
            final float mjdTime = gridPointData.getFloat(index);
            if (mjdTime > 0) {  // condition for valid measurement
                final Date date = SmosFile.mjdFloatDateToUtc(mjdTime);
                timeTracker.track(date);
            }
        } else {
            int index = type.getMemberIndex(SmosConstants.BT_DATA_LIST_NAME);
            final SequenceData btDataList = gridPointData.getSequence(index);
            final CompoundData btData = btDataList.getCompound(0);
            index = btData.getType().getMemberIndex(SmosConstants.BT_SNAPSHOT_ID_OF_PIXEL_NAME);
            if (index >= 0) {
                final long snapShotId = btData.getUInt(index);
                timeTracker.track(snapshotIdTimeMap.get(snapShotId));
            }
        }
    }

    private void trackGeometry(CompoundData gridPointData) throws IOException {
        double lat = gridPointData.getDouble(latIndex);
        double lon = gridPointData.getDouble(lonIndex);
        // normalisation to [-180, 180] necessary for some L1c test products
        if (lon > 180.0) {
            lon = lon - 360.0;
        }
        geometryTracker.add(new Point2D.Double(lon, lat));
    }

    private void init(CompoundData gridPointData) throws IOException {
        final CompoundType gridPointType = gridPointData.getType();
        latIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        lonIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);

        final CollectionData parent = gridPointData.getParent();
        final long parentPosition = parent.getPosition();
        copySnapshotData(parent, parentPosition);

        createSnapshotIdMap(parent);

        targetContext.getData().setLong(SmosConstants.GRID_POINT_COUNTER_NAME, 0);
        targetContext.getData().flush();

        gridPointDataPosition = parentPosition;
    }

    private void createSnapshotIdMap(CollectionData parent) throws IOException {
        final DataContext context = parent.getContext();
        final int snapshotListIndex = context.getData().getMemberIndex(SmosConstants.SNAPSHOT_LIST_NAME);
        if (snapshotListIndex == -1) {
            return; // we have a browse product
        }
        final SequenceData snapshotData = context.getData().getSequence(snapshotListIndex);
        final int snapshotCount = snapshotData.getElementCount();
        for (int i = 0; i < snapshotCount; i++) {
            final CompoundData snapshot = snapshotData.getCompound(i);
            final CompoundData utcData = snapshot.getCompound(0);
            final int days = utcData.getInt(0);
            final long seconds = utcData.getUInt(1);
            final long microSeconds = utcData.getUInt(2);
            final Date snapshotTime = SmosFile.cfiDateToUtc(days, seconds, microSeconds);
            final long snapshotId = snapshot.getUInt(1);

            snapshotIdTimeMap.put(snapshotId, snapshotTime);
        }
    }

    private void copySnapshotData(CollectionData parent, long parentPosition) throws IOException {
        copyBytes(parent.getContext(), targetContext, 0, parentPosition);
    }

    private static void copyBytes(DataContext sourceContext,
                                  DataContext targetContext, long from, long to) throws IOException {
        final int segmentSize = 16384;
        byte[] bytes = new byte[segmentSize];

        for (long pos = from; pos < to; pos += segmentSize) {
            final long remainderSize = to - pos;
            if (remainderSize < segmentSize) {
                bytes = new byte[(int) remainderSize];
            }

            get(sourceContext, bytes, pos);
            put(targetContext, bytes, pos);
        }
    }

    private static void get(CompoundData compoundData, byte[] bytes) throws IOException {
        compoundData.getContext().getHandler().read(compoundData.getContext(), bytes, compoundData.getPosition());
    }

    private static void get(DataContext sourceContext, byte[] bytes, long position) throws IOException {
        sourceContext.getHandler().read(sourceContext, bytes, position);
    }

    private static void put(DataContext targetContext, byte[] bytes, long position) throws IOException {
        targetContext.getHandler().write(targetContext, bytes, position);
    }
}
