/*
 * $Id: $
 * 
 * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation. This program is distributed in the hope it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.esa.beam.smos.visat.export;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;

import java.awt.Shape;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GridPointFilterStreamHandler {

    private final SmosFileProcessor smosFileProcessor;

    GridPointFilterStreamHandler(GridPointFilterStream filterStream, Shape area) {
        smosFileProcessor = new SmosFileProcessor(filterStream, area);
    }

    void processProduct(Product product, ProgressMonitor pm) throws IOException {
        final ProductReader productReader = product.getProductReader();
        if (productReader instanceof SmosProductReader) {
            final SmosProductReader smosProductReader = (SmosProductReader) productReader;
            final ExplorerFile explorerFile = smosProductReader.getExplorerFile();
            if (explorerFile instanceof SmosFile) {
                smosFileProcessor.process((SmosFile) explorerFile, pm);
            }
        }
    }

    void processDirectory(File dir, boolean recursive, ProgressMonitor pm) throws IOException {
        final List<File> sourceFileList = new ArrayList<File>();
        findSourceFiles(dir, recursive, sourceFileList);

        try {
            pm.beginTask("Exporting grid point data...", sourceFileList.size());
            for (final File sourceFile : sourceFileList) {
                ExplorerFile explorerFile = null;
                try {
                    try {
                        explorerFile = SmosProductReader.createExplorerFile(sourceFile);
                    } catch (IOException e) {
                        // ignore, file is skipped anyway
                    }
                    if (explorerFile instanceof SmosFile) {
                        smosFileProcessor.process((SmosFile) explorerFile, SubProgressMonitor.create(pm, 1));
                    } else {
                        // skip file
                        pm.worked(1);
                    }
                    if (pm.isCanceled()) {
                        throw new IOException("Export was cancelled by user.");
                    }
                } finally {
                    if (explorerFile != null) {
                        explorerFile.close();
                    }
                }
            }
        } finally {
            pm.done();
        }
    }

    private static void findSourceFiles(File parent, boolean recursive, List<File> sourceFileList) {
        final File[] dirs = parent.listFiles(DIRECTORY_FILTER);
        if (dirs.length == 0) {
            final File[] files = parent.listFiles(EE_FILENAME_FILTER);
            if (files.length == 2) {
                if (files[0].getName().endsWith(".DBL")) {
                    sourceFileList.add(files[0]);
                } else {
                    sourceFileList.add(files[1]);
                }
            }
            return;
        }
        for (final File dir : dirs) {
            final File[] files = dir.listFiles(EE_FILENAME_FILTER);
            if (files.length == 2) {
                if (files[0].getName().endsWith(".DBL")) {
                    sourceFileList.add(files[0]);
                } else {
                    sourceFileList.add(files[1]);
                }
            } else {
                if (recursive) {
                    findSourceFiles(dir, recursive, sourceFileList);
                }
            }
        }
    }

    private static final FileFilter DIRECTORY_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && file.canRead();
        }
    };

    private static final FilenameFilter EE_FILENAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.equals(dir.getName() + ".HDR") || name.equals(dir.getName() + ".DBL");
        }
    };
}
