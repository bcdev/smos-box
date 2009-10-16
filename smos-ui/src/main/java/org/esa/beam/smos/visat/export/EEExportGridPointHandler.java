package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.*;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

class EEExportGridPointHandler implements GridPointHandler {

    private final DataContext targetContext;
    private final GridPointFilter targetFilter;
    private final HashMap<Long, Date> snapshotIdTimeMap;
    private final TimeTracker timeTracker;

    private long gridPointCount;
    private long gridPointDataPosition;

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
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        if (gridPointCount == 0) {
            init(gridPointData.getParent());
        }
        if (targetFilter.accept(id, gridPointData)) {
            trackSensingTime(gridPointData);

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

    private void trackSensingTime(CompoundData gridPointData) throws IOException {
        final SequenceData btDataList = gridPointData.getSequence("BT_Data_List");
        final CompoundData btData = btDataList.getCompound(0);
        final int index = btData.getType().getMemberIndex("Snapshot_ID");
        if (index >= 0) {
            final long snapShotId = btData.getUInt(index);
            timeTracker.track(snapshotIdTimeMap.get(snapShotId));
        }
    }

    private void init(CollectionData parent) throws IOException {
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
