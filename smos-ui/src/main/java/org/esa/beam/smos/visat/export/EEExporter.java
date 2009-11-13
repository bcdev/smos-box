package org.esa.beam.smos.visat.export;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

/**
 * Example class or exporting subsets of SMOS data to EE format.
 */
public class EEExporter {

    // POLYGON((120 45,150 45,150 0,120 0,120 45))
    private static final Shape TARGET_REGION = new Rectangle2D.Double(-140, -24, 280, 34);

    /**
     * Example for exporting subsets of SMOS data to EE format.
     *
     * @param args the pathname of a SMOS header or datablock file as first element,
     *             the path of the target directory as second element.
     */
    public static void main(String[] args) {
        final File sourceFile = new File(args[0]);
        final File targetDirectory = new File(args[1]);

        final SmosFile smosFile;
        try {
            smosFile = SmosProductReader.createSmosFile(sourceFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            final EEExportStream exportStream = new EEExportStream(targetDirectory);
            final SmosFileProcessor processor = new SmosFileProcessor(exportStream, TARGET_REGION);
            processor.process(smosFile, ProgressMonitor.NULL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
