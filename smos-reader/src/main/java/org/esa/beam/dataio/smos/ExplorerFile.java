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

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.framework.datamodel.Product;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

public abstract class ExplorerFile {

    public static final String TAG_SPECIFIC_PRODUCT_HEADER = "Specific_Product_Header";

    private final File hdrFile;
    private final File dblFile;
    private final DataFormat dataFormat;
    private final DataContext dataContext;
    private final CompoundData dataBlock;
    private volatile Area area = null;

    protected ExplorerFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        this.hdrFile = hdrFile;
        this.dblFile = dblFile;
        this.dataFormat = Dddb.getInstance().getDataFormat(hdrFile);
        dataContext = dataFormat.createContext(dblFile, "r");
        dataBlock = dataContext.getData();
    }

    public final File getHdrFile() {
        return hdrFile;
    }

    public final File getDblFile() {
        return dblFile;
    }

    public final DataFormat getDataFormat() {
        return dataFormat;
    }

    public final DataContext getDataContext() {
        return dataContext;
    }

    public final CompoundData getDataBlock() {
        return dataBlock;
    }

    public final Area getArea() {
        Area result = area;

        if (result == null) {
            synchronized (this) {
                result = area;
                if (result == null) {
                    try {
                        area = result = computeArea();
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot compute area.", e);
                    }
                }
            }
        }

        return result;
    }

    public final boolean hasArea() {
        synchronized (this) {
            return area != null;
        }
    }

    public void close() {
        dataContext.dispose();
    }

    public final Document getDocument() throws IOException {
        final Document document;
        try {
            document = new SAXBuilder().build(hdrFile);
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format("File ''{0}'': Invalid document", hdrFile.getPath()), e);
        }
        return document;
    }

    public Element getElement(Element parent, final String name) throws IOException {
        final Iterator descendants = parent.getDescendants(new Filter() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof Element) {
                    final Element e = (Element) o;
                    if (name.equals(e.getName())) {
                        return true;
                    }
                }

                return false;
            }
        });
        if (descendants.hasNext()) {
            return (Element) descendants.next();
        } else {
            throw new IOException(
                    MessageFormat.format("File ''{0}'': Missing element ''{1}''.", getHdrFile().getPath(), name));
        }
    }

    protected abstract Area computeArea() throws IOException;

    protected abstract Product createProduct() throws IOException;
}
