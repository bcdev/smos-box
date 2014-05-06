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
import java.text.MessageFormat;
import java.util.List;

/**
* @author Ralf Quast
*/
class LightBufrFile implements ProductFile {

    private final NetcdfFile ncfile;
    private final ReducedGaussianGrid grid;

    public LightBufrFile(File file) throws IOException {
        ncfile = NetcdfFile.open(file.getPath());
        grid = new ReducedGaussianGrid(512);
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
        final int[] seqnums = new int[lonVariable.getShape(0)];
        final double lonScalingOffset = getAttributeValue(lonVariable, "add_offset", 0.0);
        final double latScalingOffset = getAttributeValue(latVariable, "add_offset", 0.0);
        final double lonScalingFactor = getAttributeValue(lonVariable, "scale_factor", 1.0);
        final double latScalingFactor = getAttributeValue(latVariable, "scale_factor", 1.0);
        final double lonMissingValue = getAttributeValue(lonVariable, "missing_value");
        final double latMissingValue = getAttributeValue(latVariable, "missing_value");

        // TODO - establish mapping from (lon, lat, snapshot) to index

        final String productName = FileUtils.getFilenameWithoutExtension(getFile());
        final String productType = "W_ES-ESA-ESAC,SMOS,N256";
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getFile());
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

    private static double getAttributeValue(Variable lonVariable, String attributeName) {
        final Attribute attribute = lonVariable.findAttribute(attributeName);
        if (attribute == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Variable ''{0}'' does not exhibit requested attribute ''{1}''.",
                                         lonVariable.getShortName(), attributeName)
            );
        }
        return attribute.getNumericValue().doubleValue();
    }

    private void addBands(Product product) {
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

    private void addBand(Product product, Variable v, int dataType) {
        final Band band = product.addBand(v.getShortName(), dataType);
        final Attribute units = v.findAttribute("units");
        if (units != null) {
            band.setUnit(units.getStringValue());
        }
        final Attribute addOffset = v.findAttribute("add_offset");
        if (addOffset != null) {
            band.setScalingOffset(addOffset.getNumericValue().doubleValue());
        }
        final Attribute scaleFactor = v.findAttribute("scale_factor");
        if (scaleFactor != null) {
            band.setScalingFactor(scaleFactor.getNumericValue().doubleValue());
        }
        final Attribute missingValue = v.findAttribute("missing_value");
        if (missingValue != null) {
            band.setNoDataValue(missingValue.getNumericValue().doubleValue());
            band.setNoDataValueUsed(true);
        }
        final CellValueProvider valueProvider = createValueProvider(v);
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

    private CellValueProvider createValueProvider(Variable v) {
        return new LightBufrValueProvider();
    }

    @Override
    public File getFile() {
        return new File(ncfile.getLocation());
    }

    private Sequence getObservationSequence() {
        return (Sequence) ncfile.findVariable("obs");
    }

    private final class LightBufrValueProvider implements CellValueProvider {

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
            return 0;
        }

        @Override
        public short getValue(long cellIndex, short noDataValue) {
            return 0;
        }

        @Override
        public int getValue(long cellIndex, int noDataValue) {
            return 0;
        }

        @Override
        public float getValue(long cellIndex, float noDataValue) {
            return 0;
        }
    }
}
