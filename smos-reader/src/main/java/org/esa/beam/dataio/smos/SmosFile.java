package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.CompoundData;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.geom.Area;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class SmosFile {

    private final File file;
    private final DataFormat format;
    private final DataContext dataContext;
    private final CompoundData dataBlock;

    protected SmosFile(File file, DataFormat format) throws FileNotFoundException {
        this.file = file;
        this.format = format;
        dataContext = format.createContext(file, "r");
        dataBlock = dataContext.getData();
    }

    public final File getFile() {
        return file;
    }

    public final DataFormat getFormat() {
        return format;
    }

    public final DataContext getDataContext() {
        return dataContext;
    }

    public final CompoundData getDataBlock() {
        return dataBlock;
    }

    public abstract Area getRegion();

    public void close() {
        dataContext.dispose();
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
}
