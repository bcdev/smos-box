/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

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
        targetDblFile = getTargetDblFile(sourceFile.getDblFile());
        targetHdrFile = getTargetHdrFile(sourceFile.getHdrFile());
        final DataFormat targetFormat = sourceFile.getDataFormat();

        //noinspection ResultOfMethodCallIgnored
        targetDblFile.getParentFile().mkdirs();
        targetContext = targetFormat.createContext(targetDblFile, "rw");
        targetGridPointHandler = new EEExportGridPointHandler(targetContext);
    }

    @Override
    public void stopFile(SmosFile sourceFile) throws IOException {
        final long gridPointCount = targetGridPointHandler.getGridPointCount();
        final boolean validPeriod = targetGridPointHandler.hasValidPeriod();
        final boolean validArea = targetGridPointHandler.hasValidArea();
        final FileNamePatcher fileNamePatcher =
                new FileNamePatcher(FileUtils.getFilenameWithoutExtension(targetDblFile));
        final EEHdrFilePatcher hdrFilePatcher = new EEHdrFilePatcher();
        hdrFilePatcher.setFileName(fileNamePatcher.getFileNameWithoutExtension());
        hdrFilePatcher.setGridPointCount(gridPointCount);

        try {
            if (validPeriod) {
                fileNamePatcher.setStartDate(targetGridPointHandler.getSensingStart());
                fileNamePatcher.setStopDate(targetGridPointHandler.getSensingStop());
                hdrFilePatcher.setSensingPeriod(targetGridPointHandler.getSensingStart(),
                                                targetGridPointHandler.getSensingStop());
            }
            if (validArea) {
                hdrFilePatcher.setArea(targetGridPointHandler.getArea());
            }
            hdrFilePatcher.patch(sourceFile.getHdrFile(), targetHdrFile);
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
            } else {
                renameFiles(fileNamePatcher);
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

    private void renameFiles(FileNamePatcher fileNamePatcher) throws IOException {
        final File oldDir = targetHdrFile.getParentFile();
        final File newDir = new File(oldDir.getParent(), fileNamePatcher.getFileNameWithoutExtension());
        renameFile(oldDir, newDir);
        final File newHdrFile = new File(newDir, fileNamePatcher.getHdrFileName());
        final File oldHdrFile = new File(newDir, targetHdrFile.getName());
        renameFile(oldHdrFile, newHdrFile);
        final File newDblFile = new File(newDir, fileNamePatcher.getDblFileName());
        final File oldDblFile = new File(newDir, targetDblFile.getName());
        renameFile(oldDblFile, newDblFile);

        targetHdrFile = newHdrFile;
        targetDblFile = newDblFile;
    }

    private void renameFile(File oldFile, File newFile) throws IOException {
        final String oldPath = oldFile.getPath();
        final String newPath = newFile.getPath();
        if (!newFile.equals(oldFile)) {
            if (newFile.exists()) {
                throw new IOException(String.format(
                        "File \n'%s'\ncould not be renamed to\n'%s'\nbecause the latter was already existing.",
                        oldPath, newPath));
            }
            if (!oldFile.renameTo(newFile)) {
                throw new IOException(String.format(
                        "File \n'%s'\ncould not be renamed to\n'%s'.", oldPath, newPath));
            }
        }
    }

    private File getTargetDblFile(File sourceDblFile) {
        return new File(new File(targetDirectory, sourceDblFile.getParentFile().getName()), sourceDblFile.getName());
    }

    private File getTargetHdrFile(File sourceHdrFile) {
        return new File(new File(targetDirectory, sourceHdrFile.getParentFile().getName()), sourceHdrFile.getName());
    }
}
