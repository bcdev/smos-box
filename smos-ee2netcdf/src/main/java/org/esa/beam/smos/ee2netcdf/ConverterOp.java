package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.dataio.smos.DggFile;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.experimental.Output;
import org.esa.beam.gpf.operators.standard.SubsetOp;
import org.esa.beam.util.converters.JtsGeometryConverter;
import org.esa.beam.util.io.FileUtils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;

@OperatorMetadata(
        alias = "SmosEE2NetCDF",
        version = "0.1",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF format.")
public class ConverterOp extends Operator implements Output {

    // input product types:
    // MIR_SM_BWxD1C
    // MIR_SM_BWxF1C
    // MIR_SM_SCxD1C
    // MIR_SM_SCxF1C
    // MIR_SM_OSUDP2
    // MIR_SM_SMUDP2

    @SourceProducts(count = -1,
            type = "MIR_BW[LS][DF]1C|MIR_SC[LS][DF]1C|MIR_OSUDP2|MIR_SMUPD2")
    private Product[] sourceProducts;

    @Parameter(description = "The target directory for the converted data. If not existing, directory will be created.",
            defaultValue = ".",
            notEmpty = true,
            notNull = true)
    private File targetDirectory;

    @Parameter(description = "The geographical region as a geometry in well-known text format (WKT).",
            converter = JtsGeometryConverter.class)
    private Geometry region;

    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();

        assertTargetDirectoryExists();

        try {
            // @todo 2 tb/tb change so that a single file failure does not kill the complete batch-run tb 2013-03-21
            // @todo tb/tb attach logger tb 2013-03-25
            convertProducts();
        } catch (IOException e) {
            throw new OperatorException(e.getMessage());
        }
    }

    private void convertProducts() throws IOException {
        for (Product sourceProduct : sourceProducts) {
            final SmosProductReader productReader = (SmosProductReader) sourceProduct.getProductReader();
            final ExplorerFile explorerFile = productReader.getExplorerFile();

            final Area dataArea = DggFile.computeArea((DggFile) explorerFile);
            final Rectangle x_y_subset = getDataBoundingRect(sourceProduct, dataArea);

            if (region != null) {
                final Rectangle rectangle = SubsetOp.computePixelRegion(sourceProduct, region, 1);
//            private static com.vividsolutions.jts.geom.Polygon convertAwtPathToJtsPolygon(Path2D
//            path, GeometryFactory factory) {
//                final PathIterator pathIterator = path.getPathIterator(null);
//                ArrayList<double[]> coordList = new ArrayList<double[]>();
//                int lastOpenIndex = 0;
//                while (!pathIterator.isDone()) {
//                    final double[] coords = new double[6];
//                    final int segType = pathIterator.currentSegment(coords);
//                    if (segType == PathIterator.SEG_CLOSE) {
//                        // we should only detect a single SEG_CLOSE
//                        coordList.add(coordList.get(lastOpenIndex));
//                        lastOpenIndex = coordList.size();
//                    } else {
//                        coordList.add(coords);
//                    }
//                    pathIterator.next();
//                }
//                final Coordinate[] coordinates = new Coordinate[coordList.size()];
//                for (int i1 = 0; i1 < coordinates.length; i1++) {
//                    final double[] coord = coordList.get(i1);
//                    coordinates[i1] = new Coordinate(coord[0], coord[1]);
//                }
//
//                return factory.createPolygon(factory.createLinearRing(coordinates), null);
//            }
            }

            final ProductSubsetDef subsetDef = new ProductSubsetDef();
            subsetDef.setRegion(x_y_subset);
            final Product subset = sourceProduct.createSubset(subsetDef, "", "");

            final File outFile = getOutputFile(explorerFile.getDblFile(), targetDirectory);
            outFile.createNewFile();

            ProductIO.writeProduct(subset, outFile, "NetCDF4-CF", false);
        }
    }

    static Rectangle getDataBoundingRect(Product sourceProduct, Area dataArea) throws IOException {
        final GeoCoding geoCoding = sourceProduct.getGeoCoding();
        final GeoPos geoPos = new GeoPos(0.f, 0.f);
        final PixelPos pixelPos = new PixelPos(0.f, 0.f);
        double min_x = Integer.MAX_VALUE;
        double max_x = Integer.MIN_VALUE;
        double min_y = Integer.MAX_VALUE;
        double max_y = Integer.MIN_VALUE;
        int segmentType;
        final float[] coords = new float[6];
        final PathIterator iterator = dataArea.getPathIterator(null);
        while (!iterator.isDone()) {
            segmentType = iterator.currentSegment(coords);
            if (segmentType == PathIterator.SEG_MOVETO
                    || segmentType == PathIterator.SEG_LINETO
                    || segmentType == PathIterator.SEG_QUADTO
                    || segmentType == PathIterator.SEG_CLOSE) {

                geoPos.setLocation(coords[1], coords[0]);
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

            iterator.next();
        }

        return new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y));
    }

    // package access for testing only - tb 2013-03-21
    static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc");
        return outFile;
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
}
