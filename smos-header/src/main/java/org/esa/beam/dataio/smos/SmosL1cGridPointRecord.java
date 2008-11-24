package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public class SmosL1cGridPointRecord {
    int gridPointId;
    float latitude;
    float longitude;
    float altitude;
    int mask;
    int btDataCount;
    SmosD1cMdsr[] btDataRecords;

    void readFrom(ImageInputStream stream) throws IOException {
        gridPointId = stream.readInt();         //  0 -->  4
        latitude = stream.readFloat();          //  4 -->  8
        longitude = stream.readFloat();         //  8 --> 12
        altitude = stream.readFloat();          // 12 --> 16
        mask = stream.readByte() & 0xFF;        // 16 --> 17
        btDataCount = stream.readByte() & 0xFF; // 17 --> 18
        btDataRecords = new SmosD1cMdsr[btDataCount];
        for (int i = 0; i < btDataCount; i++) {
            final SmosD1cMdsr mdsr = new SmosD1cMdsr();
            mdsr.readFrom(stream);
            btDataRecords[i] = mdsr;
        }
    }
}