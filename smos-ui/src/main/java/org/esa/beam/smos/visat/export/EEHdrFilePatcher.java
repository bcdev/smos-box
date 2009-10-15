package org.esa.beam.smos.visat.export;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
        final List content = document.getContent();
        final Iterator iterator = content.iterator();
        while (iterator.hasNext()) {
            final Element eeHeader = (Element) iterator.next();
            final List children = eeHeader.getChildren();
            //eeHeader.get
            final Iterator iterator1 = children.iterator();
            while (iterator1.hasNext()) {
                System.out.println(iterator1.next());
            }
        }
        return document;
    }
}
