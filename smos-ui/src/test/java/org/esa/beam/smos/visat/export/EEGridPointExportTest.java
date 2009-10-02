package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.util.RandomAccessFileIOHandler;
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

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

        final SmosFile smosFile = new SmosFile(dblFile, dblFormat);
        final SequenceData gridPointSequence = smosFile.getGridPointList();

        final RandomAccessFile raf = new RandomAccessFile(dblFile, "r");
        final RandomAccessFileIOHandler inputHandler = new RandomAccessFileIOHandler(raf);
        final DataContext inputContext = dblFormat.createContext(inputHandler);
        final CompoundType gridPointType = smosFile.getGridPointType();

        final CompoundData inputData = inputContext.getData();
        assertEquals(1, inputData.getInt(0));
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


}
