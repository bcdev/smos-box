/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.core.Assert;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Represents a SMOS L1c Science product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since BEAM 4.2
 */
public class L1cScienceSmosFile extends L1cSmosFile implements SnapshotProvider {

    public static final float CENTER_BROWSE_INCIDENCE_ANGLE = 42.5f;
    public static final float MIN_BROWSE_INCIDENCE_ANGLE = 37.5f;
    public static final float MAX_BROWSE_INCIDENCE_ANGLE = 52.5f;
    public static final float INCIDENCE_ANGLE_FACTOR = 0.001373291f; // 90.0 / 2^16

    private final boolean fullPol;
    private final int flagsIndex;

    private final int incidenceAngleIndex;
    private final int snapshotIdOfPixelIndex;
    private final SequenceData snapshotList;

    private final CompoundType snapshotType;
    private volatile Future<Void> backgroundTask;
    private volatile SnapshotPolarisationMode polMode;

    public L1cScienceSmosFile(File file, DataFormat format, boolean fullPol) throws IOException {
        super(file, format);
        this.fullPol = fullPol;

        flagsIndex = getBtDataType().getMemberIndex(SmosFormats.BT_FLAGS_NAME);
        incidenceAngleIndex = this.btDataType.getMemberIndex(SmosFormats.BT_INCIDENCE_ANGLE_NAME);
        snapshotIdOfPixelIndex = btDataType.getMemberIndex(SmosFormats.BT_SNAPSHOT_ID_OF_PIXEL_NAME);

        snapshotList = getDataBlock().getSequence(SmosFormats.SNAPSHOT_LIST_NAME);
        if (snapshotList == null) {
            throw new IOException("Data block does not include snapshot list.");
        }
        snapshotType = (CompoundType) snapshotList.getSequenceType().getElementType();
    }

    public boolean isFullPol() {
        return fullPol;
    }

    public void startBackgroundInit() {
        if (!isBackgroundInitStarted()) {
            Callable<Void> callable = new Callable<Void>() {
                public Void call() throws IOException {
                    tabulateSnapshotIds();
                    return null;
                }
            };
            backgroundTask = Executors.newSingleThreadExecutor().submit(callable);
        }
    }

    public boolean isBackgroundInitStarted() {
        return backgroundTask != null;
    }

    public boolean isBackgoundInitDone() {
        return backgroundTask.isDone();
    }

    private void tabulateSnapshotIds() throws IOException {
        final SortedSet<Long> any = new TreeSet<Long>();
        final SortedSet<Long> x = new TreeSet<Long>();
        final SortedSet<Long> y = new TreeSet<Long>();
        final SortedSet<Long> xy = new TreeSet<Long>();
        
        final Map<Long, Rectangle2D> regions = new HashMap<Long, Rectangle2D>();
        final int latIndex = getGridPointType().getMemberIndex("Grid_Point_Latitude");
        final int lonIndex = getGridPointType().getMemberIndex("Grid_Point_Longitude");
        
        final SequenceData gridPointList = getGridPointList();
        final int gridPointCount = getGridPointCount();
        for (int i = 0; i < gridPointCount; i++) {
            final SequenceData btList = getBtDataList(i);

            final int btCount = btList.getElementCount();
            if (btCount > 0) {
                final CompoundData gridData = gridPointList.getCompound(i);
                float lon = gridData.getFloat(lonIndex);
                float lat = gridData.getFloat(latIndex);
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0f) {
                    lon -= 360.0f;
                }
                final Rectangle2D.Float rectangle =
                        new Rectangle2D.Float(lon - 0.02f, lat - 0.02f, 0.04f, 0.04f);
                
                long lastSid = -1;
                for (int j = 0; j < btCount; j++) {
                    final CompoundData btData = btList.getCompound(j);
                    final long sid = btData.getLong(snapshotIdOfPixelIndex);
                    final int flags = btData.getInt(flagsIndex);

                    switch (flags & SmosFormats.L1C_POL_FLAGS_MASK) {
                        case SmosFormats.L1C_POL_MODE_X:
                            x.add(sid);
                            break;
                        case SmosFormats.L1C_POL_MODE_Y:
                            y.add(sid);
                            break;
                        case SmosFormats.L1C_POL_MODE_XY1:
                            xy.add(sid);
                            break;
                        case SmosFormats.L1C_POL_MODE_XY2:
                            xy.add(sid);
                            break;
                    }
                    if (lastSid != sid) {
                        // snapshots are ordered
                        any.add(sid);
                        Rectangle2D region = regions.get(sid);
                        if (region == null) {
                            region = new Rectangle2D.Float();
                            region.setRect(rectangle);
                            regions.put(sid, region);
                        } else {
                            region.add(rectangle);
                        }
                        lastSid = sid;
                    }
                }
            }
        }
        Long[] snapshotIds = any.toArray(new Long[any.size()]);
        Long[] xPolSnapshotIds = x.toArray(new Long[x.size()]);
        Long[] yPolSnapshotIds = y.toArray(new Long[y.size()]);
        Long[] xyPolSnapshotIds = xy.toArray(new Long[xy.size()]);

