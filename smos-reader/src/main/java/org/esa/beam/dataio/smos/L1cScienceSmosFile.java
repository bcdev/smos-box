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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Represents a SMOS L1c Science product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class L1cScienceSmosFile extends L1cSmosFile {

    private static final float CENTER_BROWSE_INCIDENCE_ANGLE = 42.5f;
    private static final float MIN_BROWSE_INCIDENCE_ANGLE = 37.5f;
    private static final float MAX_BROWSE_INCIDENCE_ANGLE = 52.5f;
    private static final float INCIDENCE_ANGLE_FACTOR = 0.001373291f; // 90.0 / 2^16

    private final boolean fullPol;
    private final int flagsIndex;

    private final int incidenceAngleIndex;
    private final int snapshotIdOfPixelIndex;
    private final SequenceData snapshotList;
    private final CompoundType snapshotType;

    private volatile Future<SnapshotInfo> snapshotInfoFuture;

    public L1cScienceSmosFile(File hdrFile, File dblFile, DataFormat format, boolean fullPol) throws IOException {
        super(hdrFile, dblFile, format);
        this.fullPol = fullPol;

        flagsIndex = getBtDataType().getMemberIndex(SmosConstants.BT_FLAGS_NAME);
        incidenceAngleIndex = this.btDataType.getMemberIndex(SmosConstants.BT_INCIDENCE_ANGLE_NAME);
        snapshotIdOfPixelIndex = btDataType.getMemberIndex(SmosConstants.BT_SNAPSHOT_ID_OF_PIXEL_NAME);

        snapshotList = getDataBlock().getSequence(SmosConstants.SNAPSHOT_LIST_NAME);
        if (snapshotList == null) {
            throw new IOException("Data block does not include snapshot list.");
        }
        snapshotType = (CompoundType) snapshotList.getType().getElementType();
    }

    public boolean isFullPol() {
        return fullPol;
    }

    @Override
    public byte getBrowseBtData(int gridPointIndex, int fieldIndex, int polMode, byte noDataValue) throws IOException {
        if (fieldIndex == flagsIndex) {
            return (byte) getCombinedBtFlags(gridPointIndex, polMode, noDataValue);
        } else {
            return (byte) getInterpolatedBtData(gridPointIndex, fieldIndex, polMode, noDataValue);
        }
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
    public byte getSnapshotBtData(int gridPointIndex, int fieldIndex, int polMode, long snapshotId,
                                  byte noDataValue) throws IOException {
        final CompoundData btData = getSnapshotBtData(gridPointIndex, polMode, snapshotId);

        if (btData != null) {
            return btData.getByte(fieldIndex);
        }

        return noDataValue;
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

    public final SequenceData getSnapshotList() {
        return snapshotList;
    }

    public final CompoundData getSnapshotData(int snapshotIndex) throws IOException {
        return snapshotList.getCompound(snapshotIndex);
    }

    public boolean hasSnapshotInfo() {
        return getSnapshotInfoFuture().isDone();
    }

    public SnapshotInfo getSnapshotInfo() {
        try {
            return getSnapshotInfoFuture().get();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
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
                if (polMode == SmosConstants.L1C_POL_MODE_ANY || polMode == (flags & 3) || (polMode & flags & 2) != 0) {
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

            if (polMode == SmosConstants.L1C_POL_MODE_ANY || polMode == (flags & 3) || (polMode & flags & 2) != 0) {
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

            if (polMode == SmosConstants.L1C_POL_MODE_ANY || polMode == (flags & 3) || (polMode & flags & 2) != 0) {
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

    private Future<SnapshotInfo> getSnapshotInfoFuture() {
        if (snapshotInfoFuture == null) {
            synchronized (this) {
                if (snapshotInfoFuture == null) {
                    snapshotInfoFuture = Executors.newSingleThreadExecutor().submit(new Callable<SnapshotInfo>() {
                        @Override
                        public SnapshotInfo call() throws IOException {
                            return createSnapshotInfo();
                        }
                    });
                }
            }
        }

        return snapshotInfoFuture;
    }

    private SnapshotInfo createSnapshotInfo() throws IOException {
        final Set<Long> all = new TreeSet<Long>();
        final Set<Long> x = new TreeSet<Long>();
        final Set<Long> y = new TreeSet<Long>();
        final Set<Long> xy = new TreeSet<Long>();

        final Map<Long, Rectangle2D> snapshotRegionMap = new TreeMap<Long, Rectangle2D>();
        final int latIndex = getGridPointType().getMemberIndex("Grid_Point_Latitude");
        final int lonIndex = getGridPointType().getMemberIndex("Grid_Point_Longitude");

        final SequenceData gridPointList = getGridPointList();
        final int gridPointCount = getGridPointCount();
        for (int i = 0; i < gridPointCount; i++) {
            final SequenceData btList = getBtDataList(i);

            final int btCount = btList.getElementCount();
            if (btCount > 0) {
                final CompoundData gridData = gridPointList.getCompound(i);
                double lon = gridData.getDouble(lonIndex);
                double lat = gridData.getDouble(latIndex);
                lon -= 0.02;
                if (lon < -180.0) {
                    lon = -180.0;
                }
                lat += 0.02;
                if (lat > 90.0) {
                    lat = 90.0;
                }
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0) {
                    lon -= 360.0;
                }
                final Rectangle2D.Double rectangle = new Rectangle2D.Double(lon, lat, 0.04, 0.04);

                long lastSid = -1;
                for (int j = 0; j < btCount; j++) {
                    final CompoundData btData = btList.getCompound(j);
                    final long sid = btData.getLong(snapshotIdOfPixelIndex);

                    if (lastSid != sid) { // snapshots are ordered
                        all.add(sid);
                        if (snapshotRegionMap.containsKey(sid)) {
                            // todo: rq/rq - snapshots on the anti-meridian (2009-10-22)
                            snapshotRegionMap.get(sid).add(rectangle);
                        } else {
                            snapshotRegionMap.put(sid, rectangle);
                        }
                        lastSid = sid;
                    }

                    final int flags = btData.getInt(flagsIndex);
                    switch (flags & SmosConstants.L1C_POL_FLAGS_MASK) {
                    case SmosConstants.L1C_POL_MODE_X:
                        x.add(sid);
                        break;
                    case SmosConstants.L1C_POL_MODE_Y:
                        y.add(sid);
                        break;
                    case SmosConstants.L1C_POL_MODE_XY1:
                        xy.add(sid);
                        break;
                    case SmosConstants.L1C_POL_MODE_XY2:
                        xy.add(sid);
                        break;
                    }
                }
            }
        }
        final Map<Long, Integer> snapshotIndexMap = new TreeMap<Long, Integer>();

        final int snapshotIdIndex = snapshotType.getMemberIndex(SmosConstants.SNAPSHOT_ID_NAME);
        final int snapshotCount = snapshotList.getElementCount();
        for (int i = 0; i < snapshotCount; i++) {
            final CompoundData snapshotData = getSnapshotData(i);
            final long sid = snapshotData.getLong(snapshotIdIndex);

            if (all.contains(sid)) {
                snapshotIndexMap.put(sid, i);
            }
        }

        return new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotRegionMap);
    }

}
