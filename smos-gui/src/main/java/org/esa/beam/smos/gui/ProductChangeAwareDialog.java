package org.esa.beam.smos.gui;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.ui.ModelessDialog;

import java.awt.*;

abstract public class ProductChangeAwareDialog extends ModelessDialog {


    public ProductChangeAwareDialog(Window parent, String title, int buttonMask, String helpID) {
        super(parent, title, buttonMask, helpID);
    }

    protected void productRemoved(Product product) {
    }

    protected void productAdded() {
    }

    protected static class ProductManagerListener implements ProductManager.Listener {

        private final ProductChangeAwareDialog dialog;

        public ProductManagerListener(ProductChangeAwareDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void productAdded(ProductManager.Event event) {
            dialog.productAdded();
        }

        @Override
        public void productRemoved(ProductManager.Event event) {
            dialog.productRemoved(event.getProduct());
        }
    }
}
