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

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;

import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.util.io.FileUtils;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 1.0
 */
public class GridPointFilterStreamHandler {

    private final GridPointFilterStream filterStream;
    private final Area area;

    public GridPointFilterStreamHandler(GridPointFilterStream filterStream, Area area) {
        this.filterStream = filterStream;
        this.area = area;
    }

    public void processProduct(Product product, ProgressMonitor pm) throws IOException {
        ProductReader productReader = product.getProductReader();
        if (productReader instanceof SmosProductReader) {
            SmosProductReader smosProductReader = (SmosProductReader) productReader;
            SmosFile smosFile = smosProductReader.getSmosFile();
            handleSmosFile(smosFile, pm);
        }
    }

    public void processDirectory(File dir, boolean recursive, ProgressMonitor pm) throws IOException {
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
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
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

    private void handleSmosFile(SmosFile smosFile, ProgressMonitor pm) throws IOException {
        CompoundType gridPointType = smosFile.getGridPointType();
        final int latIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LATITUDE_NAME);
        final int lonIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LONGITUDE_NAME);

        filterStream.startFile(smosFile);
        final int gridPointCount = smosFile.getGridPointCount();
        pm.beginTask("Export grid cells", gridPointCount);
        try {
            for (int i = 0; i < gridPointCount; i++) {
                CompoundData gridPointData = smosFile.getGridPointData(i);
                double lat = gridPointData.getDouble(latIndex);
                double lon = gridPointData.getDouble(lonIndex);
                // normalisation to [-180, 180] necessary for some L1c test products
                if (lon > 180.0) {
                    lon = lon - 360.0;
                }
                if (area.contains(lon, lat)) {
                    filterStream.handleGridPoint(i, gridPointData);
                }
                pm.worked(1);
                if (pm.isCanceled()) {
                    throw new IOException("Export cancled");
                }
            }
        } finally {
            pm.done();
        }
        filterStream.stopFile(smosFile);
    }
}
