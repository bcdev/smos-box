package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.smos.ee2netcdf.ExportParameter;

class DialogHelper {

    static boolean isSupportedType(String productType) {
        return productType.matches(ExportParameter.PRODUCT_TYPE_REGEX);
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
