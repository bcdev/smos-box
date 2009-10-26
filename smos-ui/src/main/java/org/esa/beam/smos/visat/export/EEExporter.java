package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.SmosDggFile;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.util.io.FileUtils;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

/**
 * Example class or exporting subsets of SMOS data to EE format.
 */
public class EEExporter {

    private static final Shape TARGET_REGION = new Rectangle2D.Double(-170, 60, 20, 20);

    /**
     * Example for exporting subsets of SMOS data to EE format.
     *
     * @param args the pathname of a SMOS DBL file as first element,
     *             the path of the target directory as second element.
     */
    public static void main(String[] args) {
        final File sourceDblFile = new File(args[0]);
        final File sourceHdrFile = FileUtils.exchangeExtension(sourceDblFile, ".HDR");
        final File targetDirectory = new File(args[1]);

        // 1. get the format of the SMOS DBL file
        final DataFormat sourceDblFormat;
        try {
            sourceDblFormat = SmosFormats.getFormat(sourceHdrFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // 2. create a SMOS file
        final SmosDggFile smosFile;
        try {
            smosFile = new SmosDggFile(sourceDblFile, sourceDblFormat);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 3. process SMOS file
        try {
            final EEExportStream exportStream = new EEExportStream(targetDirectory);
            final SmosFileProcessor processor = new SmosFileProcessor(exportStream, TARGET_REGION);
            processor.process(smosFile, ProgressMonitor.NULL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
