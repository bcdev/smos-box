package org.esa.beam.dataio.smos;

import java.io.File;
import java.io.FilenameFilter;

class ExplorerFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name != null && (name.endsWith(".HDR") || name.endsWith(".DBL"));
    }
}
