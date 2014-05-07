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
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.SequenceType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.binio.util.DataPrinter;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.smos.EEFilePair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LaiFileTest {

    private static final File USER_HOME = new File(System.getProperty("user.home"));
    private static final File DATA_DIR = new File(USER_HOME,
                                                  "SM_OPER_AUX_DFFLAI_20091219T000000_20100118T000000_306_001_3");
    private static final File DBL_FILE = new File(DATA_DIR,
                                                  "SM_OPER_AUX_DFFLAI_20091219T000000_20100118T000000_306_001_3.DBL");
    private static final File HDR_FILE = new File(DATA_DIR,
                                                  "SM_OPER_AUX_DFFLAI_20091219T000000_20100118T000000_306_001_3.HDR");

    private static final String DFFG_LAI_NAME = "DFFG_LAI";
    private static final int ZONE_COUNT = 74;

    @Test
    public void format() throws IOException {
        if (HDR_FILE.exists()) {
            final DataFormat dataFormat = Dddb.getInstance().getDataFormat(HDR_FILE);
            final CompoundType dataBlockType = dataFormat.getType();

            assertEquals(1, dataBlockType.getMemberCount());
            assertEquals(0, dataBlockType.getMemberIndex(DFFG_LAI_NAME));

            final Type zoneSequenceDataType = dataBlockType.getMember(0).getType();
            assertTrue(zoneSequenceDataType.isSequenceType());

            final SequenceType sequenceType = (SequenceType) zoneSequenceDataType;
            assertEquals(ZONE_COUNT, sequenceType.getElementCount());
        }
    }

    @Test
    public void testZoneDataSizes() throws IOException {
        if (HDR_FILE.exists() && DBL_FILE.exists()) {
            final DataFormat dataFormat = Dddb.getInstance().getDataFormat(HDR_FILE);
            final LaiFile laiFile = new LaiFile(new EEFilePair(HDR_FILE, DBL_FILE), dataFormat.createContext(DBL_FILE, "r"));

            final SequenceData sequenceData = laiFile.getDataBlock().getSequence(DFFG_LAI_NAME);
            assertEquals(ZONE_COUNT, sequenceData.getElementCount());

            CompoundData compoundData;

            compoundData = sequenceData.getCompound(0);
            assertEquals(0, compoundData.getPosition());
            compoundData.resolveSize();
            assertEquals(3791314, compoundData.getSize());

            compoundData = sequenceData.getCompound(1);
            assertEquals(3791314, compoundData.getPosition());
            compoundData.resolveSize();
            assertEquals(3791314, compoundData.getSize());

            compoundData = sequenceData.getCompound(2);
            compoundData.resolveSize();
            assertEquals(3052488, compoundData.getSize());

            compoundData = sequenceData.getCompound(3);
            compoundData.resolveSize();
            assertEquals(3052488, compoundData.getSize());

            laiFile.getCellIndex(0.0, 0.0);
        }
    }

    // for dumping the contents of a LAI file
    public static void main(String[] args) throws IOException {
        final DataFormat dataFormat = Dddb.getInstance().getDataFormat(HDR_FILE);
        final ExplorerFile explorerFile = new LaiFile(new EEFilePair(HDR_FILE, DBL_FILE), dataFormat.createContext(DBL_FILE, "r"));
        final SequenceData sequenceData = explorerFile.getDataBlock().getSequence(DFFG_LAI_NAME);

        try {
            for (int i = sequenceData.getElementCount(); i-- > 0;) {
                final int zoneId = sequenceData.getCompound(i).getInt("Zone_ID");
                final String fileName = DBL_FILE.getName().replace("DBL", zoneId + ".TXT");
                final File file = new File(DATA_DIR, fileName);
                final PrintStream printStream = new PrintStream(file);
                final DataPrinter dataPrinter = new DataPrinter(printStream, false);
                try {
                    dataPrinter.print(sequenceData.getCompound(i));
                } finally {
                    printStream.close();
                }
            }
        } finally {
            explorerFile.close();
        }
    }
}
