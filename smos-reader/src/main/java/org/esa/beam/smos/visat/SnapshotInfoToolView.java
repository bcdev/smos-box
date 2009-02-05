package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.dataio.smos.*;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.jfree.layout.CenterLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;

public class SnapshotInfoToolView extends SmosToolView {

    public static final String ID = SnapshotInfoToolView.class.getName();

    private SnapshotSelectorCombo snapshotSelectorCombo;
    private JTable snapshotTable;
    private L1cScienceSmosFile smosFile;
    private final SnapshotTableModel nullModel;
    private SliderChangeListener snapshotSliderListener;
    private AbstractButton snapshotModeButton;
    private AbstractButton locateSnapshotButton;
    private SSSL sssl;

    public SnapshotInfoToolView() {
        nullModel = new SnapshotTableModel(new Object[0][0]);
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        sssl = new SSSL();
        SmosBox.getInstance().getSnapshotSelectionService().addSnapshotIdChangeListener(sssl);
        realizeSnapshotIdChange(getSelectedSmosProduct());
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        SmosBox.getInstance().getSnapshotSelectionService().removeSnapshotIdChangeListener(sssl);
    }

    @Override
    protected JComponent createClientComponent() {
        snapshotSliderListener = new SliderChangeListener();

        snapshotTable = new JTable(nullModel);
        snapshotTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    setHorizontalAlignment(RIGHT);
                }
                return this;
            }
        });


        final JCheckBox synchroniseCheckBox = new JCheckBox("Synchronise with L1C view", false);

        snapshotModeButton = ToolButtonFactory.createButton(
                new ImageIcon(SnapshotInfoToolView.class.getResource("Snapshot24.png")), true);
        snapshotModeButton.addActionListener(new ToggleSnapshotModeAction());
        snapshotModeButton.setToolTipText("Toggle snapshot mode on/off");

        locateSnapshotButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/ZoomTool24.gif"), false);
        locateSnapshotButton.addActionListener(new LocateSnapshotAction());
        locateSnapshotButton.setToolTipText("Locate selected snapshot in view");

        final JButton exportButton = new JButton("Export...");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TableModelExportRunner(getPaneWindow(),
                                           getTitle(),
                                           snapshotTable.getModel(),
                                           snapshotTable.getColumnModel()).run();
            }
        });


        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        snapshotSelectorCombo = new SnapshotSelectorCombo();
        JComponent comboComponent = SnapshotSelectorCombo.createComponent(snapshotSelectorCombo, false);
        mainPanel.add(comboComponent, BorderLayout.NORTH);

        final JPanel snapshotTablePanel = new JPanel(new BorderLayout());
        snapshotTablePanel.setBorder(BorderFactory.createTitledBorder("Snapshot Information"));
        snapshotTablePanel.add(new JScrollPane(snapshotTable), BorderLayout.CENTER);
        final JPanel tableButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        tableButtonPanel.add(exportButton);
        snapshotTablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        mainPanel.add(snapshotTablePanel, BorderLayout.CENTER);

        JPanel viewSettingsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        viewSettingsPanel.setBorder(BorderFactory.createTitledBorder("View Settings"));
        viewSettingsPanel.add(synchroniseCheckBox);
        viewSettingsPanel.add(snapshotModeButton);
        viewSettingsPanel.add(locateSnapshotButton);
        mainPanel.add(viewSettingsPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    @Override
    protected synchronized void updateClientComponent(ProductSceneView smosView) {
        boolean enabled = smosView != null && getSelectedSmosFile() instanceof L1cScienceSmosFile;

        snapshotSelectorCombo.getSlider().removeChangeListener(snapshotSliderListener);
        if (enabled) {
            smosFile = (L1cScienceSmosFile) getSelectedSmosFile();
            if (!smosFile.isBackgroundInitStarted()) {
                smosFile.startBackgroundInit();
            }
            if(!smosFile.isBackgoundInitDone()) {
                startPolModeInitWaiting();
                return;
            }

            snapshotSelectorCombo.setModel(new SnapshotSelectorComboModel(smosFile));
            realizeSnapshotIdChange(getSelectedSmosProduct());
            snapshotSelectorCombo.getSlider().addChangeListener(snapshotSliderListener);
        } else {
            smosFile = null;
        }

        snapshotTable.setEnabled(enabled);
        snapshotModeButton.setEnabled(enabled);
        snapshotModeButton.setSelected(getSnapshotIdFromView() != -1);
        locateSnapshotButton.setEnabled(enabled && snapshotModeButton.isSelected());
        updateTable(smosFile.getSnapshotIndex(snapshotSelectorCombo.getSnapshotId()));
    }

    long getSelectedSnapshotId() {
        Product selectedSmosProduct = getSelectedSmosProduct();
        if (selectedSmosProduct == null) {
            return -1;
        }
        return SmosBox.getInstance().getSnapshotSelectionService().getSelectedSnapshotId(selectedSmosProduct);
    }

    public void realizeSnapshotIdChange(Product product) {
        if (product == getSelectedSmosProduct()) {
            long snapshotId = getSelectedSnapshotId();
            if (snapshotId != -1) {
                snapshotSelectorCombo.setSnapshotId(snapshotId);
                long snapshotIndex = smosFile.getSnapshotIndex(snapshotId);
                if (snapshotIndex != -1) {
                    setSnapshotIdOfView();
                } else {
                    snapshotTable.setModel(nullModel);
                }
            }
        }
    }

    private void updateTable(int snapshotIndex) {
        final CompoundData data;
        try {
            data = smosFile.getSnapshotData(snapshotIndex);
        } catch (IOException e) {
            return;
        }

        final CompoundType compoundType = data.getCompoundType();
        final int memberCount = data.getMemberCount();
        final ArrayList<Object[]> list = new ArrayList<Object[]>(memberCount);

        for (int i = 0; i < memberCount; i++) {
            final Object[] entry = new Object[2];
            entry[0] = compoundType.getMemberName(i);

            final Type memberType = compoundType.getMemberType(i);
            if (memberType.isSimpleType()) {
                try {
                    entry[1] = GridPointBtDataset.getNumbericMember(data, i);
                } catch (IOException e) {
                    entry[1] = "Failed reading data";
                }
                list.add(entry);
            }
        }

        snapshotTable.setModel(new SnapshotTableModel(list.toArray(new Object[2][list.size()])));
    }

    private void setSnapshotIdOfView() {
        ProductSceneView sceneView = getSelectedSmosView();
        ImageLayer imageLayer = sceneView.getBaseImageLayer();
        RenderedImage sourceImage = sceneView.getRaster().getSourceImage();
        if (sourceImage instanceof DefaultMultiLevelImage) {
            DefaultMultiLevelImage defaultMultiLevelImage = (DefaultMultiLevelImage) sourceImage;
            if (defaultMultiLevelImage.getSource() instanceof SmosMultiLevelSource) {
                SmosMultiLevelSource smosMultiLevelSource = (SmosMultiLevelSource) defaultMultiLevelImage.getSource();
                GridPointValueProvider gridPointValueProvider = smosMultiLevelSource.getValueProvider();
                if (gridPointValueProvider instanceof L1cFieldValueProvider) {
                    L1cFieldValueProvider l1cFieldValueProvider = (L1cFieldValueProvider) gridPointValueProvider;
                    long id = snapshotModeButton.isSelected() ? getSelectedSnapshotId() : -1;
                    if (l1cFieldValueProvider.getSnapshotId() != id) {
                        l1cFieldValueProvider.setSnapshotId(id);
                        smosMultiLevelSource.reset();
                        sceneView.getRaster().setValidMaskImage(null);
                        sceneView.getRaster().setGeophysicalImage(null);
                        imageLayer.regenerate();
                    }
                }
            }
        }
    }

    private long getSnapshotIdFromView() {
        ProductSceneView sceneView = getSelectedSmosView();
        RenderedImage sourceImage = sceneView.getRaster().getSourceImage();
        if (sourceImage instanceof DefaultMultiLevelImage) {
            DefaultMultiLevelImage defaultMultiLevelImage = (DefaultMultiLevelImage) sourceImage;
            if (defaultMultiLevelImage.getSource() instanceof SmosMultiLevelSource) {
                SmosMultiLevelSource smosMultiLevelSource = (SmosMultiLevelSource) defaultMultiLevelImage.getSource();
                GridPointValueProvider gridPointValueProvider = smosMultiLevelSource.getValueProvider();
                if (gridPointValueProvider instanceof L1cFieldValueProvider) {
                    L1cFieldValueProvider l1cFieldValueProvider = (L1cFieldValueProvider) gridPointValueProvider;
                    return l1cFieldValueProvider.getSnapshotId();
                }
            }
        }
        return -1;
    }

    private void locateSnapshotIdOfView() {
        SmosFile file = getSelectedSmosFile();
        if (file instanceof L1cScienceSmosFile) {
            final L1cScienceSmosFile l1cScienceSmosFile = (L1cScienceSmosFile) file;

            ProgressMonitorSwingWorker<Rectangle2D, Object> pmsw = new ProgressMonitorSwingWorker<Rectangle2D, Object>(
                    locateSnapshotButton, "Computing snapshot region") {
                @Override
                protected Rectangle2D doInBackground(ProgressMonitor pm) throws Exception {
                    return l1cScienceSmosFile.computeSnapshotRegion(getSelectedSnapshotId(), pm);
                }

                @Override
                protected void done() {
                    try {
                        Rectangle2D region = get();
                        if (region != null) {
                            getSelectedSmosView().getLayerCanvas().getViewport().zoom(region);
                        } else {
                            JOptionPane.showMessageDialog(locateSnapshotButton,
                                                          "No snapshot found with ID=" + getSelectedSnapshotId());
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(locateSnapshotButton, "Error:\n" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };

            pmsw.execute();
        }
    }

    private class SliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            updateTable(smosFile.getSnapshotIndex(snapshotSelectorCombo.getSnapshotId()));
            if (snapshotSelectorCombo.isAdjusting()) {
                return;
            }
            if (getSelectedSmosProduct() != null) {
                final long snapshotId = snapshotSelectorCombo.getSnapshotId();
                SmosBox.getInstance().getSnapshotSelectionService().setSelectedSnapshotId(getSelectedSmosProduct(),
                                                                                          snapshotId);
            }
        }
    }

    class ToggleSnapshotModeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (getSelectedSnapshotId() != -1) {
                if (snapshotModeButton.isSelected()) {
                    locateSnapshotIdOfView();
                    locateSnapshotButton.setEnabled(true);
                } else {
                    locateSnapshotButton.setEnabled(false);
                }
                setSnapshotIdOfView();
            }
        }
    }

    class LocateSnapshotAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (getSelectedSnapshotId() != -1) {
                locateSnapshotIdOfView();
            }
        }
    }

    class SSSL implements SnapshotSelectionService.SelectionListener {
        @Override
        public void handleSnapshotIdChanged(Product product, long oldId, long newId) {
            realizeSnapshotIdChange(product);
        }
    }

    private void startPolModeInitWaiting() {
        final JPanel centerPanel = new JPanel(new CenterLayout());
        centerPanel.setPreferredSize(new Dimension(300, 200));
        final JPanel panel = new JPanel(new BorderLayout());
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        final JLabel message = new JLabel();
        panel.add(message, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        centerPanel.add(panel);
        setToolViewComponent(centerPanel);
        final ProgressMonitor progressMonitor = new ProgressBarProgressMonitor(progressBar, message);
        final SwingWorker worker = new PolModeWaiter(progressMonitor);
        worker.execute();
    }


    private class PolModeWaiter extends SwingWorker {

        private final ProgressMonitor pm;

        private PolModeWaiter(ProgressMonitor progressMonitor) {
            pm = progressMonitor;
        }

        @Override
        protected Object doInBackground() throws InterruptedException {
            pm.beginTask("Finding Polarisation Modes of Snapshots...", 100);
            try {
                while (!smosFile.isBackgoundInitDone()) {
                    Thread.sleep(100);
                }
            } finally {
                pm.done();
            }
            return null;
        }


        @Override
        protected void done() {
            setToolViewComponent(getClientComponent());
            updateClientComponent(getSelectedSmosView());
        }
    }
}