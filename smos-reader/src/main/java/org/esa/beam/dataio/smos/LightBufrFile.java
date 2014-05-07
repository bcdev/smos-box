package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.binning.support.ReducedGaussianGrid;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ralf Quast
 */
class LightBufrFile implements ProductFile {

    private final NetcdfFile ncfile;
    private final ReducedGaussianGrid grid;
    private int[] indexes;

    public LightBufrFile(File file) throws IOException {
        ncfile = NetcdfFile.open(file.getPath());
        grid = new ReducedGaussianGrid(512);
        indexes = new int[(int) grid.getNumBins()];
    }

    @Override
    public void close() throws IOException {
        ncfile.close();
    }

    @Override
    public Product createProduct() throws IOException {
        final Sequence sequence = getObservationSequence();
        final Variable lonVariable = sequence.findVariable("Longitude_high_accuracy");
        final Variable latVariable = sequence.findVariable("Latitude_high_accuracy");
        final Array lonData = lonVariable.read();
        final Array latData = latVariable.read();
        final Number missingValueLon = getAttributeValue(lonVariable, "missing_value");
        final Number missingValueLat = getAttributeValue(latVariable, "missing_value");
        final double addOffsetLon = getAttributeValue(lonVariable, "add_offset", 0.0);
        final double addOffsetLat = getAttributeValue(latVariable, "add_offset", 0.0);
        final double scaleFactorLon = getAttributeValue(lonVariable, "scale_factor", 1.0);
        final double scaleFactorLat = getAttributeValue(latVariable, "scale_factor", 1.0);
        final int elementCount = (int) lonData.getSize();

        // TODO - establish mapping from cell-index to array-index
        Arrays.fill(indexes, -1);
        for (int i = 0; i < 700; i++) {
            final double rawLon = lonData.getDouble(i);
            final double rawLat = latData.getDouble(i);
            if ((missingValueLon == null || rawLon != missingValueLon.doubleValue()) && (missingValueLat == null || rawLat != missingValueLat.doubleValue())) {
                final double lon = rawLon * scaleFactorLon + addOffsetLon;
                final double lat = rawLat * scaleFactorLat + addOffsetLat;
                final int binIndex = (int) grid.getBinIndex(lat, lon);
                indexes[binIndex] = i;
            }
        }

        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = "W_ES-ESA-ESAC,SMOS,N256";
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 512);
        final List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
        product.getMetadataRoot().addElement(
                MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final List<Variable> variables = sequence.getVariables();
        product.getMetadataRoot().addElement(
                MetadataUtils.readVariableDescriptions(variables, "Variable_Attributes", 100));
        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));


        addBands(product);
        //setTimes(product);

        return product;
    }

    @Override
    public final Area getArea() {
        return new Area(new Rectangle2D.Double(-180.0, -90.0, 360.0, 180.0));
    }

    private static double getAttributeValue(Variable lonVariable, String attributeName, double defaultValue) {
        final Attribute attribute = lonVariable.findAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        return attribute.getNumericValue().doubleValue();
    }

    private static Number getAttributeValue(Variable lonVariable, String attributeName) {
        final Attribute attribute = lonVariable.findAttribute(attributeName);
        if (attribute == null) {
            return null;
        }
        return attribute.getNumericValue();
    }

    private void addBands(Product product) throws IOException {
        final Sequence sequence = getObservationSequence();
        final List<Variable> variables = sequence.getVariables();
        for (final Variable v : variables) {
            if (v.getDataType().isEnum()) {
                final int dataType = ProductData.TYPE_UINT8;
                addBand(product, v, dataType);
            } else {
                final int dataType = DataTypeUtils.getRasterDataType(v);
                if (dataType != -1) {
                    addBand(product, v, dataType);
                }
            }
        }
    }

    private void addBand(Product product, Variable variable, int dataType) throws IOException {
        final Band band = product.addBand(variable.getShortName(), dataType);
        final Attribute units = variable.findAttribute("units");
        if (units != null) {
            band.setUnit(units.getStringValue());
        }
        final double addOffset = getAttributeValue(variable, "add_offset", 0.0);
        if (addOffset != 0.0) {
            band.setScalingOffset(addOffset);
        }
        final double scaleFactor = getAttributeValue(variable, "scale_factor", 1.0);
        if (scaleFactor != 1.0) {
            band.setScalingFactor(scaleFactor);
        }
        final Attribute missingValue = variable.findAttribute("missing_value");
        if (missingValue != null) {
            band.setNoDataValue(missingValue.getNumericValue().doubleValue());
            band.setNoDataValueUsed(true);
        }
        final CellValueProvider valueProvider = createValueProvider(variable);
        band.setSourceImage(createSourceImage(band, valueProvider));
    }

    private MultiLevelImage createSourceImage(final Band band, final CellValueProvider valueProvider) {
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

    private CellValueProvider createValueProvider(Variable v) throws IOException {
        return new LightBufrValueProvider(v);
    }

    @Override
    public File getDataFile() {
        return new File(ncfile.getLocation());
    }

    private Sequence getObservationSequence() {
        return (Sequence) ncfile.findVariable("obs");
    }

    private final class LightBufrValueProvider implements CellValueProvider {

        private final Variable variable;
        private volatile Array array;

        private LightBufrValueProvider(Variable variable) {
            this.variable = variable;
        }

        private Array getArray() throws IOException {
            if (array == null) {
                synchronized (this) {
                    if (array == null) {
                        array = variable.read();
                    }
                }
            }
            return array;
        }

        @Override
        public Area getArea() {
            return LightBufrFile.this.getArea();
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return LightBufrFile.this.grid.getBinIndex(lat, lon);
        }

        @Override
        public byte getValue(long cellIndex, byte noDataValue) {
            final int index = LightBufrFile.this.indexes[(int) cellIndex];
            if (index == -1) {
                return noDataValue;
            }
            try {
                return getArray().getByte(index);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public short getValue(long cellIndex, short noDataValue) {
            final int index = LightBufrFile.this.indexes[(int) cellIndex];
            if (index == -1) {
                return noDataValue;
            }
            try {
                return getArray().getShort(index);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public int getValue(long cellIndex, int noDataValue) {
            final int index = LightBufrFile.this.indexes[(int) cellIndex];
            if (index == -1) {
                return noDataValue;
            }
            try {
                return getArray().getInt(index);
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public float getValue(long cellIndex, float noDataValue) {
            final int index = LightBufrFile.this.indexes[(int) cellIndex];
            if (index == -1) {
                return noDataValue;
            }
            try {
                return getArray().getFloat(index);
            } catch (IOException e) {
                return noDataValue;
            }
        }
    }
}
