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
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

class LsMaskFile extends ExplorerFile {

    private static final String GRID_POINT_MASK_DATA_TYPE_NAME = "Grid_Point_Mask_Data_Type";
    private static final String GRID_POINT_MASK_DATA_NAME = "Grid_Point_Mask_Data";

    private final SequenceData[] zones;

    LsMaskFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        super(hdrFile, dblFile, dataFormat);
        final SequenceData zoneSequence = getDataBlock().getSequence(SmosConstants.LAND_SEA_MASK_NAME);

        zones = new SequenceData[zoneSequence.getElementCount()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = zoneSequence.getCompound(i).getSequence(GRID_POINT_MASK_DATA_NAME);
        }
    }

    @Override
    protected Area computeArea() throws IOException {
        return new Area(new Rectangle2D.Double(-180.0, -90.0, 360.0, 180.0));
    }

    @Override
    protected Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getHdrFile());
        final String productType = getDataFormat().getName().substring(12, 22);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDblFile());
        product.setPreferredTileSize(512, 512);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);

        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));
        final CompoundType compoundType = (CompoundType) getDataFormat().getTypeDef(GRID_POINT_MASK_DATA_TYPE_NAME);
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(getDataFormat().getName());
        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor, compoundType);
            }
        }

        return product;
    }

    private void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
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

    private ValueProvider createValueProvider(final BandDescriptor descriptor) {
        return new ValueProvider() {
            @Override
            public Area getArea() {
                return LsMaskFile.this.getArea();
            }

            @Override
            public byte getValue(int seqnum, byte noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getByte(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public short getValue(int seqnum, short noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getShort(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public int getValue(int seqnum, int noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(seqnum) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(seqnum) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getInt(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }

            @Override
            public float getValue(int gridPointId, float noDataValue) {
                final int zoneIndex = SmosDgg.seqnumToZoneId(gridPointId) - 1;
                final int gridIndex = SmosDgg.seqnumToSeqnumInZone(gridPointId) - 1;

                try {
                    return zones[zoneIndex].getCompound(gridIndex).getFloat(descriptor.getMemberName());
                } catch (IOException e) {
                    return noDataValue;
                }
            }
        };
    }

    private MultiLevelImage createSourceImage(final Band band, final ValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private MultiLevelSource createMultiLevelSource(Band band, ValueProvider valueProvider) {
        return new SmosMultiLevelSource(band, valueProvider);
    }
}
