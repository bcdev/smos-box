package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.binning.PlanetaryGrid;
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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ralf Quast
 */
class LightBufrFile implements ProductFile {

    private static final String VARIABLE_NAME_LON = "Longitude_high_accuracy";
    private static final String VARIABLE_NAME_LAT = "Latitude_high_accuracy";
    private static final String ATTR_NAME_MISSING_VALUE = "missing_value";
    private static final String ATTR_NAME_ADD_OFFSET = "add_offset";
    private static final String ATTR_NAME_SCALE_FACTOR = "scale_factor";

    private final NetcdfFile ncfile;
    private final Grid grid;
    private final Area area;
    private int[] arrayIndexes;

    public LightBufrFile(File file) throws IOException {
        ncfile = NetcdfFile.open(file.getPath());
        grid = new Grid(new ReducedGaussianGrid(512));
        arrayIndexes = new int[grid.getCellCount()];

        final Sequence observationSequence = getObservationSequence();
        final Variable lonVariable = observationSequence.findVariable(VARIABLE_NAME_LON);
        final Variable latVariable = observationSequence.findVariable(VARIABLE_NAME_LAT);
        final Accessor lonAccessor = new Accessor(lonVariable);
        final Accessor latAccessor = new Accessor(latVariable);
        final int elementCount = lonAccessor.getElementCount();

        // TODO - establish mapping from cell-index to array-index for individual snapshots
        Arrays.fill(arrayIndexes, -1);
        for (int i = 0; i < elementCount; i++) {
            if (lonAccessor.isValid(i) && latAccessor.isValid(i)) {
                final double lon = lonAccessor.getDouble(i);
                final double lat = latAccessor.getDouble(i);
                final int cellIndex = grid.getCellIndex(lon, lat);
                if (arrayIndexes[cellIndex] == -1) {
                    arrayIndexes[cellIndex] = i;
                }
            }
        }
        final PointList pointList = createPointList(lonAccessor, latAccessor);
        area = DggFile.computeArea(pointList);
    }

    @Override
    public void close() throws IOException {
        ncfile.close();
    }

    @Override
    public Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getDataFile());
        final String productType = "W_ES-ESA-ESAC,SMOS,N256";
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDataFile());
        product.setPreferredTileSize(512, 512);
        final List<Attribute> globalAttributes = ncfile.getGlobalAttributes();
        product.getMetadataRoot().addElement(
                MetadataUtils.readAttributeList(globalAttributes, "Global_Attributes"));
        final Sequence sequence = getObservationSequence();
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
        return new Area(area);
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
        final double addOffset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
        if (addOffset != 0.0) {
            band.setScalingOffset(addOffset);
        }
        final double scaleFactor = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        if (scaleFactor != 1.0) {
            band.setScalingFactor(scaleFactor);
        }
        final Attribute missingValue = variable.findAttribute(ATTR_NAME_MISSING_VALUE);
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

    private PointList createPointList(Accessor lonAccessor, Accessor latAccessor) throws IOException {
        final int elementCount = lonAccessor.getElementCount();
        final Set<Integer> cellIndexSet = new HashSet<>();
        final List<Point> pointList = new ArrayList<>(elementCount);

        for (int i = 0; i < elementCount; i++) {
            if (lonAccessor.isValid(i) && latAccessor.isValid(i)) {
                final double lon = lonAccessor.getDouble(i);
                final double lat = latAccessor.getDouble(i);
                final int cellIndex = grid.getCellIndex(lon, lat);
                if (cellIndexSet.add(cellIndex)) {
                    pointList.add(new Point(lon, lat));
                }
            }
        }

        return new ObservationPointList(pointList.toArray(new Point[pointList.size()]));
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
            return LightBufrFile.this.grid.getCellIndex(lon, lat);
        }

        @Override
        public byte getValue(long cellIndex, byte noDataValue) {
            final int index = LightBufrFile.this.arrayIndexes[(int) cellIndex];
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
            final int index = LightBufrFile.this.arrayIndexes[(int) cellIndex];
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
            final int index = LightBufrFile.this.arrayIndexes[(int) cellIndex];
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
            final int index = LightBufrFile.this.arrayIndexes[(int) cellIndex];
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

    private static final class Grid {

        private final PlanetaryGrid grid;

        public Grid(PlanetaryGrid grid) {
            this.grid = grid;
        }

        public int getCellCount() {
            return (int) grid.getNumBins();
        }

        public int getCellIndex(double lon, double lat) {
            return (int) grid.getBinIndex(lat, lon);
        }
    }

    private static class ObservationPointList implements PointList {

        private final Point[] points;

        public ObservationPointList(Point[] points) {
            this.points = points;
        }

        @Override
        public int getElementCount() {
            return points.length;
        }

        @Override
        public double getLon(int i) throws IOException {
            return points[i].getLon();
        }

        @Override
        public double getLat(int i) throws IOException {
            return points[i].getLat();
        }
    }

    private static final class Accessor {

        private final Array array;
        private final Number missingValue;
        private final double addOffset;
        private final double scaleFactor;

        public Accessor(Variable variable) throws IOException {
            array = variable.read();
            missingValue = getAttributeValue(variable, ATTR_NAME_MISSING_VALUE);
            addOffset = getAttributeValue(variable, ATTR_NAME_ADD_OFFSET, 0.0);
            scaleFactor = getAttributeValue(variable, ATTR_NAME_SCALE_FACTOR, 1.0);
        }

        public int getElementCount() {
            return (int) array.getSize();
        }

        public boolean isValid(int i) {
            return missingValue == null || array.getDouble(i) != missingValue;
        }

        public double getDouble(int i) {
            return array.getDouble(i) * scaleFactor + addOffset;
        }
    }

    private static final class Point {

        private final double lon;
        private final double lat;

        public Point(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
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

}
