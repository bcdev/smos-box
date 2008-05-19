package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.ByteOrder;

public class SmosL1cReader {
    private final SmosL1cRecordDescriptor recordDescriptor;
    private final SmosHeader header;
    private final ImageInputStream dataInputStream;
    private final int snapshotCount;
    private final long snapshotDsOffset;
    private final int snapshotDsrSize;
    private final int gridPointCount;
    private final long gridPointDsOffset;
    private final int gridPointDsrSize;
    private final int gridPointMdsrSize;
    private InputStream headerInputStream;
    private SmosGridPointInfo gridPointInfo;

    public SmosL1cReader(InputStream headerInputStream, ImageInputStream dataInputStream) throws IOException {

        header = new SmosHeader(headerInputStream);

        final String fileType = header.getFixedHeader().getFileType();
        recordDescriptor = SmosL1cRecordDescriptor.create(fileType);
        if (recordDescriptor == null) {
            throw new IOException("Unknown product type");
        }

        this.headerInputStream = headerInputStream;
        this.dataInputStream = dataInputStream;

        dataInputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        dataInputStream.seek(0L);
        snapshotCount = dataInputStream.readInt();
        snapshotDsOffset = dataInputStream.getStreamPosition();
        snapshotDsrSize = 161;

        dataInputStream.seek(snapshotCount * 161L + 4L);
        gridPointCount = dataInputStream.readInt();
        gridPointDsOffset = dataInputStream.getStreamPosition();
        gridPointDsrSize = 18;
        gridPointMdsrSize = recordDescriptor.getSize();

        final long t0 = System.currentTimeMillis();
        gridPointInfo = createGridPointInfo();
        final long t1 = System.currentTimeMillis();
        final long dt = t1 - t0;
        System.out.println("gridPointInfo = " + gridPointInfo +  ", time = " + dt + " ms");
//        gridPointInfo.dump(new PrintStream(new FileOutputStream("gridPointInfo.txt")));
    }

    SmosGridPointInfo createGridPointInfo() throws IOException {
        int minGridPointId = Integer.MAX_VALUE;
        int maxGridPointId = Integer.MIN_VALUE;

        dataInputStream.seek(gridPointDsOffset);
        for (int i = 0; i < gridPointCount; i++) {
            int gridPointId = readGridpointId();
            minGridPointId = Math.min(gridPointId, minGridPointId);
            maxGridPointId = Math.max(gridPointId, maxGridPointId);
        }

        // Check that we are at EOF
        if (!isEof()) {
            throw new IllegalStateException("Internal error or Illegal file format?");
        }

        final SmosGridPointInfo gridPointInfo = new SmosGridPointInfo(SmosDgg.smosGridPointIdToDggridSeqnum(minGridPointId),
                                                              SmosDgg.smosGridPointIdToDggridSeqnum(maxGridPointId));
        dataInputStream.seek(gridPointDsOffset);
        for (int i = 0; i < gridPointCount; i++) {
            final long position = dataInputStream.getStreamPosition();
            int gridPointId = readGridpointId();
            int dggridSeqnum = SmosDgg.smosGridPointIdToDggridSeqnum(gridPointId);
            gridPointInfo.setOffset(dggridSeqnum, (int) (position - gridPointDsOffset));
        }

        // Check that we are at EOF
        if (!isEof()) {
            throw new IllegalStateException("Internal error or Illegal file format?");
        }

        return gridPointInfo;
    }

    int readGridpointId() throws IOException {
//        return readGridpointIdImpl1();
//        return readGridpointIdImpl2();
        return readGridpointIdImpl3();
    }

    int readGridpointIdImpl1() throws IOException {
        int gridPointId = dataInputStream.readInt();
        dataInputStream.skipBytes(3 * 4 + 1);
        int btDataCount = dataInputStream.readByte() & 0xFF;
        dataInputStream.skipBytes(btDataCount * gridPointMdsrSize);
        return gridPointId;
    }

    int readGridpointIdImpl2() throws IOException {
        final SmosL1cGridPointRecord gridPointRecord = new SmosL1cGridPointRecord();
        gridPointRecord.readFrom(dataInputStream);
        return gridPointRecord.gridPointId;
    }

    byte[] gridPointDataBuffer;
    final ByteArrayDecoder arrayDecoder = new ByteArrayDecoder(ByteOrder.LITTLE_ENDIAN);

    private int readGridpointIdImpl3() throws IOException {
        if (gridPointDataBuffer == null) {
             gridPointDataBuffer = new byte[getGridPointDsrSize() + 255 * getGridPointMdsrSize()];
        }
        dataInputStream.read(gridPointDataBuffer, 0, getGridPointDsrSize());
        int gridPointId = arrayDecoder.toInt(gridPointDataBuffer, 0);
        int btDataCount = arrayDecoder.toByte(gridPointDataBuffer, 17) & 0xff;
        dataInputStream.read(gridPointDataBuffer, getGridPointDsrSize(), btDataCount * getGridPointMdsrSize());
        return gridPointId;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public SmosHeader getHeader() {
        return header;
    }

    public ImageInputStream getDataInputStream() {
        return dataInputStream;
    }

    public int getSnapshotCount() {
        return snapshotCount;
    }

    public int getGridPointCount() {
        return gridPointCount;
    }

    public long getSnapshotDsOffset() {
        return snapshotDsOffset;
    }

    public int getSnapshotDsrSize() {
        return snapshotDsrSize;
    }

    public long getGridPointDsOffset() {
        return gridPointDsOffset;
    }

    public int getGridPointDsrSize() {
        return gridPointDsrSize;
    }

    public int getGridPointMdsrSize() {
        return gridPointMdsrSize;
    }

    public SmosSnapshotInfo readSnapshotInfo(int index) throws IOException {
        final SmosSnapshotInfo snapshotInformation = new SmosSnapshotInfo();
        dataInputStream.seek(snapshotDsOffset + snapshotDsrSize * index);
        snapshotInformation.readFrom(dataInputStream);
        return snapshotInformation;
    }

    public SmosL1cGridPointReader createSmosL1cGridPointReader() throws IOException {
        dataInputStream.seek(gridPointDsOffset);
        return new SmosL1cGridPointReader(recordDescriptor.getFieldDescriptors(),
                                          recordDescriptor.getSize(),
                                          dataInputStream);
    }

    public SmosL1cGridPointReader createSmosL1cGridPointReader(int[] fieldIndexes) throws IOException {
        dataInputStream.seek(gridPointDsOffset);
        return new SmosL1cGridPointReader(recordDescriptor.getFieldDescriptors(fieldIndexes),
                                          recordDescriptor.getSize(),
                                          dataInputStream);
    }


    public SmosL1cGridPointRecord readGridPointRecord() throws IOException {
        final SmosL1cGridPointRecord smosL1cGridPointRecord = new SmosL1cGridPointRecord();
        smosL1cGridPointRecord.readFrom(dataInputStream);
        return smosL1cGridPointRecord;
    }

    public void close() {
        try {
            headerInputStream.close();
        } catch (IOException e) {
            // ignore
        }

        try {
            dataInputStream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private boolean isEof() {
        try {
            final long position = dataInputStream.getStreamPosition();
            dataInputStream.readByte();
            dataInputStream.seek(position);
        } catch (EOFException e) {
            return true;
        } catch (IOException e) {
            // ok
        }
        return false;
    }
}