        final Map<Long, Integer> snapshotIndexMap = new TreeMap<Long, Integer>();

        final int snapshotIdIndex = snapshotType.getMemberIndex(SmosFormats.SNAPSHOT_ID_NAME);
        final int snapshotCount = snapshotList.getElementCount();
        for (int i = 0; i < snapshotCount; i++) {
            final CompoundData snapshotData = getSnapshotData(i);
            final long sid = snapshotData.getLong(snapshotIdIndex);

            if (any.contains(sid)) {
                snapshotIndexMap.put(sid, i);
            }
        }

        polMode = new SnapshotPolarisationMode(snapshotIndexMap, snapshotIds, xPolSnapshotIds, yPolSnapshotIds,
                                               xyPolSnapshotIds, regions);
    }

    @Override
    public short getBrowseBtData(int gridPointIndex, int fieldIndex, int polMode,
                                 short noDataValue) throws IOException {
        if (fieldIndex == flagsIndex) {
            return (short) getCombinedBtFlags(gridPointIndex, polMode, noDataValue);
        } else {
            return (short) getInterpolatedBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
        }
    }

    @Override
    public int getBrowseBtData(int gridPointIndex, int fieldIndex, int polMode,
                               int noDataValue) throws IOException {
        if (fieldIndex == flagsIndex) {
            return getCombinedBtFlags(gridPointIndex, polMode, noDataValue);
        } else {
            return (int) getInterpolatedBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
        }
    }

    @Override
    public float getBrowseBtData(int gridPointIndex, int fieldIndex, int polMode,
                                 float noDataValue) throws IOException {
        return getInterpolatedBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
    }

    @Override
    public short getSnapshotBtData(int gridPointIndex, int fieldIndex, int polMode, long snapshotId,
                                   short noDataValue) throws IOException {
        final CompoundData btData = getSnapshotBtData(gridPointIndex, polMode, snapshotId);

        if (btData != null) {
            return btData.getShort(fieldIndex);
        }

        return noDataValue;
    }

    @Override
    public int getSnapshotBtData(int gridPointIndex, int fieldIndex, int polMode, long snapshotId,
                                 int noDataValue) throws IOException {
        final CompoundData btData = getSnapshotBtData(gridPointIndex, polMode, snapshotId);

        if (btData != null) {
            return btData.getInt(fieldIndex);
        }

        return noDataValue;
    }

    @Override
    public float getSnapshotBtData(int gridPointIndex, int fieldIndex, int polMode, long snapshotId,
                                   float noDataValue) throws IOException {
        final CompoundData btData = getSnapshotBtData(gridPointIndex, polMode, snapshotId);

        if (btData != null) {
            return btData.getFloat(fieldIndex);
        }

        return noDataValue;
    }

    private CompoundData getSnapshotBtData(int gridPointIndex, int polMode, long snapshotId) throws IOException {
        final SequenceData btDataList = getBtDataList(gridPointIndex);
        final int elementCount = btDataList.getElementCount();

        CompoundData btData = btDataList.getCompound(0);
        if (btData.getLong(snapshotIdOfPixelIndex) > snapshotId) {
            return null;
        }
        btData = btDataList.getCompound(elementCount - 1);
        if (btData.getLong(snapshotIdOfPixelIndex) < snapshotId) {
            return null;
        }
        for (int i = 0; i < elementCount; ++i) {
            btData = btDataList.getCompound(i);
            if (btData.getLong(snapshotIdOfPixelIndex) == snapshotId) {
                final int flags = btData.getInt(flagsIndex);
                if (polMode == (flags & 3) || (polMode & flags & 2) != 0) {
                    return btData;
                }
            }
        }

        return null;
    }

    private int getCombinedBtFlags(int gridPointIndex, int polMode, int noDataValue) throws IOException {
        final SequenceData btDataList = getBtDataList(gridPointIndex);
        final int elementCount = btDataList.getElementCount();

        int combinedFlags = 0;

        boolean hasLower = false;
        boolean hasUpper = false;

        for (int i = 0; i < elementCount; ++i) {
            final CompoundData btData = btDataList.getCompound(i);
            final int flags = btData.getInt(flagsIndex);

            if (polMode == (flags & 3) || (polMode & flags & 2) != 0) {
                final float incidenceAngle = INCIDENCE_ANGLE_FACTOR * btData.getInt(incidenceAngleIndex);

                if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                    combinedFlags |= flags;

                    if (!hasLower) {
                        hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                    }
                    if (!hasUpper) {
                        hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                    }
                }
            }
        }
        if (hasLower && hasUpper) {
            return combinedFlags;
        }

        return noDataValue;
    }

    private float getInterpolatedBtData(int gridPointIndex, int fieldIndex, int polMode,
                                        float noDataValue) throws IOException {
        final SequenceData btDataList = getBtDataList(gridPointIndex);
        final int elementCount = btDataList.getElementCount();

        int count = 0;
        float sx = 0;
        float sy = 0;
        float sxx = 0;
        float sxy = 0;

        boolean hasLower = false;
        boolean hasUpper = false;

        for (int i = 0; i < elementCount; ++i) {
            final CompoundData btData = btDataList.getCompound(i);
            final int flags = btData.getInt(flagsIndex);

            if (polMode == (flags & 3) || (polMode & flags & 2) != 0) {
                final float incidenceAngle = INCIDENCE_ANGLE_FACTOR * btData.getInt(incidenceAngleIndex);

                if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                    final float btValue = btData.getFloat(fieldIndex);

                    sx += incidenceAngle;
                    sy += btValue;
                    sxx += incidenceAngle * incidenceAngle;
                    sxy += incidenceAngle * btValue;
                    count++;

                    if (!hasLower) {
                        hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                    }
                    if (!hasUpper) {
                        hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                    }
                }
            }
        }
        if (hasLower && hasUpper) {
            final float a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
            final float b = (sy - a * sx) / count;
            return a * CENTER_BROWSE_INCIDENCE_ANGLE + b;
        }

        return noDataValue;
    }

    public final int getSnapshotIndex(long snapshotId) {
        final Map<Long, Integer> snapshotIndexMap = polMode.snapshotIndexMap;
        if (!snapshotIndexMap.containsKey(snapshotId)) {
            throw new IllegalArgumentException(MessageFormat.format("Illegal snapshot ID: {0}", snapshotId));
        }

        return snapshotIndexMap.get(snapshotId);
    }

    public final SequenceData getSnapshotList() {
        return snapshotList;
    }

    public final CompoundData getSnapshotData(int snapshotIndex) throws IOException {
        return snapshotList.getCompound(snapshotIndex);
    }

    public final CompoundType getSnapshotType() {
        return snapshotType;
    }
    
    private long getSnapshotId(SequenceData btDataList, int btDataIndex) throws IOException {
        Assert.argument(btDataList.getSequenceType().getElementType() == btDataType);
        return btDataList.getCompound(btDataIndex).getLong(snapshotIdOfPixelIndex);
    }

    @Override
    public final Long[] getAllSnapshotIds() {
        return polMode.snapshotIds;
    }

    @Override
    public final Long[] getXPolSnapshotIds() {
        return polMode.xPolSnapshotIds;
    }

    @Override
    public final Long[] getYPolSnapshotIds() {
        return polMode.yPolSnapshotIds;
    }

    @Override
    public final Long[] getCrossPolSnapshotIds() {
        return polMode.xyPolSnapshotIds;
    }
    
    public final Rectangle2D getSnapshotRegion(long snapshotId) {
        return polMode.regions.get(snapshotId);
    }

    class SnapshotPolarisationMode {

        private final Map<Long, Integer> snapshotIndexMap;
        private final Long[] snapshotIds;
        private final Long[] xPolSnapshotIds;
        private final Long[] yPolSnapshotIds;
        private final Long[] xyPolSnapshotIds;
        private final Map<Long, Rectangle2D> regions;

        SnapshotPolarisationMode(Map<Long, Integer> snapshotIndexMap, Long[] snapshotIds,
                                 Long[] xPolSnapshotIds,
                                 Long[] yPolSnapshotIds, Long[] xyPolSnapshotIds, Map<Long, Rectangle2D> regions) {
            this.snapshotIndexMap = Collections.unmodifiableMap(snapshotIndexMap);
            this.snapshotIds = snapshotIds;
            this.xPolSnapshotIds = xPolSnapshotIds;
            this.yPolSnapshotIds = yPolSnapshotIds;
            this.xyPolSnapshotIds = xyPolSnapshotIds;
            this.regions = Collections.unmodifiableMap(regions);
        }
    }
}
