/*
 * $Id: $
 *
 * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Represents a SMOS product file providing data on the DGG.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class SmosFile {

    private File file;
    private DataFormat format;
    private DataContext dataContext;

    private CompoundData dataBlock;
    private SequenceData gridPointList;
    private CompoundType gridPointType;
    private int gridPointIdIndex;
    private int[] gridPointIndexes;

    private Area region;
    private int minSeqnum;
    private int maxSeqnum;

    public SmosFile(File file, DataFormat format) throws IOException {
        this.file = file;
        this.format = format;

        dataContext = format.createContext(file, "r");
        dataBlock = dataContext.getData();

        gridPointList = dataBlock.getSequence(SmosFormats.GRID_POINT_LIST_NAME);
        if (gridPointList == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing grid point list.", file.getPath()));
        }

        gridPointType = (CompoundType) gridPointList.getType().getElementType();
        gridPointIdIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_ID_NAME);
        gridPointIndexes = createGridPointIndexes();
        region = computeRegion();
    }

    public final int getGridPointCount() {
        return gridPointList.getElementCount();
    }

    public final int getGridPointId(int i) throws IOException {
        return gridPointList.getCompound(i).getInt(gridPointIdIndex);
    }

    public final int getGridPointSeqnum(int i) throws IOException {
        return SmosDgg.smosGridPointIdToDggSeqnum(getGridPointId(i));
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

    public SequenceData getGridPointList() {
        return gridPointList;
    }

    public int getGridPointIndex(int seqnum) {
        if (seqnum < minSeqnum || seqnum > maxSeqnum) {
            return -1;
        }

        return gridPointIndexes[seqnum - minSeqnum];
    }

    public CompoundType getGridPointType() {
        return gridPointType;
    }

    public CompoundData getGridPointData(int gridPointIndex) throws IOException {
        return gridPointList.getCompound(gridPointIndex);
    }

    public Area getRegion() {
        return region;
    }

    public void close() {
        dataContext.dispose();
    }

    private Area computeRegion() throws IOException {
        final int latIndex = getGridPointType().getMemberIndex(SmosFormats.GRID_POINT_LAT_NAME);
        final int lonIndex = getGridPointType().getMemberIndex(SmosFormats.GRID_POINT_LON_NAME);
        final SequenceData gridPointList = getGridPointList();

        final Rectangle2D[] tileRects = new Rectangle2D[512];
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 16; ++j) {
                tileRects[i * 16 + j] = createTileRectangle(i, j);
            }
        }

        final Area region = new Area();

        for (int i = 0; i < gridPointList.getElementCount(); i++) {
            CompoundData compound = gridPointList.getCompound(i);
            double lon = compound.getFloat(lonIndex);
            double lat = compound.getFloat(latIndex);

            // normalisation to [-180, 180] necessary for some L1c test products
            if (lon > 180.0) {
                lon = lon - 360.0;
            }
            final double hw = 0.02;
            final double hh = 0.02;

            final double x = lon - hw;
            final double y = lat - hh;
            final double w = 0.04;
            final double h = 0.04;

            if (!region.contains(x, y, w, h)) {
                for (Rectangle2D tileRect : tileRects) {
                    if (tileRect.intersects(x, y, w, h) && !region.contains(tileRect)) {
                        region.add(new Area(tileRect));
                        if (region.contains(x, y, w, h)) {
                            break;
                        }
                    }
                }
            }
        }

        return region;
    }

    private int[] createGridPointIndexes() throws IOException {
        minSeqnum = getGridPointSeqnum(0);
        maxSeqnum = minSeqnum;

        final int gridPointCount = getGridPointCount();
        for (int i = 1; i < gridPointCount; i++) {
            final int seqnum = getGridPointSeqnum(i);

            if (seqnum < minSeqnum) {
                minSeqnum = seqnum;
            } else {
                if (seqnum > maxSeqnum) {
                    maxSeqnum = seqnum;
                }
            }
        }

        final int[] gridPointIndexes = new int[maxSeqnum - minSeqnum + 1];
        Arrays.fill(gridPointIndexes, -1);

        for (int i = 0; i < gridPointCount; i++) {
            gridPointIndexes[getGridPointSeqnum(i) - minSeqnum] = i;
        }

        return gridPointIndexes;
    }

    private static Rectangle2D createTileRectangle(int i, int j) {
        final double w = 11.25;
        final double h = 11.25;
        final double x = w * i - 180.0 - w;
        final double y = 90.0 - h * (j + 1);

        return new Rectangle2D.Double(x, y, w, w);
    }

    // todo: rq/rq - move this method to somewhere else (2009-10-21)
    public static Date getCfiDateInUtc(int days, long seconds, long microseconds) {
        final Calendar instance = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        instance.set(Calendar.YEAR, 2000);
        instance.set(Calendar.MONTH, 0);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        instance.add(Calendar.DATE, days);
        instance.add(Calendar.SECOND, (int) seconds);
        instance.add(Calendar.MILLISECOND, (int) (microseconds * 0.001));

        return instance.getTime();
    }
}
