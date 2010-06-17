/*
 * $Id: $
 *
 * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.io.BeamFileFilter;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * Plugin providing the SMOS product reader.
 */
public class SmosProductReaderPlugIn implements ProductReaderPlugIn {

    private static final FilenameFilter FILENAME_FILTER = new ExplorerFilenameFilter();

    @Override
    public SmosProductReader createReaderInstance() {
        return new SmosProductReader(this);
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        File file = input instanceof File ? (File) input : new File(input.toString());

        if (file.isDirectory()) {
            final File[] files = file.listFiles(FILENAME_FILTER);
            if (files != null && files.length == 2) {
                file = files[0];
            }
        }
        if (file.getName().endsWith(".HDR") || file.getName().endsWith(".DBL")) {
            final File hdrFile = FileUtils.exchangeExtension(file, ".HDR");
            final File dblFile = FileUtils.exchangeExtension(file, ".DBL");

            if (hdrFile.exists() && dblFile.exists()) {
                try {
                    if (Dddb.getInstance().getDataFormat(hdrFile) != null) {
                        return DecodeQualification.INTENDED;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        return new Class[]{File.class, String.class};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{".DBL"};
    }

    @Override
    public String getDescription(Locale locale) {
        return "SMOS Data Products";
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{"SMOS"};
    }

    @Override
    public BeamFileFilter getProductFileFilter() {
        return new BeamFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null)) {
            @Override
            public boolean isCompoundDocument(File dir) {
                return dir != null && dir.isDirectory() && dir.listFiles(FILENAME_FILTER).length == 2;
            }
        };
    }
}
