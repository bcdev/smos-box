package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.*;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;
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
    private boolean isL2File;

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

        final String fomatName = targetContext.getFormat().getName();
        // @todo 2 tb/tb extend to L2 -DA products once they're supported
        isL2File = SmosProductReader.is_L2_User_File(fomatName);
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        if (gridPointCount == 0) {
            init(gridPointData);
        }
        if (targetFilter.accept(id, gridPointData)) {
            trackSensingTime(gridPointData);
            trackGeometry(gridPointData);

            targetContext.getData().setLong(SmosFormats.GRID_POINT_COUNTER_NAME, ++gridPointCount);
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
        if (isL2File) {
            throw new IllegalStateException("Currently not implemented - waiting for ESA input");
//            int index = gridPointData.getType().getMemberIndex("Mean_acq_time");
//            final CompoundData utcData = gridPointData.getCompound(index);
//            final int days = utcData.getInt(0);
//            final long seconds = utcData.getUInt(1);
//            final long microSeconds = utcData.getUInt(2);
//            timeTracker.track(SmosFile.getCfiDateInUtc(days, seconds, microSeconds));
        } else {
            int index = gridPointData.getType().getMemberIndex("BT_Data_List");
            final SequenceData btDataList = gridPointData.getSequence(index);
            final CompoundData btData = btDataList.getCompound(0);
            index = btData.getType().getMemberIndex("Snapshot_ID");
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
        latIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LAT_NAME);
        lonIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LON_NAME);

        final CollectionData parent = gridPointData.getParent();
        final long parentPosition = parent.getPosition();
        copySnapshotData(parent, parentPosition);

        createSnapshotIdMap(parent);

        targetContext.getData().setLong(SmosFormats.GRID_POINT_COUNTER_NAME, 0);
        targetContext.getData().flush();

        gridPointDataPosition = parentPosition;
    }

    private void createSnapshotIdMap(CollectionData parent) throws IOException {
        final DataContext context = parent.getContext();
        final SequenceData snapShotData;
        try {
            snapShotData = context.getData().getSequence(SmosFormats.SNAPSHOT_LIST_NAME);
        } catch (DataAccessException e) {
            return; // we have a browse product
            // but this procedure is not really cool, better ask if the seqzuence is present
        }

        final int numSnapshots = snapShotData.getElementCount();
        for (int i = 0; i < numSnapshots; i++) {
            final CompoundData snapShot = snapShotData.getCompound(i);
            final CompoundData utcData = snapShot.getCompound(0);
            final int days = utcData.getInt(0);
            final long seconds = utcData.getUInt(1);
            final long microSeconds = utcData.getUInt(2);
            final Date snapShotTime = SmosFile.getCfiDateInUtc(days, seconds, microSeconds);
            final long snapShotId = snapShot.getUInt(1);

            snapshotIdTimeMap.put(snapShotId, snapShotTime);
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
