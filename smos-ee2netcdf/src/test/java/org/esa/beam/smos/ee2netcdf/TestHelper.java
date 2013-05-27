package org.esa.beam.smos.ee2netcdf;

import java.io.File;

public class TestHelper {

    public static File getResourceFile(String filename) {
        final File resourceDirectory = getResourceDirectory();
        return new File(resourceDirectory, filename);
    }

    public static File getResourceDirectory() {
        File resourceDir = new File("./smos-ee2netcdf/src/test/resources/org/esa/beam/smos/ee2netcdf/");
        if (!resourceDir.isDirectory()) {
            resourceDir = new File("./src/test/resources/org/esa/beam/smos/ee2netcdf/");
        }
        return resourceDir;
    }
}
