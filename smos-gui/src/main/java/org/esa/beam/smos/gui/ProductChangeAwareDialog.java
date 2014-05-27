package org.esa.beam.smos.gui;

import com.bc.ceres.swing.selection.Selection;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import com.bc.ceres.swing.selection.SelectionManager;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;
import org.esa.beam.framework.ui.ModelessDialog;
import org.esa.beam.framework.ui.product.tree.AbstractTN;

import javax.swing.tree.TreePath;
import java.awt.*;

abstract public class ProductChangeAwareDialog extends ModelessDialog {


    protected ProductChangeAwareDialog(Window parent, String title, int buttonMask, String helpID) {
        super(parent, title, buttonMask, helpID);
    }

    protected void productRemoved(Product product) {
    }

    protected void productAdded() {
    }

    protected void geometryAdded() {
    }

    protected void geometryRemoved() {
    }

    protected void productSelectionChanged() {

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


    public static class GeometryListener implements ProductNodeListener {

        private final ProductChangeAwareDialog dialog;

        public GeometryListener(ProductChangeAwareDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void nodeChanged(ProductNodeEvent event) {
        }

        @Override
        public void nodeDataChanged(ProductNodeEvent event) {
        }

        @Override
        public void nodeAdded(ProductNodeEvent event) {
            dialog.geometryAdded();
        }

        @Override
        public void nodeRemoved(ProductNodeEvent event) {
            dialog.geometryRemoved();
        }
    }

    public static class ProductSelectionListener implements SelectionChangeListener {

        private final ProductChangeAwareDialog dialog;
        private final SelectionManager selectionManager;

        public ProductSelectionListener(ProductChangeAwareDialog dialog, SelectionManager selectionManager) {
            this.dialog = dialog;
            this.selectionManager = selectionManager;
        }

        @Override
        public void selectionChanged(SelectionChangeEvent selectionChangeEvent) {
            final Selection selection = selectionChangeEvent.getSelection();
            if (selection != null) {
                final Object selectedValue = selection.getSelectedValue();
                if (selectedValue != null && selectedValue instanceof TreePath) {
                    final TreePath treePath = (TreePath) selectedValue;
                    final Object lastPathComponent = treePath.getLastPathComponent();
                    if (lastPathComponent instanceof AbstractTN) {
                        final AbstractTN treeNode = (AbstractTN) lastPathComponent;
                        final Object content = treeNode.getContent();
                        if (content instanceof Product) {
                            dialog.productSelectionChanged();
                        }
                    }

                }
            }
        }

        @Override
        public void selectionContextChanged(SelectionChangeEvent selectionChangeEvent) {

        }

        public void dispose() {
            selectionManager.removeSelectionChangeListener(this);
        }
    }
}
