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
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
class GridPointFilterStreamHandler {

    private final GridPointFilterStream filterStream;
    private final Shape area;
    private final SmosFileProcessor smosFileProcessor;

    GridPointFilterStreamHandler(GridPointFilterStream filterStream, Shape area) {
        this.filterStream = filterStream;
        this.area = area;
        smosFileProcessor = new SmosFileProcessor(filterStream, area);
    }

    void processProduct(Product product, ProgressMonitor pm) throws IOException {
        ProductReader productReader = product.getProductReader();
        if (productReader instanceof SmosProductReader) {
            SmosProductReader smosProductReader = (SmosProductReader) productReader;
            ExplorerFile smosFile = smosProductReader.getExplorerFile();
            if (smosFile instanceof SmosFile) {
                smosFileProcessor.process((SmosFile) smosFile, pm);
            }
        }
    }

    void processDirectory(File dir, boolean recursive, ProgressMonitor pm) throws IOException {
        List<File> fileList = new ArrayList<File>();
        scanDir(dir, recursive, fileList, 0);

        ProductReader smosProductReader = ProductIO.getProductReader("SMOS");
        ProductReaderPlugIn readerPlugIn = smosProductReader.getReaderPlugIn();
        pm.beginTask("Export grid cells", fileList.size());
        try {
            for (File file : fileList) {
                DecodeQualification qualification = readerPlugIn.getDecodeQualification(file);
                if (qualification.equals(DecodeQualification.INTENDED)) {
                    try {
                        Product product = smosProductReader.readProductNodes(file, null);
                        processProduct(product, SubProgressMonitor.create(pm, 1));
                    } catch (Exception e) {
                        // ignore
                    }
                } else {
                    pm.worked(1);
                }
                if (pm.isCanceled()) {
                    throw new IOException("Export cancled");
                }
            }
        } finally {
            pm.done();
        }
    }

    private void scanDir(File dir, boolean recursive, List<File> fileList, int depth) throws IOException {
        final File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (final File file : files) {
            if (file.isDirectory()) {
                if (recursive || depth < 1) {
                    scanDir(file, recursive, fileList, depth + 1);
                }
            } else {
                if (file.getName().endsWith(".HDR")) {
                    final File dblFile = FileUtils.exchangeExtension(file, ".DBL");
                    if (dblFile.exists()) {
                        fileList.add(file);
                    }
                }
            }
        }
    }

}
