package org.esa.beam.dataio.smos;

import junit.framework.TestCase;
import org.jdom.JDOMException;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class SmosL1cReaderF1cTest extends TestCase {
    private static final double RE = 6370000.0;

    private SmosL1cReader reader;


    public void testChunkedReading() throws IOException {
        final long dsOffset = reader.getGridPointDsOffset();
        final int dsrSize = reader.getGridPointMdsrSize();
        final ImageInputStream stream = reader.getDataInputStream();
        stream.seek(dsOffset);

        final byte[] chunk1 = new byte[18];
        stream.read(chunk1);
        final int chunk2Size = chunk1[17] & 0xFF;
        assertEquals(28, dsrSize);
        assertEquals(129, chunk2Size);
        final byte[] chunk2 = new byte[chunk2Size * dsrSize];
        stream.read(chunk2);

        final ByteArrayDecoder decoder = new ByteArrayDecoder(ByteOrder.LITTLE_ENDIAN);

        final SmosL1cGridPointRecord gridPointRecord = decodeGridPointRecord(decoder, chunk1);
        testGridPointRecord(gridPointRecord);
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, 0));
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, 1));
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, 2));
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, gridPointRecord.btDataCount-3));
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, gridPointRecord.btDataCount-2));
        testSmosD1cMdsr(decodeMdsr(decoder, chunk2, gridPointRecord.btDataCount-1));
    }

    private SmosL1cGridPointRecord decodeGridPointRecord(ByteArrayDecoder decoder, byte[] chunk1) {
        final SmosL1cGridPointRecord gridPointRecord = new SmosL1cGridPointRecord();
        gridPointRecord.gridPointId = decoder.toInt(chunk1, 0);
        gridPointRecord.latitude = decoder.toFloat(chunk1, 4);
        gridPointRecord.longitude = decoder.toFloat(chunk1, 8);
        gridPointRecord.altitude = decoder.toFloat(chunk1, 12);
        gridPointRecord.mask = decoder.toByte(chunk1, 16) & 0xFF;
        gridPointRecord.btDataCount = decoder.toByte(chunk1, 17) & 0xFF;
        return gridPointRecord;
    }

    private SmosD1cMdsr decodeMdsr(ByteArrayDecoder decoder, byte[] chunk2, int i) {
        final int offs = 0;
        final int size = 28;
        final int boffs = offs + i * size;
        final SmosD1cMdsr mdsr = new SmosD1cMdsr();
        mdsr.flags = decoder.toShort(chunk2, boffs + 0) & 0xFFFF;
        mdsr.btValueReal = decoder.toFloat(chunk2, boffs + 2);
        mdsr.btValueImag = decoder.toFloat(chunk2, boffs + 6);
        mdsr.pixelRadiometricAccuracy = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 10), 50F);
        mdsr.incidenceAngle = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 12), 90F);
        mdsr.azimuthAngle = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 14), 360F);
        mdsr.faradayRotationAngle = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 16), 360F);
        mdsr.geometricRotationAngle = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 18), 360F);
        mdsr.snapshotId = decoder.toInt(chunk2, i * size + 20);
        mdsr.footprintAxis1 = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 24), 100F);
        mdsr.footprintAxis2 = SmosD1cMdsr.scaleUShort(decoder.toShort(chunk2, boffs + 26), 100F);
        return mdsr;
    }


    public void testProperties() throws JDOMException, IOException {
        assertEquals(4L, reader.getSnapshotDsOffset());
        assertEquals(161, reader.getSnapshotDsrSize());
        assertEquals(18, reader.getGridPointDsrSize());
        assertEquals(28, reader.getGridPointMdsrSize());
        assertInRange(1, 5000, reader.getSnapshotCount());
        assertInRange(1, 2621442, reader.getGridPointCount());
        final long expectedOffset = 4L + reader.getSnapshotCount() * reader.getSnapshotDsrSize() + 4L;
        assertEquals(expectedOffset, reader.getGridPointDsOffset());
    }

    public void testReadGridPointRecord() throws IOException {
        reader.getDataInputStream().seek(reader.getGridPointDsOffset());
        final SmosL1cGridPointRecord record = reader.readGridPointRecord();
        testGridPointRecord(record);
        final SmosD1cMdsr[] records = record.btDataRecords;
        assertNotNull(records);
        for (SmosD1cMdsr mdsr : records) {
            testSmosD1cMdsr(mdsr);
        }
    }

    private void testGridPointRecord(SmosL1cGridPointRecord record) {
        assertInRange(1, 9262145, record.gridPointId); // #
        assertInRange(0, 255, record.mask); // #
        assertInRange(-90.0F, +90.0F, record.latitude);  // deg
        assertInRange(-180.0F, +180.0F, record.longitude);   // deg
        assertInRange(-1000.0F, +10000.0F, record.altitude); // m
        assertInRange(0, 255, record.btDataCount); // #
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

        final Object[] data = gridPointData.bandData;
        assertNotNull(data);
//        assertEquals(data.length);
//        testBtData(gridPointData.btData[0]);
//        testBtData(gridPointData.btData[gridPointData.btDataCount - 1]);
    }


    private void testSmosD1cMdsr(SmosD1cMdsr mdsr) {
        assertInRange(0, 65535, mdsr.flags);
        assertInRange(-10.0F, 400.0F, mdsr.btValueReal);
        assertInRange(-10.0F, 400.0F, mdsr.btValueImag);
        assertInRange(0.0F, 90.0F, mdsr.incidenceAngle);
        assertInRange(0.0F, 360.0F, mdsr.azimuthAngle);
        assertInRange(0.0F, 360.0F, mdsr.geometricRotationAngle);
        assertInRange(0.0F, 360.0F, mdsr.faradayRotationAngle);
        assertInRange(0, 50000, mdsr.snapshotId);
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


    // Skips all tests in this TestCase if the property is not defined
    @Override
    public void runBare() throws Throwable {
        final String propertyName = getClass().getName() + ".productDir";
        final String sclf1cDirPath = System.getProperty(propertyName);
        if (sclf1cDirPath == null || sclf1cDirPath.isEmpty()) {
            System.err.println("System property '" + propertyName + "' must be set in order to perform reader tests!");
            return;
        }
        super.runBare();

    }

    @Override
    protected void setUp() throws Exception {
        final String propertyName = getClass().getName() + ".productDir";
        final String sclf1cDirPath = System.getProperty(propertyName);
        if (sclf1cDirPath == null || sclf1cDirPath.isEmpty()) {
            throw new Exception("System property '" + propertyName + "' must be set in order to perform reader tests!");
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

}
