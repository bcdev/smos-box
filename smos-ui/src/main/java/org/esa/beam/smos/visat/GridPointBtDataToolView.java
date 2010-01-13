package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.visat.VisatApp;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.AbstractButton;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

public abstract class GridPointBtDataToolView extends SmosToolView {

    public static final String ID = GridPointBtDataToolView.class.getName();

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
        SmosFile smosFile = getSelectedSmosFile();
        L1cSmosFile l1cSmosFile = null;
        if (smosFile instanceof L1cSmosFile) {
            l1cSmosFile = (L1cSmosFile) smosFile;
        } else {
            // If smosFile is not an L1cSmosFile (i.e. its an L2SmosFile)
            // find the corresponding L1cSmosFile
            final Product selectedProduct = getSelectedSmosProduct();
            final MetadataElement element = findElement(selectedProduct.getMetadataRoot(), "List_of_Data_Sets");
            if (element != null) {
                final String name = getRefFilename(element);
                if (name != null) {
                    l1cSmosFile = findL1cSmosFile(name);
                }
            }
        }
        return l1cSmosFile;
    }

    final void updateGridPointBtDataComponent() {
        int id = -1;
        if (!snapToSelectedPinCheckBox.isSelected()) {
            id = SmosBox.getInstance().getGridPointSelectionService().getSelectedGridPointId();
        } else {
            final Pin selectedPin = getSelectedSmosProduct().getPinGroup().getSelectedNode();

            if (selectedPin != null) {
                final PixelPos pixelPos = selectedPin.getPixelPos();
                final int x = (int) Math.floor(pixelPos.getX());
                final int y = (int) Math.floor(pixelPos.getY());
                id = SmosBox.getInstance().getSmosViewSelectionService().getGridPointId(x, y);
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

    private static String getRefFilename(MetadataElement element) {
        for (final MetadataElement metadataElement : element.getElements()) {
            if ("L1C_SM_FILE".equals(metadataElement.getAttributeString("DS_Name", ""))
                    || "L1C_OS_FILE".equals(metadataElement.getAttributeString("DS_Name", ""))) {
                final String name = metadataElement.getAttributeString("Ref_Filename", "");
                return getRelevantProductname(name);
            }
        }
        return null;
    }
    
    private static String getRelevantProductname(String name) {
        if (name.length() > 10) {
            // ignore version numbers in file name
            return name.substring(0, name.length() - 10);
        }
        return null;
    }

    private static L1cSmosFile findL1cSmosFile(String name) {
        final Product[] products = VisatApp.getApp().getProductManager().getProducts();

        for (final Product product : products) {
            final ProductReader productReader = product.getProductReader();
            if (productReader instanceof SmosProductReader) {
                final ExplorerFile smosFile = ((SmosProductReader) productReader).getExplorerFile();
                if (smosFile instanceof L1cSmosFile) {
                    String productName = product.getName();
                    String relevantProductname = getRelevantProductname(productName);
                    if (name.equalsIgnoreCase(relevantProductname)) {
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
            if (!snapToSelectedPinCheckBox.isSelected()) {
                updateGridPointBtDataComponent(newId);
            }
        }
    }

    private class IL implements ItemListener {
        private final ProductNodeListener pnl;

        private IL() {
            pnl = new PNL();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateGridPointBtDataComponent();
                getSelectedSmosProduct().addProductNodeListener(pnl);
            } else {
                getSelectedSmosProduct().removeProductNodeListener(pnl);
            }
        }

        private class PNL implements ProductNodeListener {
            @Override
            public void nodeChanged(ProductNodeEvent event) {
                // selection, pixel position, etc.
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
                if (sourceNode instanceof Pin) {
                    updateGridPointBtDataComponent();
                }
            }
        }
    }
}