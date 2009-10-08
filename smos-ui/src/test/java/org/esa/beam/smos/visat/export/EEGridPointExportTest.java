package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.IOHandler;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.SequenceType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.binio.util.ByteArrayCodec;
import com.bc.ceres.binio.util.ByteArrayIOHandler;
import com.bc.ceres.binio.util.DataPrinter;
import com.bc.ceres.binio.util.RandomAccessFileIOHandler;
import com.bc.ceres.core.ProgressMonitor;
import static junit.framework.Assert.assertEquals;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import java.awt.Rectangle;
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

        final RandomAccessFile raf = new RandomAccessFile(dblFile, "r");
        final IOHandler iHandler = new RandomAccessFileIOHandler(raf);
        final IOHandler oHandler = new ByteArrayIOHandler();

        final DataContext iContext = dblFormat.createContext(iHandler);
        final DataContext oContext = dblFormat.createContext(oHandler);

        final CompoundData iData = iContext.getData();
        assertEquals("Data_Block", iData.getType().getName());
        final CompoundData oData = oContext.getData();

        final SequenceData iSequence = iData.getSequence(SmosFormats.GRID_POINT_LIST_NAME);
        assertEquals(5533, iData.getInt(SmosFormats.GRID_POINT_COUNTER_NAME));
        assertEquals(5533, iSequence.getElementCount());
        assertEquals("Grid_Point_Data_Type[5533]", iSequence.getType().getName());

        long iPos = iSequence.getPosition();
        long oPos = iPos;
        byte[] data = new byte[(int) iPos];
        iContext.getHandler().read(oContext, data, 0);
        oContext.getHandler().write(oContext, data, 0);
        final long startPos = iPos;
        oContext.getHandler().write(oContext, new byte[]{0, 0, 0, 0}, iPos - 4);

        for (int count = 0, i = 0; i < iSequence.getElementCount(); i++) {
            final CompoundData inCompound = iSequence.getCompound(i);
            iSequence.getCompound(i + 1);
            final long inSize = inCompound.getSize();
            data = new byte[(int) inSize];
            final double lat = inCompound.getDouble(SmosFormats.GRID_POINT_LATITUDE_NAME);
            final double lon = inCompound.getDouble(SmosFormats.GRID_POINT_LONGITUDE_NAME);
            if (new Rectangle.Double(0.0, 0.0, 10.0, 10.0).contains(lon, lat)) {
                iContext.getHandler().read(iContext, data, iPos);
                final byte[] counterBuffer = new byte[4];
                final ByteArrayCodec codec = ByteArrayCodec.getInstance(dblFormat.getByteOrder());
                codec.setInt(counterBuffer, 0, ++count);
                oContext.getHandler().write(oContext, counterBuffer, startPos - 4);
                oContext.getHandler().write(oContext, data, oPos);
                oPos += inSize;
            }
            iPos += inSize;
        }

        oContext.dispose();
        final DataPrinter dataPrinter = new DataPrinter();
        dataPrinter.print(oContext.createData());
    }


    void copyCompound(CompoundData in, CompoundData out) throws IOException {
        CompoundType inCompoundType = in.getType();
        CompoundType outCompoundType = out.getType();
        if (!inCompoundType.equals(outCompoundType)) {
            throw new RuntimeException("boo");
        }
        final int memberCount = inCompoundType.getMemberCount();
        for (int i = 0; i < memberCount; i++) {
            CompoundMember member = inCompoundType.getMember(i);
            Type type = member.getType();
            if (type.isCompoundType()) {
                copyCompound(in.getCompound(i), out.getCompound(i));
            } else if (type.isSequenceType()) {
                copySequence(in.getSequence(i), out.getSequence(i));
            } else if (type.isSimpleType()) {
                out.setDouble(i, in.getDouble(i));
            }
        }
    }

    private void copySequence(SequenceData in, SequenceData out) throws IOException {
        SequenceType inSequenceType = in.getType();
        SequenceType outSequenceType = out.getType();
//        if (!inSequenceType.equals(outSequenceType)) {
//            System.out.println("inSequenceType = " + inSequenceType);
//            System.out.println("outSequenceType = " + outSequenceType);
//            throw new RuntimeException("boo");
//        }
        Type type = inSequenceType.getElementType();
        final int elementCount = inSequenceType.getElementCount();
        for (int i = 0; i < elementCount; i++) {
            if (type.isCompoundType()) {
                copyCompound(in.getCompound(i), out.getCompound(i));
            } else if (type.isSequenceType()) {
                copySequence(in.getSequence(i), out.getSequence(i));
            } else if (type.isSimpleType()) {
                out.setDouble(i, in.getDouble(i));
            }
        }
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
