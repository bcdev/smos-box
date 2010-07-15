package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
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
    public void startFile(SmosFile sourceFile) throws IOException {
        final File sourceDblFile = sourceFile.getDblFile();
        targetDblFile = getTargetDblFile(sourceDblFile);
        final DataFormat targetFormat = sourceFile.getDataFormat();

        //noinspection ResultOfMethodCallIgnored
        targetDblFile.getParentFile().mkdirs();
        targetContext = targetFormat.createContext(targetDblFile, "rw");
        targetGridPointHandler = new EEExportGridPointHandler(targetContext);
    }

    @Override
    public void stopFile(SmosFile sourceFile) throws IOException {
        final long gridPointCount = targetGridPointHandler.getGridPointCount();
        try {
            final File sourceHdrFile = getSourceHdrFile(sourceFile.getDblFile());
            targetHdrFile = getTargetHdrFile(sourceHdrFile);

            final EEHdrFilePatcher patcher = new EEHdrFilePatcher();
            patcher.setGridPointCount(gridPointCount);
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
            renameFiles();
        } finally {
            try {
                close();
            } catch (IOException e) {
                // ignore
            }
            if (gridPointCount == 0) {
                final File parentDir = targetHdrFile.getParentFile();
                //noinspection ResultOfMethodCallIgnored
                targetHdrFile.delete();
                //noinspection ResultOfMethodCallIgnored
                targetDblFile.delete();
                //noinspection ResultOfMethodCallIgnored
                parentDir.delete();
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
        final File parentDir = targetHdrFile.getParentFile();
        final File newHdrFile = new File(parentDir, fileNamePatcher.getHdrFileName());
        renameFile(targetHdrFile, newHdrFile);
        final File newDblFile = new File(parentDir, fileNamePatcher.getDblFileName());
        renameFile(targetDblFile, newDblFile);
        final File newParentDir = new File(parentDir.getParent(), fileNamePatcher.getFileNameWithoutExtension());
        renameFile(parentDir, newParentDir);

        targetHdrFile = new File(newParentDir, fileNamePatcher.getHdrFileName());
        targetDblFile = new File(newParentDir, fileNamePatcher.getDblFileName());
    }

    private void renameFile(File oldFile, File newFile) throws IOException {
        if (!oldFile.renameTo(newFile)) {
            throw new IOException(String.format(
                    "Cannot rename file \n'%s'\nto\n'%s'.", oldFile.getPath(), newFile.getPath()));
        }
    }

    private FileNamePatcher createFileNamePatcher() {
        final String basename = FileUtils.getFilenameWithoutExtension(targetDblFile);
        final FileNamePatcher patcher = new FileNamePatcher(basename);
        if (targetGridPointHandler.hasValidPeriod()) {
            patcher.setStartDate(targetGridPointHandler.getSensingStart());
            patcher.setStopDate(targetGridPointHandler.getSensingStop());
        }
        return patcher;
    }

    private File getSourceHdrFile(File sourceDblFile) {
        return FileUtils.exchangeExtension(sourceDblFile, ".HDR");
    }

    private File getTargetDblFile(File sourceDblFile) {
        return new File(new File(targetDirectory, sourceDblFile.getParentFile().getName()), sourceDblFile.getName());
    }

    private File getTargetHdrFile(File sourceHdrFile) {
        return new File(new File(targetDirectory, sourceHdrFile.getParentFile().getName()), sourceHdrFile.getName());
    }
}
