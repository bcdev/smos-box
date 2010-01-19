package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class LaiFile extends ExplorerFile {

    private static final String CUMULATED_LON_COUNT_NAME = "Cumulated_N_Lon";
    private static final String DELTA_LAT_NAME = "Delta_Lat";
    private static final String DELTA_LON_NAME = "Long_Step_Size_Ang";
    private static final String DFFG_LAI_NAME = "DFFG_LAI";
    private static final String LIST_OF_DFFG_LAI_POINT_DATA_NAME = "List_of_DFFG_LAI_Point_Datas";
    private static final String LIST_OF_ROW_STRUCT_DATA_NAME = "List_of_Row_Struct_Datas";
    private static final String LON_COUNT_NAME = "N_Lon";
    private static final String MAX_LAT_NAME = "Lat_b";
    private static final String MIN_LAT_NAME = "Lat_a";
    private static final String MAX_LON_NAME = "Lon_b";
    private static final String MIN_LON_NAME = "Lon_a";

    private static final String ROW_COUNT_NAME = "N_Lat";
    private static final String TAG_SCALING_OFFSET = "Offset";
    private static final String TAG_SCALING_FACTOR = "Scaling_Factor";

    private static final String TAG_DIGITS_TO_SHIFT = "Digits_To_Shift";
    private final double scalingOffset;
    private final double scalingFactor;

    private final long zoneIndexMultiplier;
    private volatile Future<List<DFFG>> gridListFuture;

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

    public final long getCellIndex(double lon, double lat) {
        final int zoneIndex = EEAP.getInstance().getZoneIndex(lon, lat);
        if (zoneIndex != -1) {
            final int gridIndex = getGridList().get(zoneIndex).getIndex(lon, lat);
            if (gridIndex != -1) {
                return gridIndex + zoneIndex * zoneIndexMultiplier;
            }
        }
        return -1;
    }

    byte getLaiValue(long cellIndex, byte noDataValue) {
        final int zoneIndex = getZoneIndex(cellIndex);
        final int gridIndex = getGridIndex(cellIndex, zoneIndex);

        try {
            return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getByte("LAI");
        } catch (IOException e) {
            return noDataValue;
        }
    }

    short getLaiValue(long cellIndex, short noDataValue) {
        final int zoneIndex = getZoneIndex(cellIndex);
        final int gridIndex = getGridIndex(cellIndex, zoneIndex);

        try {
            return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getShort("LAI");
        } catch (IOException e) {
            return noDataValue;
        }
    }

    int getLaiValue(long cellIndex, int noDataValue) {
        final int zoneIndex = getZoneIndex(cellIndex);
        final int gridIndex = getGridIndex(cellIndex, zoneIndex);

        try {
            return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getInt("LAI");
        } catch (IOException e) {
            return noDataValue;
        }
    }

    float getLaiValue(long cellIndex, float noDataValue) {
        final int zoneIndex = getZoneIndex(cellIndex);
        final int gridIndex = getGridIndex(cellIndex, zoneIndex);

        try {
            return getGridList().get(zoneIndex).getSequenceData().getCompound(gridIndex).getFloat("LAI");
        } catch (IOException e) {
            return noDataValue;
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
        final Band band = product.addBand("LAI", ProductData.TYPE_UINT8);
        band.setNoDataValue(0.0);
        band.setNoDataValueUsed(true);
        band.setScalingOffset(scalingOffset);
        band.setScalingFactor(scalingFactor);

        band.setSourceImage(new DefaultMultiLevelImage(
                new AbstractMultiLevelSource(SmosDgg.getInstance().getDggMultiLevelImage().getModel()) {
                    @Override
                    protected RenderedImage createImage(int level) {
                        return new LaiOpImage(LaiFile.this, band, getModel(),
                                              ResolutionLevel.create(getModel(), level));
                    }
                }));

        return product;
    }

    private int getGridIndex(long cellIndex, int zoneIndex) {
        return (int) (cellIndex - zoneIndex * zoneIndexMultiplier);
    }

    private int getZoneIndex(long cellIndex) {
        return (int) (cellIndex / zoneIndexMultiplier);
    }

    private List<DFFG> getGridList() {
        try {
            return getGridListFuture().get();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    private Future<List<DFFG>> getGridListFuture() {
        if (gridListFuture == null) {
            synchronized (this) {
                if (gridListFuture == null) {
                    gridListFuture = Executors.newSingleThreadExecutor().submit(
                            new Callable<List<DFFG>>() {
                                @Override
                                public List<DFFG> call() throws IOException {
                                    return createGridList();
                                }
                            });
                }
            }
        }
        return gridListFuture;
    }

    private List<DFFG> createGridList() throws IOException {
        final SequenceData zoneSequenceData = getDataBlock().getSequence(DFFG_LAI_NAME);
        if (zoneSequenceData == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing zone data.", getDblFile().getPath()));
        }
        final ArrayList<DFFG> gridList = new ArrayList<DFFG>(
                zoneSequenceData.getElementCount());

        for (int i = 0; i < zoneSequenceData.getElementCount(); i++) {
            final CompoundData zoneCompoundData = zoneSequenceData.getCompound(i);
            final double minLat = zoneCompoundData.getDouble(MIN_LAT_NAME);
            final double maxLat = zoneCompoundData.getDouble(MAX_LAT_NAME);
            final double minLon = zoneCompoundData.getDouble(MIN_LON_NAME);
            final double maxLon = zoneCompoundData.getDouble(MAX_LON_NAME);
            final double deltaLat = zoneCompoundData.getDouble(DELTA_LAT_NAME);
            final int rowCount = zoneCompoundData.getInt(ROW_COUNT_NAME);
            // due to an unknown bug in ceres-binio it is essential to remember the LAI sequence data 
            final SequenceData sequenceData = zoneCompoundData.getSequence(LIST_OF_DFFG_LAI_POINT_DATA_NAME);
            final DFFG grid = new DFFG(minLat, maxLat, minLon, maxLon, deltaLat, rowCount, sequenceData);

            final SequenceData rowList = zoneCompoundData.getSequence(LIST_OF_ROW_STRUCT_DATA_NAME);
            for (int p = 0; p < rowList.getElementCount(); p++) {
                final CompoundData rowData = rowList.getCompound(p);
                final int lonCount = rowData.getInt(LON_COUNT_NAME);
                final double deltaLon = rowData.getDouble(DELTA_LON_NAME);
                final int cumulatedLonCount = rowData.getInt(CUMULATED_LON_COUNT_NAME);

                grid.setRow(p, lonCount, deltaLon, cumulatedLonCount);
            }

            gridList.add(grid);
        }

        return Collections.unmodifiableList(gridList);
    }
}
