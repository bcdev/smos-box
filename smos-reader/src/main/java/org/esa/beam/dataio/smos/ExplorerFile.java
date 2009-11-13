package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ExplorerFile {

    private final File hdrFile;
    private final File dblFile;
    private final DataFormat dataFormat;
    private final DataContext dataContext;
    private final CompoundData dataBlock;
    private volatile Future<Area> envelopeFuture;

    protected ExplorerFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        this.hdrFile = hdrFile;
        this.dblFile = dblFile;
        this.dataFormat = DDDB.getInstance().getDataFormat(hdrFile);
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

    public final Area getEnvelope() {
        try {
            return getEnvelopeFuture().get();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    public final boolean hasEnvelope() {
        return getEnvelopeFuture().isDone();
    }

    public void close() {
        dataContext.dispose();
    }

    protected abstract Area computeEnvelope() throws IOException;

    protected abstract Product createProduct() throws IOException;

    private Future<Area> getEnvelopeFuture() {
        if (envelopeFuture == null) {
            synchronized (this) {
                if (envelopeFuture == null) {
                    envelopeFuture = Executors.newSingleThreadExecutor().submit(new Callable<Area>() {
                        @Override
                        public Area call() throws IOException {
                            return computeEnvelope();
                        }
                    });
                }
            }
        }

        return envelopeFuture;
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
        int days = (int) mjd;
        double doubleSecs = (mjd - days) * 86400;
        int secs = (int)doubleSecs;
        int microsecs = (int) ((doubleSecs - secs) * 1e6);
        return cfiDateToUtc(days, secs, microsecs);
    }
}
