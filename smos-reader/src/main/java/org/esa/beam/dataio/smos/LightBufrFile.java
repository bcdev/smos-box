package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.binning.PlanetaryGrid;
import org.esa.beam.binning.support.ReducedGaussianGrid;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.dataio.netcdf.util.MetadataUtils;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ralf Quast
 */
class LightBufrFile implements ProductFile {

    private static final String ATTR_NAME_MISSING_VALUE = "missing_value";
    private static final String ATTR_NAME_ADD_OFFSET = "add_offset";
    private static final String ATTR_NAME_SCALE_FACTOR = "scale_factor";
    private static final String VAR_NAME_LON = "Longitude_high_accuracy";
    private static final String VAR_NAME_LAT = "Latitude_high_accuracy";
    private static final String VAR_NAME_INCIDENCE_ANGLE = "Incidence_angle";
    private static final String VAR_NAME_POLARISATION = "Polarisation";
    private static final String VAR_NAME_SNAPSHOT_IDENTIFIER = "Snapshot_identifier";

    private static final double CENTER_BROWSE_INCIDENCE_ANGLE = 42.5;
    private static final double MIN_BROWSE_INCIDENCE_ANGLE = 37.5;
    private static final double MAX_BROWSE_INCIDENCE_ANGLE = 52.5;

    private final NetcdfFile ncfile;
    private final Grid grid;
    private final Area area;
    private final Map<Long, List<Integer>> indexMap;
    private final Accessor snapshotIdAccessor;
    private final Accessor polFlagsAccessor;
    private final Accessor incidenceAngleAccessor;
    private final Map<String, Array> arrayMap;
    private final CellValueCombinator cellValueCombinator;
    private final CellValueInterpolator cellValueInterpolator;

    public LightBufrFile(File file) throws IOException {
        ncfile = NetcdfFile.open(file.getPath());
        grid = new Grid(new ReducedGaussianGrid(512));

        final Sequence observationSequence = getObservationSequence();
        final Accessor lonAccessor = new Accessor(observationSequence.findVariable(VAR_NAME_LON));
        final Accessor latAccessor = new Accessor(observationSequence.findVariable(VAR_NAME_LAT));
        final int elementCount = lonAccessor.getElementCount();

        final PointList pointList = createPointList(lonAccessor, latAccessor);
        area = DggUtils.computeArea(pointList);


        indexMap = new HashMap<>(pointList.getElementCount());
        for (int i = 0; i < elementCount; i++) {
            if (lonAccessor.isValid(i) && latAccessor.isValid(i)) {
                final double lon = lonAccessor.getDouble(i);
                final double lat = latAccessor.getDouble(i);
                final long cellIndex = grid.getCellIndex(lon, lat);
                if (!indexMap.containsKey(cellIndex)) {
                    indexMap.put(cellIndex, new ArrayList<Integer>(50));
                }
                indexMap.get(cellIndex).add(i);
            }
        }

        snapshotIdAccessor = new Accessor(observationSequence.findVariable(VAR_NAME_SNAPSHOT_IDENTIFIER));
        polFlagsAccessor = new Accessor(observationSequence.findVariable(VAR_NAME_POLARISATION));
        incidenceAngleAccessor = new Accessor(observationSequence.findVariable(VAR_NAME_INCIDENCE_ANGLE));

        arrayMap = new HashMap<>(15);
        cellValueCombinator = new CellValueCombinator();
        cellValueInterpolator = new CellValueInterpolator();
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
        setTimes(product);

        return product;
    }

    @Override
    public final Area getArea() {
        return new Area(area);
    }

    @Override
    public File getDataFile() {
        return new File(ncfile.getLocation());
    }

    private void addBands(Product product) throws IOException {
        final Sequence sequence = getObservationSequence();
        final Family<BandDescriptor> descriptors = Dddb.getInstance().getBandDescriptors("BUFR");
        for (final BandDescriptor d : descriptors.asList()) {
            final Variable v = sequence.findVariable(d.getMemberName());
            if (v.getDataType().isEnum()) {
                final int dataType = ProductData.TYPE_UINT8;
                addBand(product, v, dataType, d);
            } else {
                final int dataType = DataTypeUtils.getRasterDataType(v);
                if (dataType != -1) {
                    addBand(product, v, dataType, d);
                }
            }
        }
    }

