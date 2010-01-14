package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.SequenceType;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

class LaiFile extends ExplorerFile {

    private final SequenceData zoneList;
    private final Rectangle2D[] zoneBounds;
    private final CompoundType gridPointType;

    LaiFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        super(hdrFile, dblFile, dataFormat);

        zoneList = getDataBlock().getSequence("DFFG_LAI");
        if (zoneList == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing EEAP zone list.", dblFile.getPath()));
        }
        final CompoundType zoneDataType = (CompoundType) zoneList.getType().getElementType();
        final int index = zoneDataType.getMemberIndex("List_of_DFFG_LAI_Point_Datas");
        final SequenceType sequenceType = (SequenceType) zoneDataType.getMemberType(index);
        gridPointType = (CompoundType) sequenceType.getElementType();

        zoneBounds = new Rectangle2D[zoneList.getElementCount()];

        final File file = new File("/Users/ralf/Desktop/lai.out");
        final PrintWriter pw = new PrintWriter(file);

        for (int i = 0; i < zoneList.getElementCount(); i++) {
            final CompoundData zoneData = zoneList.getCompound(i);
            final SequenceData rowSequenceData = zoneData.getSequence("List_of_Row_Struct_Datas");
            final SequenceData laiSequenceData = zoneData.getSequence("List_of_DFFG_LAI_Point_Datas");

            if (laiSequenceData.getElementCount() > 0) {
                pw.println("****************************************");
                final long zoneId = zoneData.getLong("Zone_ID");
                final float delta = zoneData.getFloat("Delta");
                final float latA = zoneData.getFloat("Lat_a");
                final float latB = zoneData.getFloat("Lat_b");
                final float lonA = zoneData.getFloat("Lon_a");
                final float lonB = zoneData.getFloat("Lon_b");
                final float deltaLat = zoneData.getFloat("Delta_Lat");
                final float deltaLatKm = zoneData.getFloat("Delta_Lat_km");

                pw.println("Zone_ID = " + zoneId);
                pw.println("Delta = " + delta);
                pw.println("Lat_a = " + latA);
                pw.println("Lat_b = " + latB);
                pw.println("Lon_a = " + lonA);
                pw.println("Lon_b = " + lonB);
                pw.println("Delta_Lat = " + deltaLat);
                pw.println("Delta_Lat_km = " + deltaLatKm);
                pw.println("N_Lat = " + rowSequenceData.getElementCount());

                zoneBounds[i] = new Rectangle2D.Float(lonA, latA, lonB - lonA, latB - latA);

                for (int k = 0; k < rowSequenceData.getElementCount(); k++) {
                    final CompoundData rowData = rowSequenceData.getCompound(k);

                    final long nLon = rowData.getLong("N_Lon");
                    final float stepSizeAng = rowData.getFloat("Long_Step_Size_Ang");
                    final float stepSizeKm = rowData.getFloat("Long_Step_Size_Km");
                    final long cumulatedNLon = rowData.getLong("Cumulated_N_Lon");

                    pw.println("N_Lon = " + nLon);
                    pw.println("Long_Step_Size_Ang = " + stepSizeAng);
                    pw.println("Long_Step_Size_Km = " + stepSizeKm);
                    pw.println("Cumulated_N_Lon = " + cumulatedNLon);
                }

                pw.println();
            }
        }

        pw.close();
    }

    public CompoundType getGridPointType() {
        return gridPointType;
    }

    @Override
    protected Area computeArea() throws IOException {
        return new Area(new Rectangle2D.Double(-180, -90, 360, 180));
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
        addBands(product);

        return product;
    }

    protected void addBands(Product product) {
        final String formatName = getDataFormat().getName();
        final Family<BandDescriptor> descriptors = DDDB.getInstance().getBandDescriptors(formatName);

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

//            final ValueProvider valueProvider = createValueProvider(descriptor);
//            band.setSourceImage(createSourceImage(band, valueProvider));
//            band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
        }
    }

}
