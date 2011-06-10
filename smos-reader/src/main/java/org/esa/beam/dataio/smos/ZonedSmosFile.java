package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

class ZonedSmosFile extends ExplorerFile {

    private final SequenceData[] zones;

    public ZonedSmosFile(File hdrFile, File dblFile, DataFormat format) throws IOException {
        super(hdrFile, dblFile, format);
        final SequenceData zoneSequence = getDataBlock().getSequence(0);

        zones = new SequenceData[zoneSequence.getElementCount()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = zoneSequence.getCompound(i).getSequence(1);
        }
        // todo - gridPointInfo as in SmosFIle, but iterate over all zones
    }

    @Override
    protected Area computeArea() throws IOException {
        // todo - implement as in SmosFIle, but iterate over all zones
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
        final CompoundType compoundType = (CompoundType) zones[0].getType().getElementType();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors(getDataFormat().getName());
        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                addBand(product, descriptor, compoundType);
            }
        }

        return product;
    }

    private void addBand(Product product, BandDescriptor descriptor, CompoundType compoundType) {
        // todo - implement as in SmosFIle / GlobalZonedSmosFile
    }
}
