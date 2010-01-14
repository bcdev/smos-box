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
    private static final Shape TARGET_REGION = new Rectangle2D.Double(23.882095, 71.050095, 2.8358097, 0.8998108);

    //POLYGON((26.717905 71.5,26.710585 71.546,26.689098 71.590675,26.65416 71.63379,26.60648 71.675125,26.546772 71.714455,26.47575 71.75155,26.30261 71.81813,26.092764 71.87307,25.851913 71.91455,25.585758 71.940765,25.3 71.949905,25.014242 71.940765,24.748087 71.91455,24.507236 71.87307,24.29739 71.81813,24.12425 71.75155,24.053228 71.714455,23.99352 71.675125,23.94584 71.63379,23.910902 71.590675,23.889416 71.546,23.882095 71.5,23.889416 71.454,23.910902 71.409325,23.94584 71.36621,23.99352 71.324875,24.053228 71.285545,24.12425 71.24845,24.29739 71.18187,24.507236 71.12693,24.748087 71.08545,25.014242 71.059235,25.3 71.050095,25.585758 71.059235,25.851913 71.08545,26.092764 71.12693,26.30261 71.18187,26.47575 71.24845,26.546772 71.285545,26.60648 71.324875,26.65416 71.36621,26.689098 71.409325,26.710585 71.454,26.717905 71.5))

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
            smosFile = (SmosFile) SmosProductReader.createExplorerFile(sourceFile);
        } catch (Exception e) {
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
