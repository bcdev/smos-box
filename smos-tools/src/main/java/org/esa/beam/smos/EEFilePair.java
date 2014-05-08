package org.esa.beam.smos;

import java.io.File;

public class EEFilePair {
    private final File hdrFile;
    private final File dblFile;

    public EEFilePair(File hdrFile, File dblFile) {
        this.hdrFile = hdrFile;
        this.dblFile = dblFile;
    }

    public File getHdrFile() {
        return hdrFile;
    }

    public File getDblFile() {
        return dblFile;
    }
}
