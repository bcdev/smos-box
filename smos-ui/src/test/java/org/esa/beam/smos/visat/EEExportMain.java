package org.esa.beam.smos.visat;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.visat.export.GridPointFilterStream;
import org.esa.beam.smos.visat.export.GridPointFilterStreamHandler;
import org.esa.beam.smos.visat.export.EEGridExport;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class EEExportMain {

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

//        GridPointFilterStream exportStream = new CsvGridExport(printWriter, ";");
        GridPointFilterStream exportStream = new EEGridExport(outputDir);
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
}
