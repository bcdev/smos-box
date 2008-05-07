package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public class SmosL1cGridPointData {
    int gridPointId;
    float latitude;
    float longitude;
    float altitude;
    int mask;
    int btDataCount;
    Object[] bandData;

    void readFrom(SmosL1cFieldDescriptor[] fieldDescriptors, int recordSize, ImageInputStream stream) throws IOException {

        gridPointId = stream.readInt();         //  0 -->  4
        latitude = stream.readFloat();          //  4 -->  8
        longitude = stream.readFloat();         //  8 --> 12
        altitude = stream.readFloat();          // 12 --> 16
        mask = stream.readByte() & 0xFF;        // 16 --> 17
        btDataCount = stream.readByte() & 0xFF; // 17 --> 18

        final long pos0 = stream.getStreamPosition();
        bandData = new Object[fieldDescriptors.length];
        SmosL1cField[] smosL1cFields = new SmosL1cField[fieldDescriptors.length];
        for (int i = 0; i < fieldDescriptors.length; i++) {
            final SmosL1cFieldDescriptor bandDescriptor = fieldDescriptors[i];
            smosL1cFields[i] = bandDescriptor.createField(btDataCount);
            bandData[i] = smosL1cFields[i].getData();
        }
        for (int btDataIndex = 0; btDataIndex < btDataCount; btDataIndex++) {
            for (int i = 0; i < fieldDescriptors.length; i++) {
                final SmosL1cFieldDescriptor bandDescriptor = fieldDescriptors[i];
                stream.seek(pos0 + btDataIndex * recordSize + bandDescriptor.offset);
                smosL1cFields[i].readDataElement(stream, btDataIndex);
            }
        }
        stream.seek(pos0 + btDataCount * recordSize);
    }

}