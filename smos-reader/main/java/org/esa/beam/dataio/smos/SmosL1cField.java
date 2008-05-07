package org.esa.beam.dataio.smos;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

public abstract class SmosL1cField {
    private final SmosL1cFieldDescriptor descriptor;

    protected SmosL1cField(SmosL1cFieldDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public SmosL1cFieldDescriptor getDescriptor() {
        return descriptor;
    }

    public abstract Object getData();
    public abstract void readDataElement(ImageInputStream iis, int index) throws IOException;
}
