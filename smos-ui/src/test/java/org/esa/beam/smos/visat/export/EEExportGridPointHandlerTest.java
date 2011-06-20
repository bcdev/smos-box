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

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.util.ByteArrayIOHandler;
import com.bc.ceres.binio.util.DataPrinter;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.GridPointList;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EEExportGridPointHandlerTest {

    private static final String SCENARIO_27_DBL_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.DBL";

    @Test
    public void handleGridPointsForScenario27() throws URISyntaxException, IOException {
        final ExplorerFile explorerFile = SmosProductReader.createExplorerFile(getResourceAsFile(SCENARIO_27_DBL_NAME));
        assertTrue(explorerFile instanceof SmosFile);
        final SmosFile sourceFile = (SmosFile) explorerFile;

        final GridPointList sourceGridPointList = sourceFile.getGridPointList();
        assertEquals(5533, sourceGridPointList.getElementCount());

        final DataContext targetContext = sourceFile.getDataFormat().createContext(new ByteArrayIOHandler());
        final GridPointHandler handler = new EEExportGridPointHandler(targetContext, new GridPointFilter() {
            @Override
            public boolean accept(int id, CompoundData gridPointData) throws IOException {
                return id == 0 || id == 7;
            }
        });

        for (int i = 0; i < sourceGridPointList.getElementCount(); i++) {
            final CompoundData gridPointData = sourceGridPointList.getCompound(i);
            handler.handleGridPoint(i, gridPointData);
        }

        final CompoundData targetData = targetContext.createData();
        targetContext.dispose();

        final DataPrinter dataPrinter = new DataPrinter();
        dataPrinter.print(targetData);

        final SequenceData targetSnapshotList = targetData.getSequence(SmosConstants.SNAPSHOT_LIST_NAME);
        assertEquals(2, targetSnapshotList.getElementCount());
        assertEquals(60046, targetSnapshotList.getCompound(0).getInt("Snapshot_ID"));
        assertEquals(60047, targetSnapshotList.getCompound(1).getInt("Snapshot_ID"));

        final SequenceData targetGridPointList = targetData.getSequence(SmosConstants.GRID_POINT_LIST_NAME);
        assertEquals(2, targetGridPointList.getElementCount());

        assertGridPointData(targetGridPointList.getCompound(0), 233545, 70.169426F, 173.99301F);
        assertGridPointData(targetGridPointList.getCompound(1), 235084, 62.994823F, 179.01186F);
    }

    private static void assertGridPointData(CompoundData gridPointData, int id, float... bt) throws IOException {
        assertEquals(id, gridPointData.getInt("Grid_Point_ID"));

        final SequenceData btDataSequence = gridPointData.getSequence(SmosConstants.BT_DATA_LIST_NAME);
        assertEquals(bt.length, btDataSequence.getElementCount());

        for (int i = 0; i < bt.length; i++) {
            assertEquals(bt[i], btDataSequence.getCompound(i).getFloat("BT_Value"), 0.0f);
        }
    }

    private static File getResourceAsFile(String name) throws URISyntaxException {
        final URL url = EEExportGridPointHandlerTest.class.getResource(name);
        final URI uri = url.toURI();

        return new File(uri);
    }

}
