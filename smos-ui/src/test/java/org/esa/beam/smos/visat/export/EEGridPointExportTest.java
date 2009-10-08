package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CollectionData;
import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.util.ByteArrayIOHandler;
import com.bc.ceres.core.ProgressMonitor;
import static junit.framework.Assert.assertEquals;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class EEGridPointExportTest {

    private static final String SCENARIO_27_DBL_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.DBL";
    private static final String SCENARIO_27_HDR_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.HDR";

    @Test
    public void exportScenario27() throws URISyntaxException, IOException {
        final File dblFile = getResourceAsFile(SCENARIO_27_DBL_NAME);
        final File hdrFile = getResourceAsFile(SCENARIO_27_HDR_NAME);
        final DataFormat dblFormat = SmosFormats.getFormat(hdrFile);
        final SmosFile sourceFile = new SmosFile(dblFile, dblFormat);

        final SequenceData sourceGridPointList = sourceFile.getGridPointList();
        assertEquals(5533, sourceGridPointList.getElementCount());
        assertEquals("Grid_Point_Data_Type[5533]", sourceGridPointList.getType().getName());

        final DataContext targetContext = dblFormat.createContext(new ByteArrayIOHandler());
        final RegionalGridPointFilter targetFilter = new RegionalGridPointFilter(sourceFile.getRegion());
        final GridPointHandler handler = new GridPointHandler(targetContext, targetFilter);

        for (int i = 0; i < sourceGridPointList.getElementCount(); i++) {
            final CompoundData gridPointData = sourceGridPointList.getCompound(i);
            handler.handleGridPoint(i, gridPointData);
        }

        final CompoundData targetData = targetContext.createData();
        targetContext.dispose();

        final SequenceData targetGridPointList = targetData.getSequence(SmosFormats.GRID_POINT_LIST_NAME);
        assertEquals(5533, targetGridPointList.getElementCount());
    }

    public static void main(String[] args) {
        File inputfile = new File(args[0]);
        File outputDir = new File(".");
        Area area = new Area(new Rectangle2D.Double(92, 7, 2, 2));
//        PrintWriter printWriter;
//        try {
//            printWriter = new PrintWriter(new File(outputDir, inputfile.getName() + ".csv"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return;
//        }

//        GridPointFilterStream exportStream = new CsvExport(printWriter, ";");
        GridPointFilterStream exportStream = new EEGridPointExport(outputDir);
        GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(exportStream, area);

        ProductReader smosProductReader = ProductIO.getProductReader("SMOS");
        ProductReaderPlugIn readerPlugIn = smosProductReader.getReaderPlugIn();
        DecodeQualification qualification = readerPlugIn.getDecodeQualification(inputfile);

        if (qualification.equals(DecodeQualification.INTENDED)) {
            try {
                Product product = smosProductReader.readProductNodes(inputfile, null);

                streamHandler.processProduct(product, ProgressMonitor.NULL);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Not a SMOS product: " + inputfile.getAbsolutePath());
        }
    }


    private File getResourceAsFile(String name) throws URISyntaxException {
        final URL url = getClass().getResource(name);
        final URI uri = url.toURI();

        return new File(uri);
    }

    static interface GridPointFilter {

        boolean accept(int id, CompoundData gridPointData) throws IOException;
    }

    static class RegionalGridPointFilter implements GridPointFilter {

        private final Shape region;

        RegionalGridPointFilter(Shape region) {
            this.region = region;
        }

        @Override
        public boolean accept(int id, CompoundData gridPointData) throws IOException {
            final double lat = gridPointData.getDouble(SmosFormats.GRID_POINT_LAT_NAME);
            final double lon = gridPointData.getDouble(SmosFormats.GRID_POINT_LON_NAME);

            return region.contains(lon, lat);
        }
    }

    static class GridPointHandler {

        private final DataContext targetContext;
        private final GridPointFilter targetFilter;

        private long gridPointCount;
        private long gridPointDataPosition;

        GridPointHandler(DataContext targetContext, GridPointFilter targetFilter) throws IOException {
            this.targetContext = targetContext;
            this.targetFilter = targetFilter;
        }

        public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
            if (gridPointCount == 0) {
                init(gridPointData.getParent());
            }
            if (targetFilter.accept(id, gridPointData)) {
                targetContext.getData().setLong(SmosFormats.GRID_POINT_COUNTER_NAME, ++gridPointCount);
                // ATTENTION: must be flushed before grid point data is written, don't know why (rq-20091008)
                targetContext.getData().flush();

                gridPointData.resolveSize();
                final long size = gridPointData.getSize();
                final byte[] bytes = new byte[(int) size];

                get(gridPointData, bytes);
                put(targetContext, bytes, gridPointDataPosition);
                gridPointDataPosition += size;
            }
        }

        private void init(CollectionData parent) throws IOException {
            final long parentPosition = parent.getPosition();
            copyBytes(parent.getContext(), targetContext, 0, parentPosition);

            targetContext.getData().setLong(SmosFormats.GRID_POINT_COUNTER_NAME, 0);
            targetContext.getData().flush();

            gridPointDataPosition = parentPosition;
        }

        private static void copyBytes(DataContext sourceContext,
                                      DataContext targetContext, long from, long to) throws IOException {
            final int segmentSize = 16384;
            byte[] bytes = new byte[segmentSize];

            for (long pos = from; pos < to; pos += segmentSize) {
                final long remainderSize = to - pos;
                if (remainderSize < segmentSize) {
                    bytes = new byte[(int) remainderSize];
                }

                get(sourceContext, bytes, pos);
                put(targetContext, bytes, pos);
            }
        }

        private static void get(CompoundData compoundData, byte[] bytes) throws IOException {
            compoundData.getContext().getHandler().read(compoundData.getContext(), bytes, compoundData.getPosition());
        }

        private static void get(DataContext sourceContext, byte[] bytes, long position) throws IOException {
            sourceContext.getHandler().read(sourceContext, bytes, position);
        }

        private static void put(DataContext targetContext, byte[] bytes, long position) throws IOException {
            targetContext.getHandler().write(targetContext, bytes, position);
        }
    }
}
