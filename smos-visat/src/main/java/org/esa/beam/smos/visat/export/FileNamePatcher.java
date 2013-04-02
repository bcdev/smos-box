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

import org.esa.beam.dataio.smos.util.DateTimeUtils;

import java.text.DecimalFormat;
import java.util.Date;

public class FileNamePatcher {
    private String newStartDate;
    private String oldStartDate;
    private String newStopDate;
    private String oldStopDate;
    private String prefix;
    private String version;
    private String oldCounter;
    private String suffix;
    private int fileCounter;

    public FileNamePatcher(String originalName) {
        prefix = originalName.substring(0, 19);
        oldStartDate = originalName.substring(19, 34);
        oldStopDate = originalName.substring(35, 50);
        version = originalName.substring(50, 55);
        oldCounter = originalName.substring(55, 58);
        suffix = originalName.substring(58, originalName.length());
    }

    public void setStartDate(Date startDate) {
        newStartDate = DateTimeUtils.toFileNameFormat(startDate);
    }

    public void setStopDate(Date stopDate) {
        newStopDate = DateTimeUtils.toFileNameFormat(stopDate);
    }

    public void setFileCounter(int counter) {
        fileCounter = counter;
    }

    public String getHdrFileName() {
        final StringBuffer buffer = getFileNameBufferWithoutExtension();
        buffer.append(".HDR");
        return buffer.toString();
    }

    public String getDblFileName() {
        final StringBuffer buffer = getFileNameBufferWithoutExtension();
        buffer.append(".DBL");
        return buffer.toString();
    }

    public String getFileNameWithoutExtension() {
        return getFileNameBufferWithoutExtension().toString();
    }

    private StringBuffer getFileNameBufferWithoutExtension() {
        final StringBuffer buffer = new StringBuffer(256);
        buffer.append(prefix);
        if (newStartDate != null) {
            buffer.append(newStartDate);
        } else {
            buffer.append(oldStartDate);
        }
        buffer.append("_");

        if (newStopDate != null) {
            buffer.append(newStopDate);
        } else {
            buffer.append(oldStopDate);
        }

        buffer.append(version);

        if (fileCounter != 0) {
            final DecimalFormat decimalFormat = new DecimalFormat("000");
            final String counterString = decimalFormat.format(fileCounter);
            final int length = counterString.length();
            buffer.append(counterString.substring(length - 3, length));
        } else {
            buffer.append(oldCounter);
        }

        buffer.append(suffix);
        return buffer;
    }
}
