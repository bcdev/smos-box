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
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
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

    private static final String INCIDENCE_ANGLE_NAME = "Incidence_Angle";
    private static final String SNAPSHOT_ID_OF_PIXEL_NAME = "Snapshot_ID_of_Pixel";

    private static final double CENTER_BROWSE_INCIDENCE_ANGLE = 42.5;
    private static final double MIN_BROWSE_INCIDENCE_ANGLE = 37.5;
    private static final double MAX_BROWSE_INCIDENCE_ANGLE = 52.5;

    private final Map<String, ValueProvider> valueProviderMap = new HashMap<String, ValueProvider>(17);
    private final int flagsIndex;
    private final int incidenceAngleIndex;
    private final int snapshotIdOfPixelIndex;
    private final double incidenceAngleScalingFactor;
    private final SequenceData snapshotList;
    private final CompoundType snapshotType;

    private volatile Future<SnapshotInfo> snapshotInfoFuture;

    L1cScienceSmosFile(File hdrFile, File dblFile, DataFormat format) throws IOException {
        super(hdrFile, dblFile, format);

        flagsIndex = getBtDataType().getMemberIndex(SmosConstants.BT_FLAGS_NAME);
        incidenceAngleIndex = getBtDataType().getMemberIndex(INCIDENCE_ANGLE_NAME);
        final Family<BandDescriptor> bandDescriptors = Dddb.getInstance().getBandDescriptors(format.getName());
        if (bandDescriptors == null) {
            throw new IOException(MessageFormat.format(
                    "No band descriptors found for format ''{0}''.", format.getName()));
        }
        incidenceAngleScalingFactor = getIncidenceAngleScalingFactor(bandDescriptors);
        snapshotIdOfPixelIndex = getBtDataType().getMemberIndex(SNAPSHOT_ID_OF_PIXEL_NAME);

        snapshotList = getDataBlock().getSequence(SmosConstants.SNAPSHOT_LIST_NAME);
        if (snapshotList == null) {
            throw new IOException("Data block does not include snapshot list.");
        }
        snapshotType = (CompoundType) snapshotList.getType().getElementType();
    }

    private double getIncidenceAngleScalingFactor(Family<BandDescriptor> descriptors) {
        for (final BandDescriptor descriptor : descriptors.asList()) {
            if (INCIDENCE_ANGLE_NAME.equals(descriptor.getMemberName())) {
                return descriptor.getScalingFactor();
            }
        }
        throw new IllegalStateException("No incidence angle scaling factor found.");
    }

    @Override
    public void close() {
        valueProviderMap.clear();
        super.close();
    }

    @Override
    protected void addBands(Product product) {
        super.addBands(product);

        if (SmosProductReader.isDualPolScienceFormat(getDataFormat().getName())) {
            addRotatedDualPolBands(product, valueProviderMap);
        } else {
            addRotatedFullPolBands(product, valueProviderMap);
        }
    }

    @Override
    protected final void addBand(Product product, BandDescriptor descriptor) {
        if (descriptor.getPolarization() < 0) {
            super.addBand(product, descriptor);
        } else {
            addBand(product, descriptor, getBtDataType());
        }
    }

    @Override
    protected final ValueProvider createValueProvider(BandDescriptor descriptor) {
        final int polarization = descriptor.getPolarization();
        if (polarization < 0) {
            return super.createValueProvider(descriptor);
        }
        final int memberIndex = getBtDataType().getMemberIndex(descriptor.getMemberName());
        final ValueProvider valueProvider = new L1cScienceValueProvider(this, memberIndex, polarization);
        valueProviderMap.put(descriptor.getBandName(), valueProvider);

        return valueProvider;
    }

    @Override
    protected final MultiLevelImage createSourceImage(Band band, ValueProvider valueProvider) {
        // todo - make source image reset itself and fire node-data-changed, if affected by snapshot ID (rq-20100121)
        return super.createSourceImage(band, valueProvider);
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

    byte getBrowseBtDataValueByte(int gridPointIndex, int memberIndex, int polarization,
                                  byte noDataValue) throws IOException {
        if (memberIndex == flagsIndex) {
            return (byte) getCombinedFlags(gridPointIndex, polarization, noDataValue);
        } else {
            return (byte) getInterpolatedValue(gridPointIndex, memberIndex, polarization, noDataValue);
        }
    }

    short getBrowseBtDataValueShort(int gridPointIndex, int memberIndex, int polarization,
                                    short noDataValue) throws IOException {
        if (memberIndex == flagsIndex) {
            return (short) getCombinedFlags(gridPointIndex, polarization, noDataValue);
        } else {
            return (short) getInterpolatedValue(gridPointIndex, memberIndex, polarization, noDataValue);
        }
    }

    int getBrowseBtDataValueInt(int gridPointIndex, int memberIndex, int polarization,
                                int noDataValue) throws IOException {
        if (memberIndex == flagsIndex) {
            return getCombinedFlags(gridPointIndex, polarization, noDataValue);
        } else {
            return (int) getInterpolatedValue(gridPointIndex, memberIndex, polarization, noDataValue);
        }
    }

    float getBrowseBtDataValueFloat(int gridPointIndex, int memberIndex, int polarization,
                                    float noDataValue) throws IOException {
        return (float) getInterpolatedValue(gridPointIndex, memberIndex, polarization, noDataValue);
    }

    CompoundData getSnapshotBtData(int gridPointIndex, int polarization, long snapshotId) throws IOException {
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
                if (polarization == 4 || polarization == (flags & 1) || (polarization & flags & 2) != 0) {
                    return btData;
                }
            }
        }

        return null;
    }

    private double getInterpolatedValue(int gridPointIndex, int memberIndex, int polarization,
                                        double noDataValue) throws IOException {
        final SequenceData btDataList = getBtDataList(gridPointIndex);
        final int elementCount = btDataList.getElementCount();

        int count = 0;
        double sx = 0;
        double sy = 0;
        double sxx = 0;
        double sxy = 0;

        boolean hasLower = false;
        boolean hasUpper = false;

        for (int i = 0; i < elementCount; ++i) {
            final CompoundData btData = btDataList.getCompound(i);
            final int flags = btData.getInt(flagsIndex);

            if (polarization == 4 || polarization == (flags & 1) || (polarization & flags & 2) != 0) {
                final double incidenceAngle = incidenceAngleScalingFactor * btData.getInt(incidenceAngleIndex);

                if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                    final float value = btData.getFloat(memberIndex);

                    sx += incidenceAngle;
                    sy += value;
                    sxx += incidenceAngle * incidenceAngle;
                    sxy += incidenceAngle * value;
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
            final double a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
            final double b = (sy - a * sx) / count;
            return a * CENTER_BROWSE_INCIDENCE_ANGLE + b;
        }

        return noDataValue;
    }

    private int getCombinedFlags(int gridPointIndex, int polMode, int noDataValue) throws IOException {
        final SequenceData btDataList = getBtDataList(gridPointIndex);
        final int elementCount = btDataList.getElementCount();

        int combinedFlags = 0;

        boolean hasLower = false;
        boolean hasUpper = false;

        for (int i = 0; i < elementCount; ++i) {
            final CompoundData btData = btDataList.getCompound(i);
            final int flags = btData.getInt(flagsIndex);

            if (polMode == 4 || polMode == (flags & 3) || (polMode & flags & 2) != 0) {
                final double incidenceAngle = incidenceAngleScalingFactor * btData.getInt(incidenceAngleIndex);

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

        final Map<Long, Rectangle2D> snapshotAreaMap = new TreeMap<Long, Rectangle2D>();
        final int latIndex = getGridPointType().getMemberIndex("Latitude");
        final int lonIndex = getGridPointType().getMemberIndex("Longitude");

        final SequenceData gridPointList = getGridPointList();
        final int gridPointCount = getGridPointCount();

        for (int i = 0; i < gridPointCount; i++) {
            final SequenceData btList = getBtDataList(i);
            final int btCount = btList.getElementCount();

            if (btCount > 0) {
                final CompoundData gridData = gridPointList.getCompound(i);
                double lon = gridData.getDouble(lonIndex);
                double lat = gridData.getDouble(latIndex);
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0) {
                    lon -= 360.0;
                }
                final Rectangle2D rectangle = createGridPointRectangle(lon, lat);

                long lastId = -1;
                for (int j = 0; j < btCount; j++) {
                    final CompoundData btData = btList.getCompound(j);
                    final long id = btData.getLong(snapshotIdOfPixelIndex);

                    if (lastId != id) { // snapshots are ordered
                        all.add(id);
                        if (snapshotAreaMap.containsKey(id)) {
                            // todo: rq/rq - snapshots on the anti-meridian, use area instead of rectangle (2009-10-22)
                            snapshotAreaMap.get(id).add(rectangle);
                        } else {
                            snapshotAreaMap.put(id, rectangle);
                        }
                        lastId = id;
                    }

                    final int flags = btData.getInt(flagsIndex);
                    switch (flags & SmosConstants.L1C_POL_MODE_FLAGS_MASK) {
                    case SmosConstants.L1C_POL_MODE_X:
                        x.add(id);
                        break;
                    case SmosConstants.L1C_POL_MODE_Y:
                        y.add(id);
                        break;
                    case SmosConstants.L1C_POL_MODE_XY1:
                    case SmosConstants.L1C_POL_MODE_XY2:
                        xy.add(id);
                        break;
                    }
                }
            }
        }

        final Map<Long, Integer> snapshotIndexMap = new TreeMap<Long, Integer>();
        final int snapshotIdIndex = snapshotType.getMemberIndex(SmosConstants.SNAPSHOT_ID_NAME);
        final int snapshotCount = snapshotList.getElementCount();

        for (int i = 0; i < snapshotCount; i++) {
            final long id = getSnapshotData(i).getLong(snapshotIdIndex);
            if (all.contains(id)) {
                snapshotIndexMap.put(id, i);
            }
        }

        return new SnapshotInfo(snapshotIndexMap, all, x, y, xy, snapshotAreaMap);
    }

    private void addRotatedDualPolBands(Product product, Map<String, ValueProvider> valueProviderMap) {
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(getDataFormat().getName());

        DP vp;
        BandDescriptor descriptor;

        vp = new DPH(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, vp);

        vp = new DPV(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, vp);

        vp = new DPH(product, valueProviderMap, true);
        descriptor = descriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, vp);

        vp = new DPV(product, valueProviderMap, true);
        descriptor = descriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, vp);

        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_1"), "(BT_Value_H + BT_Value_V) / 2.0");
        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_2"), "(BT_Value_H - BT_Value_V) / 2.0");
    }

    private void addRotatedFullPolBands(Product product, Map<String, ValueProvider> valueProviderMap) {
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(getDataFormat().getName());

        FP vp;
        BandDescriptor descriptor;

        vp = new FPH(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_H");
        addRotatedBand(product, descriptor, vp);

        vp = new FPV(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_V");
        addRotatedBand(product, descriptor, vp);

        vp = new FPHVR(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_HV_Real");
        addRotatedBand(product, descriptor, vp);

        vp = new FPHVI(product, valueProviderMap, false);
        descriptor = descriptors.getMember("BT_Value_HV_Imag");
        addRotatedBand(product, descriptor, vp);

        vp = new FPH(product, valueProviderMap, true);
        descriptor = descriptors.getMember("Pixel_Radiometric_Accuracy_H");
        addRotatedBand(product, descriptor, vp);

        vp = new FPV(product, valueProviderMap, true);
        descriptor = descriptors.getMember("Pixel_Radiometric_Accuracy_V");
        addRotatedBand(product, descriptor, vp);

        vp = new FPHVR(product, valueProviderMap, true);
        descriptor = descriptors.getMember("Pixel_Radiometric_Accuracy_HV");
        addRotatedBand(product, descriptor, vp);

        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_1"), "(BT_Value_H + BT_Value_V) / 2.0");
        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_2"), "(BT_Value_H - BT_Value_V) / 2.0");
        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_3"), "BT_Value_HV_Real");
        ProductHelper.addVirtualBand(product, descriptors.getMember("Stokes_4"), "BT_Value_HV_Imag");
    }

    private Band addRotatedBand(Product product, BandDescriptor descriptor, ValueProvider valueProvider) {
        final Band band = product.addBand(descriptor.getBandName(), ProductData.TYPE_FLOAT32);

        band.setUnit(descriptor.getUnit());
        band.setDescription(descriptor.getDescription());

        if (descriptor.hasFillValue()) {
            band.setNoDataValueUsed(true);
            band.setNoDataValue(descriptor.getFillValue());
        }
        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));

        return band;
    }

    private static Rectangle2D createGridPointRectangle(double lon, double lat) {
        // the average width of a grid point is about 0.04
        lon -= 0.02;
        if (lon < -180.0) {
            lon = -180.0;
        } else if (lon + 0.04 > 180.0) {
            lon -= 0.04;
        }
        // the height of a grid point always is about 0.02
        lat -= 0.01;
        if (lat < -90.0) {
            lat = -90.0;
        } else if (lat + 0.02 > 90.0) {
            lat -= 0.02;
        }
        return new Rectangle2D.Double(lon, lat, 0.04, 0.02);
    }
}
