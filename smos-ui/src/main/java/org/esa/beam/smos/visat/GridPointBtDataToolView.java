package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.visat.VisatApp;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

        SmosFile smosFile = getSelectedSmosFile();

        // If smosFile is not an L1cSmosFile (i.e. its an L2SmosFile)
        // find the corresponding L1cSmosFile
        if (!(smosFile instanceof L1cSmosFile)) {
            final Product selectedProduct = getSelectedSmosProduct();
            final MetadataElement element = findElement(selectedProduct.getMetadataRoot(), "List_of_Data_Sets");
            if (element != null) {
                final String name = getRefFilename(element);
                if (name != null) {
                    final L1cSmosFile l1cSmosFile = findL1cSmosFile(name);
                    if (l1cSmosFile != null) {
                        smosFile = l1cSmosFile;
                    }
                }
            }
        }
        final int gridPointIndex = smosFile.getGridPointIndex(selectedGridPointId);

        if (gridPointIndex >= 0 && smosFile instanceof L1cSmosFile) {
            setInfoText("" +
                    "<html>" +
                    "SEQNUM=<b>" + selectedGridPointId + "</b>, " +
                    "INDEX=<b>" + gridPointIndex + "</b>" +
                    "</html>");

            try {
                GridPointBtDataset ds = GridPointBtDataset.read((L1cSmosFile) smosFile, gridPointIndex);
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
        // TODO: implement
        return null;
    }

    private static String getRefFilename(MetadataElement element) {
        for (final MetadataElement metadataElement : element.getElements()) {
            if ("L1C_SM_FILE".equals(metadataElement.getAttributeString("DS_Name", ""))
                    || "L1C_OS_FILE".equals(metadataElement.getAttributeString("DS_Name", ""))) {
                final String name = metadataElement.getAttributeString("Ref_Filename", "");
                if (name.length() > 10) {
                    // ignore version numbers in file name
                    return name.substring(0, name.length() - 10);
                }
                break;
            }
        }

        return null;
    }

    private static L1cSmosFile findL1cSmosFile(String name) {
        final Product[] products = VisatApp.getApp().getProductManager().getProducts();

        for (final Product product : products) {
            final ProductReader productReader = product.getProductReader();
            if (productReader instanceof SmosProductReader) {
                final SmosFile smosFile = ((SmosProductReader) productReader).getSmosFile();
                if (smosFile instanceof L1cSmosFile) {
                    if (name.equalsIgnoreCase(smosFile.getFile().getName())) {
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