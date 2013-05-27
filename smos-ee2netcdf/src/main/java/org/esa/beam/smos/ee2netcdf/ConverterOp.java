package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import org.esa.beam.dataio.smos.DggFile;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.experimental.Output;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.util.io.WildcardMatcher;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

@SuppressWarnings("MismatchedReadAndWriteOfArray")
@OperatorMetadata(
        alias = ConverterOp.ALIAS,
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF format.")
public class ConverterOp extends Operator implements Output {

    public static final String ALIAS = "SmosEE2NetCDF";

    // input product types:
    // MIR_SM_BWxD1C
    // MIR_SM_BWxF1C
    // MIR_SM_SCxD1C
    // MIR_SM_SCxF1C
    // MIR_SM_OSUDP2
    // MIR_SM_SMUDP2

    @SourceProducts(type = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUDP2",
            description = "The source products to be converted. If not given, the parameter 'sourceProductPaths' must be provided.")
    private Product[] sourceProducts;

    @Parameter(description = "Comma-separated list of file paths specifying the source products.\n" +
            "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
            "'*' (matches any character sequence in path names) and\n" +
            "'?' (matches any single character).")
    private String[] sourceProductPaths;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    private File targetDirectory;

    @Parameter(description = "The geographical region as a geometry in well-known text format (WKT).",
            converter = JtsGeometryConverter.class)
    private Geometry region;

    @Parameter(defaultValue = "false",
            description = "Set true to overwrite already existing target files.")
    private boolean overwriteTarget;


    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();

        assertTargetDirectoryExists();

        if (sourceProducts != null) {
            for (Product sourceProduct : sourceProducts) {
                convertProduct(sourceProduct);
            }
        }

        if (sourceProductPaths != null) {
            final TreeSet<File> sourceFileSet = createInputFileSet(sourceProductPaths);

            for (File inputFile : sourceFileSet) {
                convertFile(inputFile);
            }
        }
    }

    public static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc");
        return outFile;
    }

    // package access for testing only tb 2013-03-27
    static TreeSet<File> createInputFileSet(String[] sourceProductPaths) {
        final TreeSet<File> sourceFileSet = new TreeSet<File>();
        try {
            for (String sourceProductPath : sourceProductPaths) {
                sourceProductPath = sourceProductPath.trim();
                WildcardMatcher.glob(sourceProductPath, sourceFileSet);
            }
        } catch (IOException e) {
            throw new OperatorException(e.getMessage());
        }
        return sourceFileSet;
    }

    // package access for testing only tb 2013-03-26
    static MultiPolygon convertToPolygon(Area dataArea) {
        final PathIterator pathIterator = dataArea.getPathIterator(null);
        final ArrayList<double[]> coordList = new ArrayList<double[]>();
        final ArrayList<Polygon> polygonList = new ArrayList<Polygon>();
        final GeometryFactory geometryFactory = new GeometryFactory();

        while (!pathIterator.isDone()) {
            final double[] coords = new double[6];
            final int segType = pathIterator.currentSegment(coords);
            if (segType == PathIterator.SEG_CLOSE) {
                coordList.add(coordList.get(0));
                final Coordinate[] coordinates = convert(coordList);

                final Polygon polygon = geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates), null);
                polygonList.add(polygon);

                coordList.clear();
            } else {
                coordList.add(coords);
            }
            pathIterator.next();
        }

        final Polygon[] polygons = polygonList.toArray(new Polygon[polygonList.size()]);
        return new MultiPolygon(polygons, geometryFactory);
    }

    // package access for testing tb 2013-03-26
    static Coordinate[] convert(ArrayList<double[]> coordList) {
        final Coordinate[] coordinates = new Coordinate[coordList.size()];
        for (int i = 0; i < coordinates.length; i++) {
            final double[] coord = coordList.get(i);
            coordinates[i] = new Coordinate(coord[0], coord[1]);
        }
        return coordinates;
    }

    // package access for testing only tb 2013-03-26
    static Rectangle getDataBoundingRect(Product sourceProduct, Geometry dataArea) throws IOException {
        final GeoCoding geoCoding = sourceProduct.getGeoCoding();
        final GeoPos geoPos = new GeoPos(0.f, 0.f);
        final PixelPos pixelPos = new PixelPos(0.f, 0.f);
        double min_x = Integer.MAX_VALUE;
        double max_x = Integer.MIN_VALUE;
        double min_y = Integer.MAX_VALUE;
        double max_y = Integer.MIN_VALUE;

        final Coordinate[] coordinates = dataArea.getCoordinates();
        for (final Coordinate coordinate : coordinates) {
            geoPos.setLocation((float) coordinate.y, (float) coordinate.x);
            geoCoding.getPixelPos(geoPos, pixelPos);
            double ceil = Math.ceil(pixelPos.x);
            if (ceil > max_x) {
                max_x = ceil;
            }

            double floor = Math.floor(pixelPos.x);
            if (floor < min_x) {
                min_x = floor;
            }

            ceil = Math.ceil(pixelPos.y);
            if (ceil > max_y) {
                max_y = ceil;
            }

            floor = Math.floor(pixelPos.y);
            if (floor < min_y) {
                min_y = floor;
            }
        }

        return new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y));
    }

    // package access for testing only - tb 2013-03-27
    static ProductSubsetDef createSubsetDef(Rectangle rectangle) {
        final ProductSubsetDef subsetDef = new ProductSubsetDef();
        subsetDef.setRegion(rectangle);
        return subsetDef;
    }

    private void assertTargetDirectoryExists() {
        if (!targetDirectory.isDirectory()) {
            if (!targetDirectory.mkdirs()) {
                throw new OperatorException("Unable to create target directory: " + targetDirectory.getAbsolutePath());
            }
        }
    }

    private void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }

    private void convertFile(File inputFile) {
        try {
            final Product product = ProductIO.readProduct(inputFile);
            if (product != null) {
                convertProduct(product);
            } else {
                getLogger().warning("Unable to open file: " + inputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            getLogger().severe("Failed to convert file: " + inputFile.getAbsolutePath());
            getLogger().severe(e.getMessage());
        }
    }

    private void convertProduct(Product sourceProduct) {
        try {
            final SmosProductReader productReader = (SmosProductReader) sourceProduct.getProductReader();
            final ExplorerFile explorerFile = productReader.getExplorerFile();

            final Area dataArea = DggFile.computeArea((DggFile) explorerFile);
            Geometry polygon = convertToPolygon(dataArea);

            if (region != null) {
                polygon = region.intersection(polygon);
            }

            if (polygon.isEmpty()) {
                getLogger().info("No geometric intersection: " + sourceProduct.getFileLocation());
                return;
            }

            final Rectangle x_y_subset = getDataBoundingRect(sourceProduct, polygon);
            final ProductSubsetDef subsetDef = createSubsetDef(x_y_subset);
            final Product subset = sourceProduct.createSubset(subsetDef, "", "");

            final File outFile = getOutputFile(explorerFile.getDblFile(), targetDirectory);
            if (outFile.isFile() && overwriteTarget) {
                if (!outFile.delete()) {
                    throw new IOException("Unable to delete already existing product: " + outFile.getAbsolutePath());
                }
            }
            if (!outFile.createNewFile()) {
                throw new IOException("Unable to create target product: " + outFile.getAbsolutePath());
            }

            ProductIO.writeProduct(subset, outFile, "NetCDF4-CF", false);
            getLogger().info("Successfully converted: " + sourceProduct.getFileLocation());
        } catch (IOException e) {
            getLogger().severe("Failed to convert file: " + sourceProduct.getFileLocation());
            getLogger().severe(e.getMessage());
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(ConverterOp.class);
        }
    }
}
