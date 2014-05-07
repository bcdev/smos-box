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
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LaiFile extends ExplorerFile {

    private static final String CUMULATED_LON_COUNT_NAME = "Cumulated_N_Lon";
    private static final String DELTA_LAT_NAME = "Delta_Lat";
    private static final String DELTA_LON_NAME = "Long_Step_Size_Ang";
    private static final String DFFG_LAI_NAME = "DFFG_LAI";
    private static final String DFFG_LAI_POINT_DATA_TYPE_NAME = "DFFG_LAI_Point_Data_Type";
    private static final String LIST_OF_DFFG_LAI_POINT_DATA_NAME = "List_of_DFFG_LAI_Point_Datas";
    private static final String LIST_OF_ROW_STRUCT_DATA_NAME = "List_of_Row_Struct_Datas";
    private static final String LON_COUNT_NAME = "N_Lon";
    private static final String MAX_LAT_NAME = "Lat_b";
    private static final String MIN_LAT_NAME = "Lat_a";
    private static final String MAX_LON_NAME = "Lon_b";
    private static final String MIN_LON_NAME = "Lon_a";

    private static final String TAG_SCALING_OFFSET = "Offset";
    private static final String TAG_SCALING_FACTOR = "Scaling_Factor";
    private static final String TAG_DIGITS_TO_SHIFT = "Digits_To_Shift";

    private final double scalingOffset;
    private final double scalingFactor;

    private final long zoneIndexMultiplier;
    private volatile List<Dffg> gridList = null;

    LaiFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        super(hdrFile, dblFile, dataFormat);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        scalingOffset = Double.valueOf(specificProductHeader.getChildText(TAG_SCALING_OFFSET, namespace));
        scalingFactor = Double.valueOf(specificProductHeader.getChildText(TAG_SCALING_FACTOR, namespace));
        final int k = Integer.valueOf(specificProductHeader.getChildText(TAG_DIGITS_TO_SHIFT, namespace));
        zoneIndexMultiplier = (long) Math.pow(10.0, k);
    }

    long getCellIndex(double lon, double lat) {
        final int zoneIndex = Eeap.getInstance().getZoneIndex(lon, lat);
        if (zoneIndex != -1) {
            final int gridIndex = getGridList().get(zoneIndex).getIndex(lon, lat);
            if (gridIndex != -1) {
                return gridIndex + zoneIndex * zoneIndexMultiplier;
            }
        }

        return -1;
    }

    @Override
    public final Area getArea() {
        return new Area(new Rectangle2D.Double(-180.0, -90.0, 360.0, 180.0));
    }

    @Override
    public Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getHeaderFile());
        final String productType = getDataFormat().getName().substring(12, 22);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 512);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));
        addBands(product);

        return product;
    }

    private CompoundType getCellType() {
        return (CompoundType) getDataFormat().getTypeDef(DFFG_LAI_POINT_DATA_TYPE_NAME);
    }

    private void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(formatName);

        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor, getCellType());
            }
        }
    }

    private void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        final int memberIndex = compoundType.getMemberIndex(descriptor.getMemberName());

        if (memberIndex >= 0) {
            final CompoundMember member = compoundType.getMember(memberIndex);

            final int dataType = ProductHelper.getDataType(member.getType());
            final Band band = product.addBand(descriptor.getBandName(), dataType);

            if ("LAI".equals(member.getName())) {
                band.setScalingOffset(scalingOffset);
                band.setScalingFactor(scalingFactor);
            } else {
                band.setScalingOffset(descriptor.getScalingOffset());
                band.setScalingFactor(descriptor.getScalingFactor());
            }
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

            final CellValueProvider valueProvider = createLaiValueProvider(descriptor);
            band.setSourceImage(createSourceImage(band, valueProvider));
            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

    private CellValueProvider createLaiValueProvider(BandDescriptor descriptor) {
        final int memberIndex = getCellType().getMemberIndex(descriptor.getMemberName());

        return new LaiValueProvider(memberIndex);
    }

    private MultiLevelImage createSourceImage(Band band, CellValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(final Band band, final CellValueProvider valueProvider) {
        return new AbstractMultiLevelSource(SmosDgg.getInstance().getMultiLevelImage().getModel()) {
            @Override
            protected RenderedImage createImage(int level) {
                return new CellGridOpImage(valueProvider, band, getModel(), ResolutionLevel.create(getModel(), level));
            }
        };
    }

    private int getGridIndex(long cellIndex, int zoneIndex) {
        return (int) (cellIndex - zoneIndex * zoneIndexMultiplier);
    }

    private int getZoneIndex(long cellIndex) {
        return (int) (cellIndex / zoneIndexMultiplier);
    }

    private List<Dffg> getGridList() {
        List<Dffg> result = gridList;

        if (result == null) {
            synchronized (this) {
                result = gridList;
                if (result == null) {
                    try {
                        gridList = result = createGridList();
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot create zone list.", e);
                    }
                }
            }
        }

        return result;
    }

    private List<Dffg> createGridList() throws IOException {
        final SequenceData zoneSequenceData = getDataBlock().getSequence(DFFG_LAI_NAME);
        if (zoneSequenceData == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing zone data.", getDataFile().getPath()));
        }
        final ArrayList<Dffg> gridList = new ArrayList<Dffg>(
                zoneSequenceData.getElementCount());

        for (int i = 0; i < zoneSequenceData.getElementCount(); i++) {
            final CompoundData zoneCompoundData = zoneSequenceData.getCompound(i);
            final double minLat = zoneCompoundData.getDouble(MIN_LAT_NAME);
            final double maxLat = zoneCompoundData.getDouble(MAX_LAT_NAME);
            final double minLon = zoneCompoundData.getDouble(MIN_LON_NAME);
            final double maxLon = zoneCompoundData.getDouble(MAX_LON_NAME);
            final double deltaLat = zoneCompoundData.getDouble(DELTA_LAT_NAME);

            // due to an unresolved bug in ceres-binio it is essential to remember the LAI sequence data
            final SequenceData sequenceData = zoneCompoundData.getSequence(LIST_OF_DFFG_LAI_POINT_DATA_NAME);
            final Dffg grid = new Dffg(minLat, maxLat, minLon, maxLon, deltaLat, sequenceData);
            final SequenceData rowSequenceData = zoneCompoundData.getSequence(LIST_OF_ROW_STRUCT_DATA_NAME);
            for (int p = 0; p < rowSequenceData.getElementCount(); p++) {
                final CompoundData rowCompoundData = rowSequenceData.getCompound(p);
                final int lonCount = rowCompoundData.getInt(LON_COUNT_NAME);
                final double deltaLon = rowCompoundData.getDouble(DELTA_LON_NAME);
                final int cumulatedLonCount = rowCompoundData.getInt(CUMULATED_LON_COUNT_NAME);

                grid.setRow(p, lonCount, deltaLon, cumulatedLonCount);
            }

            gridList.add(grid);
        }

        return Collections.unmodifiableList(gridList);
    }

    private final class LaiValueProvider implements CellValueProvider {

        private final int memberIndex;

        public LaiValueProvider(int memberIndex) {
            this.memberIndex = memberIndex;
        }

        @Override
        public final Area getArea() {
            return LaiFile.this.getArea();
        }

        @Override
        public final long getCellIndex(double lon, double lat) {
            return LaiFile.this.getCellIndex(lon, lat);
        }

        @Override
        public final byte getValue(long cellIndex, byte noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getByte(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final short getValue(long cellIndex, short noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getShort(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final int getValue(long cellIndex, int noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getInt(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public final float getValue(long cellIndex, float noDataValue) {
            final int zoneIndex = getZoneIndex(cellIndex);
            final int gridIndex = getGridIndex(cellIndex, zoneIndex);

            try {
                return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getFloat(memberIndex);
            } catch (IOException e) {
                return noDataValue;
            }
        }
    }
}
