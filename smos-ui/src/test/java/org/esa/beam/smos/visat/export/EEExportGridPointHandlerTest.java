package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.util.ByteArrayIOHandler;
import com.bc.ceres.binio.util.DataPrinter;
import static junit.framework.Assert.assertEquals;
import org.esa.beam.dataio.smos.DDDB;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.dataio.smos.SmosFile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class EEExportGridPointHandlerTest {

    private static final String SCENARIO_27_DBL_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.DBL";
    private static final String SCENARIO_27_HDR_NAME = "scenario27/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0/SM_TEST_MIR_SCSD1C_20070223T142110_20070223T142111_320_001_0.HDR";

    @Test
    public void handleGridPointsForScenario27() throws URISyntaxException, IOException {
        final File dblFile = getResourceAsFile(SCENARIO_27_DBL_NAME);
        final File hdrFile = getResourceAsFile(SCENARIO_27_HDR_NAME);
        final DataFormat dblFormat = DDDB.getInstance().getDataFormat(hdrFile);
        final SmosFile sourceFile = new SmosFile(hdrFile, dblFile, dblFormat);

        final SequenceData sourceGridPointList = sourceFile.getGridPointList();
        assertEquals(5533, sourceGridPointList.getElementCount());
        assertEquals("Grid_Point_Data_Type[5533]", sourceGridPointList.getType().getName());

        final DataContext targetContext = dblFormat.createContext(new ByteArrayIOHandler());
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

        final SequenceData targetSnapshotList = targetData.getSequence(SmosConstants.L1C_SNAPSHOT_LIST_NAME);
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

        final SequenceData btDataSequence = gridPointData.getSequence(SmosConstants.L1C_BT_DATA_LIST_NAME);
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
