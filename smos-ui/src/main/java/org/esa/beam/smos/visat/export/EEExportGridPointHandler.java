package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CollectionData;
import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import org.esa.beam.dataio.smos.SmosFormats;

import java.io.IOException;

class EEExportGridPointHandler implements GridPointHandler {

    private final DataContext targetContext;
    private final GridPointFilter targetFilter;

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
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        // @todo 1 tb/tb track start and stop times - NOT POSSIBLE for BWSD1C, BWSF1C, BWLD1C and BWLF1C
        if (gridPointCount == 0) {
            init(gridPointData.getParent());
        }
        if (targetFilter.accept(id, gridPointData)) {
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

    private void init(CollectionData parent) throws IOException {
        final long parentPosition = parent.getPosition();
        copySnapshotData(parent, parentPosition);

        targetContext.getData().setLong(SmosFormats.GRID_POINT_COUNTER_NAME, 0);
        targetContext.getData().flush();

        gridPointDataPosition = parentPosition;
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
