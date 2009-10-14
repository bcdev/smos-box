package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class EEExportStream implements GridPointFilterStream {

    private final File targetDirectory;

    private DataContext targetContext;
    private GridPointHandler targetGridPointHandler;

    public EEExportStream(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public void startFile(SmosFile sourceFile) throws FileNotFoundException {
        final File sourceDblFile = sourceFile.getFile();
        final File targetDblFile = getTargetDblFile(sourceDblFile);
        final DataFormat targetFormat = sourceFile.getFormat();

        targetContext = targetFormat.createContext(targetDblFile, "rw");
        targetGridPointHandler = new EEExportGridPointHandler(targetContext);
    }

    @Override
    public void stopFile(SmosFile sourceFile) throws IOException {
        try {
            final File sourceHdrFile = getSourceHdrFile(sourceFile.getFile());
            final File targetHdrFile = getTargetHdrFile(sourceHdrFile);

            new EEHdrFilePatcher().patch(sourceHdrFile, targetHdrFile);
        } finally {
            dispose();
        }
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        targetGridPointHandler.handleGridPoint(id, gridPointData);
    }

    @Override
    public void close() throws IOException {
        dispose();
    }

    private void dispose() {
        targetGridPointHandler = null;
        if (targetContext != null) {
            targetContext.dispose();
        }
    }

    private String createTargetFileName(String sourceFileName) {
        // @todo 1 : tb/tb return target file name according to file specs and subset
        return sourceFileName;
    }

    private File getSourceHdrFile(File sourceDblFile) {
        return FileUtils.exchangeExtension(sourceDblFile, ".HDR");
    }

    private File getTargetDblFile(File sourceDblFile) {
        return new File(targetDirectory, createTargetFileName(sourceDblFile.getName()));
    }

    private File getTargetHdrFile(File sourceHdrFile) {
        return new File(targetDirectory, createTargetFileName(sourceHdrFile.getName()));
    }

}
