package org.esa.beam.smos.ee2netcdf.visat;

public class ExportParameter {
    private boolean useSelectedProduct;

    public void setUseSelectedProduct(boolean useSelectedProduct) {
        this.useSelectedProduct = useSelectedProduct;
    }

    public boolean isUseSelectedProduct() {
        return useSelectedProduct;
    }
}
