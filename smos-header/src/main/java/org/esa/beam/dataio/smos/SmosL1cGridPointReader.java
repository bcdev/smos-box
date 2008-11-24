package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


public class SmosL1cGridPointReader {
    private final SmosL1cFieldDescriptor[] fieldDescriptors;
    private final int recordSize;
    private final ImageInputStream dataInputStream;

    public SmosL1cGridPointReader(SmosL1cFieldDescriptor[] fieldDescriptors,
                                  int recordSize,
                                  ImageInputStream dataInputStream) {
        this.fieldDescriptors = fieldDescriptors;
        this.recordSize = recordSize;
        this.dataInputStream = dataInputStream;
    }

    SmosL1cGridPointReader(SmosL1cRecordDescriptor recordDescriptor,
                           int[] fieldIndexes,
                           ImageInputStream dataInputStream) {
        this.fieldDescriptors = recordDescriptor.getFieldDescriptors(fieldIndexes);
        this.recordSize = recordDescriptor.getSize();
        this.dataInputStream = dataInputStream;
    }

    public SmosL1cGridPointData readNext() throws IOException {
        final SmosL1cGridPointData data = new SmosL1cGridPointData();
        data.readFrom(fieldDescriptors, recordSize, dataInputStream);
        return data;
    }
}