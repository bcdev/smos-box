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

package org.esa.beam.smos.lsmask;


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

public class SmosLsMask {

    private static final String SMOS_LS_MASK_DIR_PROPERTY_NAME = "org.esa.beam.smos.lsMaskDir";

    private final MultiLevelImage multiLevelImage;

    public static SmosLsMask getInstance() {
        return Holder.INSTANCE;
    }

    public MultiLevelImage getMultiLevelImage() {
        return multiLevelImage;
    }

    private SmosLsMask() {
        try {
            String dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            final File dir = new File(dirPath);
            final MultiLevelSource multiLevelSource = TiledFileMultiLevelSource.create(dir);

            multiLevelImage = new DefaultMultiLevelImage(multiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS Land/Sea Mask multi-level image: {0}", e.getMessage()), e);
        }
    }

    private static String getDirPathFromModule() throws URISyntaxException {
        final URL url = SmosLsMask.class.getResource("image.properties");
        final URI uri = url.toURI();

        return new File(uri).getParent();
    }

    private static String getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_LS_MASK_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final File dir = new File(dirPath);
            if (!dir.canRead()) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir.getPath(), SMOS_LS_MASK_DIR_PROPERTY_NAME));
            }
        }

        return dirPath;
    }

    // Initialization on demand holder idiom

    private static class Holder {

        private static final SmosLsMask INSTANCE = new SmosLsMask();
    }
}
