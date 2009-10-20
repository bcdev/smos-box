package org.esa.beam.smos.visat.export;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

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
        final SimpleDateFormat dateFormatMicroSec = new SimpleDateFormat("'UTC='yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        final Element variableHeader = element.getChild("Variable_Header", namespace);
        final Element specificHeader = variableHeader.getChild("Specific_Product_Header", namespace);
        final Element mainInfo = specificHeader.getChild("Main_Info", namespace);
        final Element timeInfo = mainInfo.getChild("Time_Info", namespace);

        if (sensingStart != null) {
            final Element validityStart = timeInfo.getChild("Precise_Validity_Start", namespace);
            validityStart.setText(dateFormatMicroSec.format(sensingStart));
        }
        if (sensingStop != null) {
            final Element validityStop = timeInfo.getChild("Precise_Validity_Stop", namespace);
            validityStop.setText(dateFormatMicroSec.format(sensingStop));
        }

        if (area != null) {
            final DecimalFormat numberFormat = createDecimalFormat();

            // @todo 3 tb/tb check for orbit orientation - right now we always assume north-south direction
            final Element productLocation = specificHeader.getChild("Product_Location", namespace);
            final Element startLat = productLocation.getChild("Start_Lat", namespace);
            startLat.setText(numberFormat.format(area.getMaxY()));

            final Element startLon = productLocation.getChild("Start_Lon", namespace);
            startLon.setText(numberFormat.format(area.getMinX()));

            final Element stopLat = productLocation.getChild("Stop_Lat", namespace);
            stopLat.setText(numberFormat.format(area.getMinY()));

            final Element stopLon = productLocation.getChild("Stop_Lon", namespace);
            stopLon.setText(numberFormat.format(area.getMaxX()));

            // @todo 3 tb/tb pure averaging is not really correct here
            final Element midLat = productLocation.getChild("Mid_Lat", namespace);
            midLat.setText(numberFormat.format(area.getMinY() + 0.5 * (area.getMaxY() - area.getMinY())));

            final Element midLon = productLocation.getChild("Mid_Lon", namespace);
            midLon.setText(numberFormat.format(area.getMinX() + 0.5 * (area.getMaxX() - area.getMinX())));
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
            }
        }
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

        final SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC='yyyy-MM-dd'T'HH:mm:ss");
        final Element validityPeriod = fixedHeader.getChild("Validity_Period", namespace);
        if (sensingStart != null) {
            final Element validityStart = validityPeriod.getChild("Validity_Start", namespace);
            validityStart.setText(dateFormat.format(sensingStart));
        }

        if (sensingStop != null) {
            final Element validityStop = validityPeriod.getChild("Validity_Stop", namespace);
            validityStop.setText(dateFormat.format(sensingStop));
        }
    }
}
