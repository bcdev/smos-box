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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ExplorerFile {

    public static final String TAG_SPECIFIC_PRODUCT_HEADER = "Specific_Product_Header";
    
    private final File hdrFile;
    private final File dblFile;
    private final DataFormat dataFormat;
    private final DataContext dataContext;
    private final CompoundData dataBlock;
    private volatile Future<Area> areaFuture;

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
        try {
            return getAreaFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public final boolean hasArea() {
        return getAreaFuture().isDone();
    }

    public void close() {
        dataContext.dispose();
    }

    public final Document getDocument() throws IOException {
        final Document document;
        try {
            document = new SAXBuilder().build(hdrFile);
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", hdrFile.getPath()), e);
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
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing element ''{1}''.", getHdrFile().getPath(), name));
        }
    }

    protected abstract Area computeArea() throws IOException;

    protected abstract Product createProduct() throws IOException;

    private Future<Area> getAreaFuture() {
        if (areaFuture == null) {
            synchronized (this) {
                if (areaFuture == null) {
                    areaFuture = Executors.newSingleThreadExecutor().submit(new Callable<Area>() {
                        @Override
                        public Area call() throws IOException {
                            return computeArea();
                        }
                    });
                }
            }
        }

        return areaFuture;
    }

    public static Date cfiDateToUtc(int days, long seconds, long microseconds) {
        final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, days);
        calendar.add(Calendar.SECOND, (int) seconds);
        calendar.add(Calendar.MILLISECOND, (int) (microseconds * 0.001));

        return calendar.getTime();
    }

    public static Date mjdFloatDateToUtc(float mjd) {
        final int days = (int) mjd;
        final double secondsFraction = (mjd - days) * 86400.0;
        final int seconds = (int) secondsFraction;
        final int microseconds = (int) ((secondsFraction - seconds) * 1.0e6);

        return cfiDateToUtc(days, seconds, microseconds);
    }
}
