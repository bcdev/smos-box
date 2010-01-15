package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.SequenceType;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

class LaiFile extends ExplorerFile {

    private final SequenceData zoneList;

    LaiFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        super(hdrFile, dblFile, dataFormat);

        zoneList = getDataBlock().getSequence("DFFG_LAI");
        if (zoneList == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing zone data.", dblFile.getPath()));
        }
        final CompoundType zoneType = (CompoundType) zoneList.getType().getElementType();
        final int laiDataInZoneIndex = zoneType.getMemberIndex("List_of_DFFG_LAI_Point_Datas");
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

        return product;
    }
}
