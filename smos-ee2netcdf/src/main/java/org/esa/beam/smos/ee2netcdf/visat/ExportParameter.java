package org.esa.beam.smos.ee2netcdf.visat;

import java.io.File;

public class ExportParameter {
    private boolean useSelectedProduct;
    private File sourceDirectory;

    public void setUseSelectedProduct(boolean useSelectedProduct) {
        this.useSelectedProduct = useSelectedProduct;
    }

    public boolean isUseSelectedProduct() {
        return useSelectedProduct;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }
}
