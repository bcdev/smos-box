package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public abstract class GridPointBtDataToolView extends SmosToolView {

    public static final String ID = GridPointBtDataToolView.class.getName();

    private static final String TAG_DS_NAME = "DS_Name";
    private static final String TAG_LIST_OF_DATA_SETS = "List_of_Data_Sets";
    private static final String TAG_REF_FILENAME = "Ref_Filename";

    private JLabel infoLabel;
    private JCheckBox snapToSelectedPinCheckBox;
    private GPSL gpsl;

    @Override
    protected JComponent createClientComponent() {
        infoLabel = new JLabel();
        snapToSelectedPinCheckBox = new JCheckBox("Snap to selected pin");
        snapToSelectedPinCheckBox.addItemListener(new IL());

        final JPanel optionsPanel = new JPanel(new BorderLayout(6, 0));
        optionsPanel.add(snapToSelectedPinCheckBox, BorderLayout.WEST);
        optionsPanel.add(createGridPointComponentOptionsComponent(), BorderLayout.CENTER);

        final JPanel mainPanel = new JPanel(new BorderLayout(2, 2));
        mainPanel.add(infoLabel, BorderLayout.CENTER);
        mainPanel.add(createGridPointComponent(), BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);

        final AbstractButton helpButton = createHelpButton();
        optionsPanel.add(helpButton, BorderLayout.EAST);

        if (getDescriptor().getHelpId() != null) {
            HelpSys.enableHelpOnButton(helpButton, getDescriptor().getHelpId());
            HelpSys.enableHelpKey(mainPanel, getDescriptor().getHelpId());
        }

        return mainPanel;
    }

    protected JComponent createGridPointComponentOptionsComponent() {
        return new JPanel();
    }

    boolean isSnappedToPin() {
        return snapToSelectedPinCheckBox.isSelected();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        gpsl = new GPSL();
        SmosBox.getInstance().getGridPointSelectionService().addGridPointSelectionListener(gpsl);
        updateGridPointBtDataComponent(SmosBox.getInstance().getGridPointSelectionService().getSelectedGridPointId());
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        SmosBox.getInstance().getGridPointSelectionService().removeGridPointSelectionListener(gpsl);
        updateGridPointBtDataComponent(-1);
    }

    protected L1cSmosFile getL1cSmosFile() {
        final SmosFile smosFile = getSelectedSmosFile();
        if (smosFile instanceof L1cSmosFile) {
            return (L1cSmosFile) smosFile;
        } else if (smosFile != null) {
            // find the L1c SMOS file corresponding to the selected SMOS file
            final Product selectedProduct = getSelectedSmosProduct();
            if (selectedProduct != null) {
                final MetadataElement element = findElement(selectedProduct.getMetadataRoot(), TAG_LIST_OF_DATA_SETS);
                if (element != null) {
                    final String referredFileName = getReferredFileName(element);
                    if (referredFileName != null) {
                        return findL1cSmosFile(referredFileName);
                    }
                }
            }
        }
        return null;
    }

    final void updateGridPointBtDataComponent() {
        int id = -1;
        if (!isSnappedToPin()) {
            id = SmosBox.getInstance().getGridPointSelectionService().getSelectedGridPointId();
        } else {
            final ProductSceneView view = getSelectedSmosView();
            if (view != null) {
                final Placemark selectedPin = view.getSelectedPin();
                if (selectedPin != null) {
                    final PixelPos pixelPos = selectedPin.getPixelPos();
                    final int x = (int) Math.floor(pixelPos.getX());
                    final int y = (int) Math.floor(pixelPos.getY());
                    id = SmosBox.getInstance().getSmosViewSelectionService().getGridPointId(x, y);
                }
            }
        }
        updateGridPointBtDataComponent(id);
    }

    private void updateGridPointBtDataComponent(int selectedGridPointId) {
        if (selectedGridPointId == -1) {
            setInfoText("No data");
            clearGridPointBtDataComponent();
            return;
        }
        L1cSmosFile l1cSmosFile = getL1cSmosFile();
        final int gridPointIndex = l1cSmosFile != null ? l1cSmosFile.getGridPointIndex(selectedGridPointId) : -1;

        if (gridPointIndex >= 0) {
            setInfoText("" +
                        "<html>" +
                        "SEQNUM=<b>" + selectedGridPointId + "</b>, " +
                        "INDEX=<b>" + gridPointIndex + "</b>" +
                        "</html>");

            try {
                GridPointBtDataset ds = GridPointBtDataset.read(l1cSmosFile, gridPointIndex);
                updateGridPointBtDataComponent(ds);
            } catch (IOException e) {
                updateGridPointBtDataComponent(e);
            }
        } else {
            setInfoText("No data");
            clearGridPointBtDataComponent();
        }
    }

    protected void setInfoText(String text) {
        infoLabel.setText(text);
    }

    protected abstract JComponent createGridPointComponent();

    protected abstract void updateGridPointBtDataComponent(GridPointBtDataset ds);

    protected abstract void updateGridPointBtDataComponent(IOException e);

    protected abstract void clearGridPointBtDataComponent();

    private static MetadataElement findElement(MetadataElement element, String elementName) {
        if (element.getName().equals(elementName)) {
            return element;
        } else {
            for (final MetadataElement childElement : element.getElements()) {
                MetadataElement metadataElement = findElement(childElement, elementName);
                if (metadataElement != null) {
                    return metadataElement;
                }
            }
        }
        return null;
    }

    private static String getReferredFileName(MetadataElement element) {
        for (final MetadataElement metadataElement : element.getElements()) {
            if ("L1C_SM_FILE".equals(metadataElement.getAttributeString(TAG_DS_NAME, ""))
                || "L1C_OS_FILE".equals(metadataElement.getAttributeString(TAG_DS_NAME, ""))) {
                final String name = metadataElement.getAttributeString(TAG_REF_FILENAME, "");
                return trimVersionNumber(name);
            }
        }
        return null;
    }

    private static String trimVersionNumber(String productName) {
        if (productName.length() > 10) {
            return productName.substring(0, productName.length() - 10);
        }
        return null;
    }

    private static L1cSmosFile findL1cSmosFile(String referredFileName) {
        final Product[] products = VisatApp.getApp().getProductManager().getProducts();

        for (final Product product : products) {
            final ProductReader productReader = product.getProductReader();
            if (productReader instanceof SmosProductReader) {
                final ExplorerFile smosFile = ((SmosProductReader) productReader).getExplorerFile();
                if (smosFile instanceof L1cSmosFile) {
                    if (referredFileName.equalsIgnoreCase(trimVersionNumber(product.getName()))) {
                        return (L1cSmosFile) smosFile;
                    }
                }
            }
        }
        return null;
    }

    private class GPSL implements GridPointSelectionService.SelectionListener {

        @Override
        public void handleGridPointSelectionChanged(int oldId, int newId) {
            if (!isSnappedToPin()) {
                updateGridPointBtDataComponent(newId);
            }
        }
    }

    private class IL implements ItemListener {

        private final PCL pcl;
        private final PNL pnl;
        private final VSL vsl;

        private IL() {
            pcl = new PCL();
            pnl = new PNL();
            vsl = new VSL();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateGridPointBtDataComponent();
                SmosBox.getInstance().getSmosViewSelectionService().addSceneViewSelectionListener(vsl);
                getSelectedSmosView().addPropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                getSelectedSmosProduct().addProductNodeListener(pnl);
            } else {
                getSelectedSmosProduct().removeProductNodeListener(pnl);
                getSelectedSmosView().removePropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                SmosBox.getInstance().getSmosViewSelectionService().removeSceneViewSelectionListener(vsl);
            }
        }

        private class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateGridPointBtDataComponent();
            }
        }

        private class PNL implements ProductNodeListener {

            @Override
            public void nodeChanged(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeDataChanged(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeAdded(ProductNodeEvent event) {
                updatePin(event);
            }

            @Override
            public void nodeRemoved(ProductNodeEvent event) {
                updatePin(event);
            }

            private void updatePin(ProductNodeEvent event) {
                final ProductNode sourceNode = event.getSourceNode();
                if (sourceNode instanceof Placemark) {
                    updateGridPointBtDataComponent();
                }
            }
        }

        private class VSL implements SceneViewSelectionService.SelectionListener {

            @Override
            public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                if (oldView != null) {
                    oldView.removePropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                }
                if (newView != null) {
                    newView.addPropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pcl);
                }
                updateGridPointBtDataComponent();
            }
        }
    }
}