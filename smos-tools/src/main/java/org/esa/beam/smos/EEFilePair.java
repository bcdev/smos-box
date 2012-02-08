package org.esa.beam.smos;

import java.io.File;

public class EEFilePair {
    private File hdrFile;
    private File dblFile;

    public void setHdrFile(File hdrFile) {
        this.hdrFile = hdrFile;
    }

    public File getHdrFile() {
        return hdrFile;
    }

    public void setDblFile(File dblFile) {
        this.dblFile = dblFile;
    }

    public File getDblFile() {
        return dblFile;
    }
}
