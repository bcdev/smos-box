package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.glevel.MultiLevelSource;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.Color;
import java.io.IOException;

class EcmwfProductFactory extends SmosProductFactory {

    private static final int LOWER_4_OF_8 = 1;
    private static final int UPPER_4_OF_8 = 2;

    @Override
    protected void addBands(Product product, SmosFile smosFile) {
        if (!(smosFile instanceof SmosDggFile)) {
            throw new IllegalArgumentException("SMOS DGG file expected.");
        }

        final String formatName = smosFile.getFormat().getName();
        final BandDescriptors descriptors = BandDescriptorRegistry.getInstance().getDescriptors(formatName);

        for (final BandDescriptor descriptor : descriptors.asList()) {
            addBand(product, (SmosDggFile) smosFile, descriptor);
        }
    }

    protected FieldValueProvider createValueProvider(SmosDggFile smosFile, BandDescriptor descriptor) {
        switch (descriptor.getSampleModel()) {
        case LOWER_4_OF_8:
            return new DggValueProvider(smosFile, descriptor.getMemberName()) {
                @Override
                public int getValue(int gridPointIndex, int noDataValue) {
                    try {
                        final long value = getSmosFile().getGridPointData(gridPointIndex).getLong(getMemberIndex());
                        return (int) (value & 0x00000000FFFFFFFFL);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        case UPPER_4_OF_8:
            return new DggValueProvider(smosFile, descriptor.getMemberName()) {
                @Override
                public int getValue(int gridPointIndex, int noDataValue) {
                    try {
                        final long value = getSmosFile().getGridPointData(gridPointIndex).getLong(getMemberIndex());
                        return (int) (value >>> 32);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        default:
            return new DggValueProvider(smosFile, descriptor.getMemberName());
        }
    }

    @Override
    protected MultiLevelSource createMultiLevelSource(Band band, FieldValueProvider valueProvider) {
        return new SmosMultiLevelSource(valueProvider, SmosDgg.getInstance().getDggMultiLevelImage(), band);
    }

    protected Band addBand(Product product, SmosDggFile smosFile, BandDescriptor descriptor) {
        final CompoundType compoundDataType = smosFile.getGridPointType();
        final int memberIndex = compoundDataType.getMemberIndex(descriptor.getMemberName());
        final CompoundMember member = compoundDataType.getMember(memberIndex);

        final int dataType = getDataType(member.getType());
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

        final FieldValueProvider valueProvider = createValueProvider(smosFile, descriptor);

        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(createImageInfo(band, descriptor));

        return band;
    }

    protected ImageInfo createImageInfo(Band band, BandDescriptor descriptor) {
        final Color[] colors;
        if (descriptor.isCyclic()) {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0),
                    new Color(255, 140, 0),
                    new Color(255, 255, 0),
                    new Color(0, 255, 0),
                    new Color(0, 255, 255),
                    new Color(0, 0, 255),
                    new Color(85, 0, 136),
                    new Color(0, 0, 0)
            };
        } else {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0)
            };
        }

        final double min;
        final double max;
        if (descriptor.hasTypicalMin()) {
            min = descriptor.getTypicalMin();
        } else {
            min = band.getStx().getMin();
        }
        if (descriptor.hasTypicalMax()) {
            max = descriptor.getTypicalMax();
        } else {
            max = band.getStx().getMax();
        }

        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final double sample = min + ((max - min) * i / (colors.length - 1));
            points[i] = new ColorPaletteDef.Point(sample, colors[i]);
        }

        return new ImageInfo(new ColorPaletteDef(points));
    }

}
