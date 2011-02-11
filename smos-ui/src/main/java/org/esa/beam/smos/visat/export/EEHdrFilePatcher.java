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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

class EEHdrFilePatcher {

    private Date sensingStart = null;
    private Date sensingStop = null;
    private String fileName = null;
    private Rectangle2D area = null;
    private long gridPointCount = 0;

    final void patch(File sourceHdrFile, File targetHdrFile) throws IOException {
        patch(new FileInputStream(sourceHdrFile), new FileOutputStream(targetHdrFile));
    }

    final void patch(InputStream sourceStream, OutputStream targetStream) throws IOException {
        final Document sourceDocument;
        try {
            sourceDocument = new SAXBuilder().build(sourceStream);
        } catch (JDOMException e) {
            throw new IOException(e.getMessage(), e);
        }
        final Document targetDocument = patchDocument(sourceDocument);
        try {
            final Format format = getFormat();
            final XMLOutputter serializer = new XMLOutputter(format);
            serializer.output(targetDocument, targetStream);
        } finally {
            targetStream.close();
        }
    }

    void setSensingPeriod(Date sensingStart, Date sensingStop) {
        this.sensingStart = sensingStart;
        this.sensingStop = sensingStop;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    void setArea(Rectangle2D area) {
        this.area = area;
    }

    void setGridPointCount(long gridPointCount) {
        this.gridPointCount = gridPointCount;
    }

    final Format getFormat() {
        return Format.getRawFormat().setOmitEncoding(true).setLineSeparator("\n");
    }

    Document patchDocument(Document document) {
        final Element element = document.getRootElement();
        final Namespace namespace = element.getNamespace();

        patchFixedHeader(element, namespace);
        patchVariableHeader(element, namespace);

        return document;
    }

    private void patchVariableHeader(Element element, Namespace namespace) {
        final Element variableHeader = element.getChild("Variable_Header", namespace);
        final Element specificHeader = variableHeader.getChild("Specific_Product_Header", namespace);
        final Element mainInfo = specificHeader.getChild("Main_Info", namespace);
        Element timeInfo = null;
        if (mainInfo != null) {
            // ECMWF aux files do not have this
            timeInfo = mainInfo.getChild("Time_Info", namespace);
        }

        if (sensingStart != null && timeInfo != null) {
            final Element validityStart = timeInfo.getChild("Precise_Validity_Start", namespace);
            validityStart.setText(DateTimeUtils.toVariableHeaderFormat(sensingStart));
        }
        if (sensingStop != null && timeInfo != null) {
            final Element validityStop = timeInfo.getChild("Precise_Validity_Stop", namespace);
            validityStop.setText(DateTimeUtils.toVariableHeaderFormat(sensingStop));
        }

        if (area != null) {
            final DecimalFormat numberFormat = createDecimalFormat();
            Element productLocation = specificHeader.getChild("Product_Location", namespace);
            if (productLocation == null) {
                // we maybe have a L2 file or an ECMWF aux file
                productLocation = specificHeader.getChild("L2_Product_Location", namespace);
            }
            patchGeolocation(namespace, numberFormat, productLocation);
        }

        if (gridPointCount > 0) {
            final NumberFormat numberFormat = new DecimalFormat("0000000000");
            final Element listOfDatasets = specificHeader.getChild("List_of_Data_Sets", namespace);
            final List children = listOfDatasets.getChildren();
            final Iterator dsIterator = children.iterator();
            long offset = 0;
            while (dsIterator.hasNext()) {
                final Element dataSet = (Element) dsIterator.next();
                final Element dsSizeElement = dataSet.getChild("DS_Size", namespace);
                final String sizeString = dsSizeElement.getText();
                final int size = Integer.parseInt(sizeString);
                if (size == 0) {
                    continue;
                }

                final String dsNameString = dataSet.getChildText("DS_Name", namespace);
                if ("Swath_Snapshot_List".equalsIgnoreCase(dsNameString)) {
                    offset += size;
                    continue;
                }

                final String dsrSizeString = dataSet.getChildText("DSR_Size", namespace);
                long dsrSize = Long.parseLong(dsrSizeString);
                long dsSize = dsrSize * gridPointCount;
                dsSizeElement.setText(numberFormat.format(dsSize));

                final Element dsOffsetElement = dataSet.getChild("DS_Offset", namespace);
                dsOffsetElement.setText(numberFormat.format(offset));
                offset += dsSize;

                final Element numDsrElement = dataSet.getChild("Num_DSR", namespace);
                numDsrElement.setText(numberFormat.format(gridPointCount));
            }
        }
    }

    private void patchGeolocation(Namespace namespace, DecimalFormat numberFormat, Element productLocation) {
        // @todo 3 tb/tb check for orbit orientation - right now we always assume north-south direction
        final Element startLat = productLocation.getChild("Start_Lat", namespace);
        startLat.setText(numberFormat.format(area.getMaxY()));

        Element startLon = productLocation.getChild("Start_Lon", namespace);
        if (startLon == null) {
            startLon = productLocation.getChild("Start_Long", namespace);
        }
        startLon.setText(numberFormat.format(area.getMinX()));

        final Element stopLat = productLocation.getChild("Stop_Lat", namespace);
        stopLat.setText(numberFormat.format(area.getMinY()));

        Element stopLon = productLocation.getChild("Stop_Lon", namespace);
        if (stopLon == null) {
            stopLon = productLocation.getChild("Stop_Long", namespace);
        }
        stopLon.setText(numberFormat.format(area.getMaxX()));

        // @todo 3 tb/tb pure averaging is not really correct here
        final Element midLat = productLocation.getChild("Mid_Lat", namespace);
        midLat.setText(numberFormat.format(area.getMinY() + 0.5 * (area.getMaxY() - area.getMinY())));

        Element midLon = productLocation.getChild("Mid_Lon", namespace);
        if (midLon == null) {
            midLon = productLocation.getChild("Mid_Long", namespace);
        }
        midLon.setText(numberFormat.format(area.getMinX() + 0.5 * (area.getMaxX() - area.getMinX())));
    }

    private DecimalFormat createDecimalFormat() {
        final DecimalFormat numberFormat = new DecimalFormat("+000.000000;-000.000000");
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        numberFormat.setDecimalFormatSymbols(symbols);
        return numberFormat;
    }

    private void patchFixedHeader(Element element, Namespace namespace) {
        final Element fixedHeader = element.getChild("Fixed_Header", namespace);

        if (fileName != null) {
            final Element fileNameField = fixedHeader.getChild("File_Name", namespace);
            fileNameField.setText(fileName);
        }

        final Element validityPeriod = fixedHeader.getChild("Validity_Period", namespace);
        if (sensingStart != null) {
            final Element validityStart = validityPeriod.getChild("Validity_Start", namespace);
            validityStart.setText(DateTimeUtils.toFixedHeaderFormat(sensingStart));
        }

        if (sensingStop != null) {
            final Element validityStop = validityPeriod.getChild("Validity_Stop", namespace);
            validityStop.setText(DateTimeUtils.toFixedHeaderFormat(sensingStop));
        }
    }
}
