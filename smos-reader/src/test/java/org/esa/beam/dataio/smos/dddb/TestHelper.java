package org.esa.beam.dataio.smos.dddb;

import java.io.File;

public class TestHelper {

    public static File getResourceFile(String filename) {
        final File resourceDirectory = getResourceDirectory();
        return new File(resourceDirectory, filename);
    }

    public static File getResourceDirectory() {
        File resourceDir = new File("./smos-reader/src/test/resources/org/esa/beam/dataio/smos/dddb/");
        if (!resourceDir.isDirectory()) {
            resourceDir = new File("./src/test/resources/org/esa/beam/dataio/smos/dddb/");
        }
        return resourceDir;
    }
}
