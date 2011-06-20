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
package org.esa.beam.dataio.smos;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Represents a SMOS DGG product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class SmosFile extends ExplorerFile {

    private final GridPointList gridPointList;
    private final int gridPointIdIndex;

    private volatile Future<GridPointInfo> gridPointInfoFuture;

    SmosFile(File hdrFile, File dblFile, DataFormat format) throws IOException {
        super(hdrFile, dblFile, format);
        try {
            final SequenceData gridPointSequence = getDataBlock().getSequence(SmosConstants.GRID_POINT_LIST_NAME);
            if (gridPointSequence != null) {
                gridPointList = createGridPointList(gridPointSequence);
            } else {
                gridPointList = createGridPointListFromZones(getDataBlock().getSequence(0));
            }
            gridPointIdIndex = gridPointList.getCompoundType().getMemberIndex(SmosConstants.GRID_POINT_ID_NAME);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Unable to read SMOS File ''{0}'': {1}.", dblFile.getPath(), e.getMessage()), e);
        }
    }

    private GridPointList createGridPointList(final SequenceData sequence) {
        return new GridPointList() {
            @Override
            public final int getElementCount() {
                return sequence.getElementCount();
            }

            @Override
            public final CompoundData getCompound(int i) throws IOException {
                return sequence.getCompound(i);
            }

            @Override
            public final CompoundType getCompoundType() {
                return (CompoundType) sequence.getType().getElementType();
            }
        };
    }

    private GridPointList createGridPointListFromZones(SequenceData zoneSequence) throws IOException {
        final SequenceData[] zones = new SequenceData[zoneSequence.getElementCount()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = zoneSequence.getCompound(i).getSequence(1);
        }
        return new GridPointList() {
            @Override
            public final int getElementCount() {
                int elementCount = 0;
                for (final SequenceData zone : zones) {
                    elementCount += zone.getElementCount();
                }
                return elementCount;
            }

            @Override
            public final CompoundData getCompound(int i) throws IOException {
                int elementCount = 0;
                for (final SequenceData zone : zones) {
                    elementCount += zone.getElementCount();
                    if (i < elementCount) {
                        return zone.getCompound(i - elementCount);
                    }
                }
                throw new IOException("");
            }

            @Override
            public final CompoundType getCompoundType() {
                return (CompoundType) zones[0].getType().getElementType();
            }
        };
    }

    public final int getGridPointCount() {
        return gridPointList.getElementCount();
    }

    public final int getGridPointId(int i) throws IOException {
        final int gridPointId = gridPointList.getCompound(i).getInt(gridPointIdIndex);
        if (gridPointId < SmosDgg.MIN_GRID_POINT_ID || gridPointId > SmosDgg.MAX_GRID_POINT_ID) {
            throw new IOException(MessageFormat.format("Invalid Grid Point ID {0} at index {1}.", gridPointId, i));
        }
        return gridPointId;
    }

    public final int getGridPointSeqnum(int i) throws IOException {
        return SmosDgg.gridPointIdToSeqnum(getGridPointId(i));
    }

    public final GridPointList getGridPointList() {
        return gridPointList;
    }

    private Future<GridPointInfo> getGridPointInfoFuture() {
        if (gridPointInfoFuture == null) {
            synchronized (this) {
                if (gridPointInfoFuture == null) {
                    gridPointInfoFuture = Executors.newSingleThreadExecutor().submit(new Callable<GridPointInfo>() {
                        @Override
                        public GridPointInfo call() throws IOException {
                            return createGridPointInfo();
                        }
                    });
                }
            }
        }

        return gridPointInfoFuture;
    }

    public final int getGridPointIndex(int seqnum) {
        try {
            return getGridPointInfoFuture().get().getGridPointIndex(seqnum);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public final CompoundType getGridPointType() {
        return gridPointList.getCompoundType();
    }

    public final CompoundData getGridPointData(int gridPointIndex) throws IOException {
        return gridPointList.getCompound(gridPointIndex);
    }

    @Override
    protected final Area computeArea() throws IOException {
        final int latIndex = getGridPointType().getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        final int lonIndex = getGridPointType().getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);

        final Rectangle2D[] tileRectangles = new Rectangle2D[512];
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 16; ++j) {
                tileRectangles[i * 16 + j] = createTileRectangle(i, j);
            }
        }

        final Area envelope = new Area();
        for (int i = 0; i < gridPointList.getElementCount(); i++) {
            CompoundData compound = gridPointList.getCompound(i);
            double lon = compound.getFloat(lonIndex);
            double lat = compound.getFloat(latIndex);

            // normalisation to [-180, 180] necessary for some L1c test products
            if (lon > 180.0) {
                lon = lon - 360.0;
            }
            final double hw = 0.02;
            final double hh = 0.02;

            final double x = lon - hw;
            final double y = lat - hh;
            final double w = 0.04;
            final double h = 0.04;

            if (!envelope.contains(x, y, w, h)) {
                for (final Rectangle2D tileRectangle : tileRectangles) {
                    if (tileRectangle.intersects(x, y, w, h) && !envelope.contains(tileRectangle)) {
                        envelope.add(new Area(tileRectangle));
                        if (envelope.contains(x, y, w, h)) {
                            break;
                        }
                    }
                }
            }
        }

        return envelope;
    }

    @Override
    protected final Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getHdrFile());
        final String productType = getDataFormat().getName().substring(12, 22);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDblFile());
        product.setPreferredTileSize(512, 512);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));
        addBands(product);
        setTimes(product);

        return product;
    }

    protected void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor);
            }
        }
    }

    protected void addBand(Product product, BandDescriptor descriptor) {
        addBand(product, descriptor, getGridPointType());
    }

    protected final void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());

        if (memberIndex >= 0) {
            final CompoundMember member = compoundType.getMember(memberIndex);

            final int dataType = ProductHelper.getDataType(member.getType());
            final Band band = product.addBand(descriptor.getBandName(), dataType);

            band.setScalingOffset(descriptor.getScalingOffset());
            band.setScalingFactor(descriptor.getScalingFactor());
            if (descriptor.hasFillValue()) {
                band.setNoDataValueUsed(true);
                band.setNoDataValue(descriptor.getFillValue());
            }
            if (!descriptor.getValidPixelExpression().isEmpty()) {
                band.setValidPixelExpression(descriptor.getValidPixelExpression());
            }
            if (!descriptor.getUnit().isEmpty()) {
                band.setUnit(descriptor.getUnit());
            }
            if (!descriptor.getDescription().isEmpty()) {
                band.setDescription(descriptor.getDescription());
            }
            if (descriptor.getFlagDescriptors() != null) {
                ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                                               descriptor.getFlagDescriptors());
            }

            final ValueProvider valueProvider = createValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

    protected SmosValueProvider createValueProvider(BandDescriptor descriptor) {
        final int memberIndex = getGridPointType().getMemberIndex(descriptor.getMemberName());

        switch (descriptor.getSampleModel()) {
        case 1:
            return new DefaultValueProvider(this, memberIndex) {
                @Override
                protected int getInt(int gridPointIndex) throws IOException {
                    return (int) (getLong(memberIndex) & 0x00000000FFFFFFFFL);
                }
            };
        case 2:
            return new DefaultValueProvider(this, memberIndex) {
                @Override
                public int getInt(int gridPointIndex) throws IOException {
                    return (int) (getLong(memberIndex) >>> 32);
                }
            };
        default:
            return new DefaultValueProvider(this, memberIndex);
        }
    }

    protected MultiLevelImage createSourceImage(final Band band, final ValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    protected MultiLevelSource createMultiLevelSource(Band band, ValueProvider valueProvider) {
        return new SmosMultiLevelSource(band, valueProvider);
    }

    private GridPointInfo createGridPointInfo() throws IOException {
        int minSeqnum = getGridPointSeqnum(0);
        int maxSeqnum = minSeqnum;

        final int gridPointCount = getGridPointCount();
        for (int i = 1; i < gridPointCount; i++) {
            final int seqnum = getGridPointSeqnum(i);

            if (seqnum < minSeqnum) {
                minSeqnum = seqnum;
            } else {
                if (seqnum > maxSeqnum) {
                    maxSeqnum = seqnum;
                }
            }
        }

        final GridPointInfo gridPointInfo = new GridPointInfo(minSeqnum, maxSeqnum);
        Arrays.fill(gridPointInfo.indexes, -1);

        for (int i = 0; i < gridPointCount; i++) {
            gridPointInfo.indexes[getGridPointSeqnum(i) - minSeqnum] = i;
        }

        return gridPointInfo;
    }

    private Rectangle2D createTileRectangle(int i, int j) {
        final double w = 11.25;
        final double h = 11.25;
        final double x = w * i - 180.0;
        final double y = 90.0 - h * (j + 1);

        return new Rectangle2D.Double(x, y, w, w);
    }

    private void setTimes(Product product) {
        final String pattern = "'UTC='yyyy-MM-dd'T'HH:mm:ss";
        try {
            final Document document = getDocument();
            final Namespace namespace = document.getRootElement().getNamespace();
            final Element validityPeriod = getElement(document.getRootElement(), "Validity_Period");
            final String validityStart = validityPeriod.getChildText("Validity_Start", namespace);
            final String validityStop = validityPeriod.getChildText("Validity_Stop", namespace);
            product.setStartTime(ProductData.UTC.parse(validityStart, pattern));
            product.setEndTime(ProductData.UTC.parse(validityStop, pattern));
        } catch (Exception e) {
            // ignore
        }
    }

    private static final class GridPointInfo {

        final int minSeqnum;
        final int maxSeqnum;
        final int[] indexes;

        GridPointInfo(int minSeqnum, int maxSeqnum) {
            this.minSeqnum = minSeqnum;
            this.maxSeqnum = maxSeqnum;
            indexes = new int[maxSeqnum - minSeqnum + 1];
        }

        int getGridPointIndex(int seqnum) {
            if (seqnum < minSeqnum || seqnum > maxSeqnum) {
                return -1;
            }

            return indexes[seqnum - minSeqnum];
        }
    }
}
