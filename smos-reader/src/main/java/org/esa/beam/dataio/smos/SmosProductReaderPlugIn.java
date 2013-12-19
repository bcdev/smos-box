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
package org.esa.beam.dataio.smos;

import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.util.io.BeamFileFilter;
import org.esa.beam.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Plugin providing the SMOS product reader.
 */
public class SmosProductReaderPlugIn implements ProductReaderPlugIn {

    @Override
    public SmosProductReader createReaderInstance() {
        return new SmosProductReader(this);
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file = input instanceof File ? (File) input : new File(input.toString());

        final String fileName = file.getName();
        if (fileName.endsWith(".HDR") || fileName.endsWith(".DBL")) {
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
        } else if (SmosUtils.isCompressedFile(file)) {
            if (SmosUtils.isL1cType(fileName) || SmosUtils.isL2Type(fileName) || SmosUtils.isAuxECMWFType(fileName)) {
                return DecodeQualification.INTENDED;
            }

            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(file);
                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                final String name1 = entries.nextElement().getName();
                final String name2 = entries.nextElement().getName();
                if (name1.endsWith(".HDR") && name2.endsWith(".DBL")) {
                    return DecodeQualification.SUITABLE;
                }
                if (name1.endsWith(".DBL") && name2.endsWith(".HDR")) {
                    return DecodeQualification.SUITABLE;
                }
            } catch (IOException e) {
                return DecodeQualification.UNABLE;
            } catch (NoSuchElementException e) {
                return DecodeQualification.UNABLE;
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        // ignore
                    }
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
        return new String[]{".HDR", ".DBL", ".zip", ".ZIP"};
    }

    @Override
    public String getDescription(Locale locale) {
        return "SMOS Data Products";
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{"SMOS-EEF"};
    }

    @Override
    public BeamFileFilter getProductFileFilter() {
        return new BeamFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }
}