    private void addBand(Product product, Variable variable, int dataType, BandDescriptor descriptor) throws
                                                                                                      IOException {
        if (!descriptor.isVisible()) {
            return;
        }
        final Band band = product.addBand(descriptor.getBandName(), dataType);
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
        if (!descriptor.getValidPixelExpression().isEmpty()) {
            band.setValidPixelExpression(descriptor.getValidPixelExpression());
        }
        if (!descriptor.getDescription().isEmpty()) {
            band.setDescription(descriptor.getDescription());
        }
        if (descriptor.getFlagDescriptors() != null) {
            ProductHelper.addFlagsAndMasks(product, band, descriptor.getFlagCodingName(),
                                           descriptor.getFlagDescriptors());
        }

        final CellValueProvider valueProvider = createCellValueProvider(variable, descriptor.getPolarization());
        band.setSourceImage(createSourceImage(band, valueProvider));
        band.setImageInfo(ProductHelper.createImageInfo(band, descriptor));
    }

    private void setTimes(Product product) {
        // TODO - implement
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

    private CellValueProvider createCellValueProvider(Variable variable, int polarization) throws IOException {
        if ("SMOS_information_flag".equals(variable.getShortName())) {
            return new CellValueProviderImpl(variable, polarization, cellValueCombinator);
        } else {
            return new CellValueProviderImpl(variable, polarization, cellValueInterpolator);
        }
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

    private final class CellValueProviderImpl implements CellValueProvider {

        private final Variable variable;
        private final int polarization;
        private final CellValueAccumulator cellValueAccumulator;

        private volatile Array array;
        private volatile long snapshotId;

        private CellValueProviderImpl(Variable variable, int polarization,
                                      CellValueAccumulator cellValueAccumulator) {
            this.variable = variable;
            this.polarization = polarization;
            this.cellValueAccumulator = cellValueAccumulator;
            snapshotId = -1;
        }

        public final long getSnapshotId() { // TODO: why is this not synchronized?
            return snapshotId;
        }

        public final void setSnapshotId(long snapshotId) { // TODO: why is this not synchronized?
            this.snapshotId = snapshotId;
        }


        private Array getArray() throws IOException {
            if (array == null) {
                synchronized (this) {
                    final String variableName = variable.getShortName();
                    synchronized (arrayMap) {
                        if (!arrayMap.containsKey(variableName)) {
                            arrayMap.put(variableName, variable.read());
                        }
                        array = arrayMap.get(variableName);
                    }
                }
            }
            return array;
        }

        @Override
        public Area getArea() {
            // TODO: implement area for snapshots - rq20140512
            return LightBufrFile.this.getArea();
        }

        @Override
        public long getCellIndex(double lon, double lat) {
            return LightBufrFile.this.grid.getCellIndex(lon, lat);
        }

        @Override
        public byte getValue(long cellIndex, byte noDataValue) {
            final List<Integer> indexes = LightBufrFile.this.indexMap.get(cellIndex);
            if (indexes == null) {
                return noDataValue;
            }
            try {
                if (snapshotId == -1) {
                    return cellValueAccumulator.accumulate(cellIndex, getArray(), polarization).byteValue();
                } else {
                    return getSnapshotValue(cellIndex, noDataValue).byteValue();
                }
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public short getValue(long cellIndex, short noDataValue) {
            final List<Integer> indexes = LightBufrFile.this.indexMap.get(cellIndex);
            if (indexes == null) {
                return noDataValue;
            }
            try {
                if (snapshotId == -1) {
                    return cellValueAccumulator.accumulate(cellIndex, getArray(), polarization).shortValue();
                } else {
                    return getSnapshotValue(cellIndex, noDataValue).shortValue();
                }
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public int getValue(long cellIndex, int noDataValue) {
            final List<Integer> indexes = LightBufrFile.this.indexMap.get(cellIndex);
            if (indexes == null) {
                return noDataValue;
            }
            try {
                if (snapshotId == -1) {
                    return cellValueAccumulator.accumulate(cellIndex, getArray(), polarization).intValue();
                } else {
                    return getSnapshotValue(cellIndex, noDataValue).intValue();
                }
            } catch (IOException e) {
                return noDataValue;
            }
        }

        @Override
        public float getValue(long cellIndex, float noDataValue) {
            final List<Integer> indexes = LightBufrFile.this.indexMap.get(cellIndex);
            if (indexes == null) {
                return noDataValue;
            }
            try {
                if (snapshotId == -1) {
                    return cellValueAccumulator.accumulate(cellIndex, getArray(), polarization).floatValue();
                } else {
                    return getSnapshotValue(cellIndex, noDataValue).floatValue();
                }
            } catch (IOException e) {
                return noDataValue;
            }
        }

        private Number getSnapshotValue(long cellIndex, Number noDataValue) throws IOException {
            final List<Integer> indexList = indexMap.get(cellIndex);

            for (final Integer index : indexList) {
                if (snapshotIdAccessor.isValid(index)) {
                    if (snapshotId == snapshotIdAccessor.getInt(index)) {
                        if (polFlagsAccessor.isValid(index)) {
                            final int polFlags = polFlagsAccessor.getInt(index);
                            if (polarization == 4 || // for flags (they do not depend on polarisation)
                                polarization == (polFlags & 1) || // for x or y polarisation (dual pol)
                                (polarization & polFlags & 2) != 0) { // for xy polarisation (full pol, real and imaginary)
                                return (Number) getArray().getObject(index);
                            }
                        }
                    }
                }
            }
            return noDataValue;
        }
    }

    private static final class Grid {

        private final PlanetaryGrid grid;

        public Grid(PlanetaryGrid grid) {
            this.grid = grid;
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
            return missingValue == null || array.getDouble(i) != missingValue.doubleValue();
        }

        public double getDouble(int i) {
            return array.getDouble(i) * scaleFactor + addOffset;
        }

        public int getInt(int i) {
            return array.getInt(i);
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

    private static interface CellValueAccumulator {

        Number accumulate(long cellIndex, Array array, int polarization) throws IOException;
    }

    private final class CellValueInterpolator implements CellValueAccumulator {

        @Override
        public Number accumulate(long cellIndex, Array array, int polarization) throws IOException {
            final List<Integer> indexList = indexMap.get(cellIndex);

            int count = 0;
            double sx = 0;
            double sy = 0;
            double sxx = 0;
            double sxy = 0;

            boolean hasLower = false;
            boolean hasUpper = false;

            for (final Integer index : indexList) {
                if (polFlagsAccessor.isValid(index) && incidenceAngleAccessor.isValid(index)) {
                    final int polFlags = polFlagsAccessor.getInt(index);

                    if (polarization == 4 || polarization == (polFlags & 3) || (polarization & polFlags & 2) != 0) {
                        final double incidenceAngle = incidenceAngleAccessor.getDouble(index);

                        if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                            final double value = array.getDouble(index);

                            sx += incidenceAngle;
                            sy += value;
                            sxx += incidenceAngle * incidenceAngle;
                            sxy += incidenceAngle * value;
                            count++;

                            if (!hasLower) {
                                hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                            }
                            if (!hasUpper) {
                                hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                            }
                        }
                    }
                }
            }
            if (hasLower && hasUpper) {
                final double a = (count * sxy - sx * sy) / (count * sxx - sx * sx);
                final double b = (sy - a * sx) / count;
                return a * CENTER_BROWSE_INCIDENCE_ANGLE + b;
            }

            throw new IOException(MessageFormat.format(
                    "No data found for grid cell ''{0}'' and polarisation ''{1}''.", cellIndex, polarization));
        }
    }

    private final class CellValueCombinator implements CellValueAccumulator {

        @Override
        public Number accumulate(long cellIndex, Array array, int polarization) throws IOException {
            final List<Integer> indexList = indexMap.get(cellIndex);

            boolean hasLower = false;
            boolean hasUpper = false;
            int combinedFlags = 0;

            for (final Integer index : indexList) {
                if (polFlagsAccessor.isValid(index) && incidenceAngleAccessor.isValid(index)) {
                    final int polFlags = polFlagsAccessor.getInt(index);

                    if (polarization == 4 || polarization == (polFlags & 3) || (polarization & polFlags & 2) != 0) {
                        final double incidenceAngle = incidenceAngleAccessor.getDouble(index);

                        if (incidenceAngle >= MIN_BROWSE_INCIDENCE_ANGLE && incidenceAngle <= MAX_BROWSE_INCIDENCE_ANGLE) {
                            final int flags = array.getInt(index);

                            combinedFlags |= flags;

                            if (!hasLower) {
                                hasLower = incidenceAngle <= CENTER_BROWSE_INCIDENCE_ANGLE;
                            }
                            if (!hasUpper) {
                                hasUpper = incidenceAngle > CENTER_BROWSE_INCIDENCE_ANGLE;
                            }
                        }
                    }
                }
            }
            if (hasLower && hasUpper) {
                return combinedFlags;
            }

            throw new IOException(MessageFormat.format(
                    "No data found for grid cell ''{0}'' and polarisation ''{1}''.", cellIndex, polarization));
        }
    }

}
