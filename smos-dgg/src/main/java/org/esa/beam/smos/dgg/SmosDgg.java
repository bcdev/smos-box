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

package org.esa.beam.smos.dgg;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.glevel.TiledFileMultiLevelSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Provides a {@link com.bc.ceres.glevel.MultiLevelImage multi level image} of
 * the SMOS Discrete global grid.
 *
 * @author Marco Peters
 * @author Ralf Quast
 * @version $Revision: $ $Date: $
 * @since SMOS-Box 1.0
 */
public class SmosDgg {

    private static final String SMOS_DGG_DIR_PROPERTY_NAME = "org.esa.beam.smos.dggDir";

    private static final int A = 1000000;
    private static final int B = 262144;
    private static final int C = B + 1;
    private static final int D = A - B;

    public static final int MIN_GRID_POINT_ID = 1;
    public static final int MAX_GRID_POINT_ID = 9262145;
    public static final int MAX_SEQNUM = 2621442;
    public static final int MAX_ZONE_ID = 10;

    private volatile MultiLevelImage dggMultiLevelImage;

    public static SmosDgg getInstance() {
        return Holder.instance;
    }

    public static int gridPointIdToSeqnum(int gridPointId) {
        return gridPointId < A ? gridPointId : gridPointId - D * ((gridPointId - 1) / A) + 1;
    }

    static int gridPointIdToSeqnumInZone(int gridPointId) {
        return gridPointId % A;
    }

    static int gridPointIdToZoneId(int gridPointId) {
        return gridPointId / A + 1;
    }

    static int seqnumToGridPointId(int seqnum) {
        return seqnum <= C ? seqnum : seqnum == MAX_SEQNUM ? MAX_GRID_POINT_ID : seqnum - 1 + ((seqnum - 2) / B) * D;
    }

    public static int seqnumToSeqnumInZone(int seqnum) {
        return seqnum <= C ? seqnum : seqnum == MAX_SEQNUM ? C : (seqnum - 2) % B + 1;
    }

    public static int seqnumToZoneId(int seqnum) {
        return seqnum <= C ? 1 : seqnum == MAX_SEQNUM ? MAX_ZONE_ID : (seqnum - 2) / B + 1;
    }

    public MultiLevelImage getMultiLevelImage() {
        return dggMultiLevelImage;
    }

    private SmosDgg() {
        try {
            String dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            final File dir = new File(dirPath);
            final MultiLevelSource dggMultiLevelSource = TiledFileMultiLevelSource.create(dir);

            dggMultiLevelImage = new DefaultMultiLevelImage(dggMultiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS DDG multi-level image: {0}", e.getMessage()), e);
        }
    }

    private static String getDirPathFromModule() throws URISyntaxException {
        final URL url = SmosDgg.class.getResource("image.properties");
        final URI uri = url.toURI();

        return new File(uri).getParent();
    }

    private static String getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_DGG_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final File dir = new File(dirPath);
            if (!dir.canRead()) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir.getPath(), SMOS_DGG_DIR_PROPERTY_NAME));
            }
        }

        return dirPath;
    }

    // Initialization on demand holder idiom

    private static class Holder {

        private static final SmosDgg instance = new SmosDgg();
    }
}
