package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EEExportStream implements GridPointFilterStream {

    private final File targetDirectory;

    private DataContext targetContext;
    private EEExportGridPointHandler targetGridPointHandler;
    private File targetDblFile;
    private File targetHdrFile;

    public EEExportStream(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public void startFile(SmosFile sourceFile) throws FileNotFoundException {
        final File sourceDblFile = sourceFile.getDblFile();
        targetDblFile = getTargetDblFile(sourceDblFile);
        final DataFormat targetFormat = sourceFile.getDataFormat();

        targetContext = targetFormat.createContext(targetDblFile, "rw");
        targetGridPointHandler = new EEExportGridPointHandler(targetContext);
    }

    @Override
    public void stopFile(SmosFile sourceFile) throws IOException {
        try {
            final File sourceHdrFile = getSourceHdrFile(sourceFile.getDblFile());
            targetHdrFile = getTargetHdrFile(sourceHdrFile);

            final EEHdrFilePatcher patcher = new EEHdrFilePatcher();
            patcher.setGridPointCount(targetGridPointHandler.getGridPointCount());
            if (targetGridPointHandler.hasValidPeriod()) {
                final FileNamePatcher fileNamePatcher = createFileNamePatcher();
                patcher.setFileName(fileNamePatcher.getFileNameWithoutExtension());
                patcher.setSensingPeriod(targetGridPointHandler.getSensingStart(),
                                         targetGridPointHandler.getSensingStop());
            }
            if (targetGridPointHandler.hasValidArea()) {
                patcher.setArea(targetGridPointHandler.getArea());
            }
            patcher.patch(sourceHdrFile, targetHdrFile);
        } finally {
            renameFiles();
            try {
                close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        targetGridPointHandler.handleGridPoint(id, gridPointData);
    }

    @Override
    public void close() throws IOException {
        if (targetContext != null) {
            targetContext.dispose();
            targetContext = null;
        }
        targetGridPointHandler = null;
    }

    public File getTargetDblFile() {
        return targetDblFile;
    }

    public File getTargetHdrFile() {
        return targetHdrFile;
    }

    private void renameFiles() throws IOException {
        final FileNamePatcher fileNamePatcher = createFileNamePatcher();
        final File targetDir = targetHdrFile.getParentFile();
        final File newHdrFile = new File(targetDir, fileNamePatcher.getHdrFileName());
        renameFile(targetHdrFile, newHdrFile);
        final File newDblFile = new File(targetDir, fileNamePatcher.getDblFileName());
        renameFile(targetDblFile, newDblFile);
        targetHdrFile = newHdrFile;
        targetDblFile = newDblFile;
    }

    private void renameFile(File oldFile, File newFile) throws IOException {
        if (!oldFile.renameTo(newFile)) {
            throw new IOException(String.format(
                    "Failed to rename file from: '%s' to '%s'.", oldFile.getAbsolutePath(), newFile.getAbsolutePath()));
        }
    }

    private FileNamePatcher createFileNamePatcher() {
        final String filenameWOExtension = FileUtils.getFilenameWithoutExtension(targetDblFile);
        final FileNamePatcher fileNamePatcher = new FileNamePatcher(filenameWOExtension);
        if (targetGridPointHandler.hasValidPeriod()) {
            fileNamePatcher.setStartDate(targetGridPointHandler.getSensingStart());
            fileNamePatcher.setStopDate(targetGridPointHandler.getSensingStop());
        }
        return fileNamePatcher;
    }

    private File getSourceHdrFile(File sourceDblFile) {
        return FileUtils.exchangeExtension(sourceDblFile, ".HDR");
    }

    private File getTargetDblFile(File sourceDblFile) {
        return new File(targetDirectory, sourceDblFile.getName());
    }

    private File getTargetHdrFile(File sourceHdrFile) {
        return new File(targetDirectory, sourceHdrFile.getName());
    }
}
