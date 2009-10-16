package org.esa.beam.smos.visat.export;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

// @todo 1 tb/tb patch File_Name field
class EEHdrFilePatcher {

    private Date sensingStart = null;
    private Date sensingStop = null;

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

    final Format getFormat() {
        return Format.getRawFormat().setOmitEncoding(true).setLineSeparator("\n");
    }

    Document patchDocument(Document document) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC='yyyy-MM-dd'T'hh:MM:ss");
        final SimpleDateFormat dateFormatMicroSec = new SimpleDateFormat("'UTC='yyyy-MM-dd'T'hh:MM:ss.SSSSSS");
        final Element element = document.getRootElement();
        final Namespace namespace = element.getNamespace();
        final Element fixedHeader = element.getChild("Fixed_Header", namespace);
        final Element validityPeriod = fixedHeader.getChild("Validity_Period", namespace);

        if (sensingStart != null) {
            final Element validityStart = validityPeriod.getChild("Validity_Start", namespace);
            validityStart.setText(dateFormat.format(sensingStart));
        }

        if (sensingStop != null) {
            final Element validityStop = validityPeriod.getChild("Validity_Stop", namespace);
            validityStop.setText(dateFormat.format(sensingStop));
        }

        final Element variableHeader = element.getChild("Variable_Header", namespace);
        // @todo 1 tb/tb only for L1C !!!!!!!!!!!!!!!!!!!!!!!!
        final Element specificHeader = variableHeader.getChild("Specific_Product_Header", namespace);
        final Element mainInfo = specificHeader.getChild("Main_Info", namespace);
        final Element timeInfo = mainInfo.getChild("Time_Info", namespace);

        if (sensingStart != null) {
            final Element validityStart = timeInfo.getChild("Precise_Validity_Start", namespace);
            validityStart.setText(dateFormatMicroSec.format(sensingStart));
        }
        if (sensingStart != null) {
            final Element validityStop = timeInfo.getChild("Precise_Validity_Stop", namespace);
            validityStop.setText(dateFormatMicroSec.format(sensingStop));
        }

        return document;
    }
}
