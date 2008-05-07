package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private final int btDataDsrSize;
    private InputStream headerInputStream;

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
        btDataDsrSize = recordDescriptor.getRecordSize();

//        int minGridPointId = Integer.MAX_VALUE;
//        int maxGridPointId = Integer.MIN_VALUE;
//        final long t0 = System.currentTimeMillis();
//
//        dataInputStream.seek(gridPointDsOffset);
//        for (int i = 0; i < gridPointCount; i++) {
//            final long position = dataInputStream.getStreamPosition();
//            int gridPointId = dataInputStream.readInt();
//            int dggridSeqnum = SmosDgg.smosGridPointIdToDggridSeqnum(gridPointId);
//            dataInputStream.skipBytes(3 * 4 + 1);
//            int btDataCount = dataInputStream.readByte() & 0xFF;
//            dataInputStream.skipBytes(btDataCount * btDataDsrSize);
//            minGridPointId = Math.min(dggridSeqnum, minGridPointId);
//            maxGridPointId = Math.max(dggridSeqnum, maxGridPointId);
//        }
//
//        // Check that we are at EOF
//        try {
//            dataInputStream.readByte();
//            throw new IOException("EOF expected");
//        } catch (EOFException e) {
//            // OK, expected
//        }
//
//        System.out.println("minGridPointId = " + minGridPointId);
//        System.out.println("maxGridPointId = " + maxGridPointId);
//        final int availableGridPointCount = maxGridPointId - minGridPointId;
//        System.out.println("availableGridPointCount = " + availableGridPointCount);
//
//        final long t1 = System.currentTimeMillis();
//        final long dt = t1 - t0;
//        System.out.println("dt = " + dt);
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

    public int getBtDataDsrSize() {
        return btDataDsrSize;
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
                                          recordDescriptor.getRecordSize(),
                                          dataInputStream);
    }

    public SmosL1cGridPointReader createSmosL1cGridPointReader(int[] fieldIndexes) throws IOException {
        dataInputStream.seek(gridPointDsOffset);
        return new SmosL1cGridPointReader(recordDescriptor.getFieldDescriptors(fieldIndexes),
                                          recordDescriptor.getRecordSize(),
                                          dataInputStream);
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
}
