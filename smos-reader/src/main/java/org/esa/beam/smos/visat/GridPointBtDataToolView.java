package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.L1cSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;

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

        final SmosFile smosFile = getSelectedSmosFile();
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