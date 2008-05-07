package org.esa.beam.dataio.smos;

import junit.framework.TestCase;
import org.jdom.JDOMException;

import javax.imageio.stream.FileImageInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class SmosL1cReaderF1cTest extends TestCase {
    private static final double RE = 6370000.0;

    private SmosL1cReader reader;

    @Override
    protected void setUp() throws Exception {
        final String propertyName = getClass().getName() + ".productDir";
        final String sclf1cDirPath = System.getProperty(propertyName);
        if (sclf1cDirPath == null || sclf1cDirPath.isEmpty()) {
            throw new Exception("System property '" +propertyName+ "' must be set in order to perform reader tests!");
        }        
        final File productDir = new File(sclf1cDirPath);
        final File headerFile = new File(productDir, productDir.getName() + ".HDR");
        final File dataFile = new File(productDir, productDir.getName() + ".DBL");
        reader = new SmosL1cReader(new FileInputStream(headerFile),
                                   new FileImageInputStream(dataFile));
    }

    @Override
    protected void tearDown() throws Exception {
        reader.close();
    }

    public void testProperties() throws JDOMException, IOException {
        assertEquals(4L, reader.getSnapshotDsOffset());
        assertEquals(161, reader.getSnapshotDsrSize());
        assertEquals(18, reader.getGridPointDsrSize());
        assertEquals(28, reader.getBtDataDsrSize());
        assertInRange(1, 5000, reader.getSnapshotCount());
        assertInRange(1, 2621442, reader.getGridPointCount());
        final long expectedOffset = 4L + reader.getSnapshotCount() * reader.getSnapshotDsrSize() + 4L;
        assertEquals(expectedOffset, reader.getGridPointDsOffset());
    }

    public void testReadGridPoint() throws JDOMException, IOException {
        final SmosL1cGridPointReader gridPointReader = reader.createSmosL1cGridPointReader();
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
    }

    public void testReadGridPointSubset() throws JDOMException, IOException {
        final int[] fieldIndexes = {
                0, // ushort  Flags
                1, // float   BT_Value_Real
                2, // float   BT_Value_Imag
                4, // ushort  Incidence_Angle
                8, // uint    Snapshot_ID_of_Pixel
        };
        final SmosL1cGridPointReader gridPointReader = reader.createSmosL1cGridPointReader(fieldIndexes);
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
        testGridPointData(gridPointReader.readNext());
    }

    private void testGridPointData(SmosL1cGridPointData gridPointData) {
        assertInRange(1, 9262145, gridPointData.gridPointId);
        assertInRange(0, 255, gridPointData.mask);
        assertInRange(-90.0F, +90.0F, gridPointData.latitude);  // deg
        assertInRange(-180.0F, +180.0F, gridPointData.longitude);   // deg
        assertInRange(-1000.0F, +10000.0F, gridPointData.altitude); // m
        assertInRange(0, 255, gridPointData.btDataCount);

        final Object[] data = gridPointData.bandData;
        assertNotNull(data);
//        assertEquals(data.length);
//        testBtData(gridPointData.btData[0]);
//        testBtData(gridPointData.btData[gridPointData.btDataCount - 1]);
    }

    private void testBtData(SmosBtData btData) {
        assertInRange(0.0F, 90.0F, btData.incidenceAngle);
        assertInRange(0.0F, 360.0F, btData.azimuthAngle);
        assertInRange(0.0F, 360.0F, btData.geometricRotationAngle);
        assertInRange(0, 65535, btData.flags);
        assertInRange(0, 50000, btData.snapshotId);
    }

    public void testReadSnapshotInfo() throws JDOMException, IOException {
        testSnapshotInfo(reader.readSnapshotInfo(0));
        testSnapshotInfo(reader.readSnapshotInfo(1));
        testSnapshotInfo(reader.readSnapshotInfo(2));
        testSnapshotInfo(reader.readSnapshotInfo(20));
        testSnapshotInfo(reader.readSnapshotInfo(21));
        testSnapshotInfo(reader.readSnapshotInfo(22));
    }

    // todo - adjust ranges
    private void testSnapshotInfo(SmosSnapshotInfo snapshotInfo) {
        assertInRange(0, 10000, snapshotInfo.snapshotTime[0]);
        assertInRange(0, 50000, snapshotInfo.snapshotId);
        assertInRange(0, 6, snapshotInfo.vectorSource);

        assertInRange(750000.0, 760000.0, h(snapshotInfo)); // m
        assertInRange(7500.0, 8000.0, v(snapshotInfo));   // m/s

        assertInRange(-1.0, +1.0, snapshotInfo.q1); // rad/PI
        assertInRange(-1.0, +1.0, snapshotInfo.q2); // rad/PI
        assertInRange(-1.0, +1.0, snapshotInfo.q3); // rad/PI
        assertInRange(-1.0, +1.0, snapshotInfo.q4); // rad/PI

        assertInRange(0.0, +10.0, snapshotInfo.tec); // tec U

        assertInRange(0.0, 100000.0, snapshotInfo.geomagF);  // nT
        assertInRange(-90.0, +90.0, snapshotInfo.geomagD); // deg
        assertInRange(-90.0, +90.0, snapshotInfo.geomagI); // deg

        assertInRange(-90.0F, +90.0F, snapshotInfo.sunRa);  // deg
        assertInRange(-90.0F, +90.0F, snapshotInfo.sunDec); // deg
        assertInRange(0.0F, 5000.0F, snapshotInfo.sunBt); // K
        assertInRange(-300.0F, +300.0F, snapshotInfo.accuracy); // K
        assertInRange(-300.0F, +300.0F, snapshotInfo.radiometricAccuracy[0]); // K
        assertInRange(-300.0F, +300.0F, snapshotInfo.radiometricAccuracy[1]); // K
    }

    private double h(SmosSnapshotInfo snapshotInfo) {
        return Math.sqrt(
                snapshotInfo.xPosition * snapshotInfo.xPosition +
                snapshotInfo.yPosition * snapshotInfo.yPosition +
                snapshotInfo.zPosition * snapshotInfo.zPosition) - RE;
    }

    private double v(SmosSnapshotInfo snapshotInfo) {
        return Math.sqrt(
                snapshotInfo.xVelocity * snapshotInfo.xVelocity +
                snapshotInfo.yVelocity * snapshotInfo.yVelocity +
                snapshotInfo.zVelocity * snapshotInfo.zVelocity);
    }

    public static void assertInRange(int expectedMin, int expectedMax, int actual) {
        final String msg = "Value must be in range (" + expectedMin + "," + expectedMax + "), but was " + actual;
        assertTrue(msg, actual >= expectedMin && actual <= expectedMax);
    }

    public static void assertInRange(float expectedMin, float expectedMax, float actual) {
        final String msg = "Value must be in range (" + expectedMin + "," + expectedMax + "), but was " + actual;
        assertTrue(msg, actual >= expectedMin && actual <= expectedMax);
    }

    public static void assertInRange(double expectedMin, double expectedMax, double actual) {
        final String msg = "Value must be in range (" + expectedMin + "," + expectedMax + "), but was " + actual;
        assertTrue(msg, actual >= expectedMin && actual <= expectedMax);
    }
}
