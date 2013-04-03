package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;

class DialogHelper {

    static boolean isSupportedType(String productType) {
        return "MIR_BWLF1C".equalsIgnoreCase(productType) ||
                "MIR_BWSF1C".equalsIgnoreCase(productType) ||
                "MIR_BWLD1C".equalsIgnoreCase(productType) ||
                "MIR_BWSD1C".equalsIgnoreCase(productType) ||
                "MIR_SCSD1C".equalsIgnoreCase(productType) ||
                "MIR_SCLD1C".equalsIgnoreCase(productType) ||
                "MIR_SCSF1C".equalsIgnoreCase(productType) ||
                "MIR_SCLF1C".equalsIgnoreCase(productType) ||
                "MIR_OSUDP2".equalsIgnoreCase(productType) ||
                "MIR_SMUDP2".equalsIgnoreCase(productType);
    }

    static boolean canProductSelectionBeEnabled(AppContext appContext) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            return isSupportedType(selectedProduct.getProductType());
        }
        return false;
    }

    static Product getSelectedSmosProduct(AppContext appContext) {
        final Product selectedProduct = appContext.getSelectedProduct();
        if (selectedProduct != null) {
            if (isSupportedType(selectedProduct.getProductType())) {
                return selectedProduct;
            }
        }
        return null;
    }
}
