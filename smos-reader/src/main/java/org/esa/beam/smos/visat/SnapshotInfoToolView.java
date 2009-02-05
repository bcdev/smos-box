package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.dataio.smos.GridPointValueProvider;
import org.esa.beam.dataio.smos.L1cFieldValueProvider;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosMultiLevelSource;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.jfree.layout.CenterLayout;


import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
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
import java.util.concurrent.ExecutionException;

public class SnapshotInfoToolView extends SmosToolView {

    public static final String ID = SnapshotInfoToolView.class.getName();

    private SnapshotSelectorCombo snapshotSelectorCombo;
    private JTable snapshotTable;
    private final SnapshotTableModel nullModel;
    private ButtonModel locateSnapshotButtonModel;
    private ButtonModel browseButtonModel;
    private ButtonModel snapshotButtonModel;
    private ButtonModel followModeButtonModel;
    private ButtonModel synchroniseButtonModel;
    private L1cScienceSmosFile smosFile;
    private SliderChangeListener snapshotSliderListener;
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

        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        snapshotSelectorCombo = new SnapshotSelectorCombo();
        JComponent comboComponent = SnapshotSelectorCombo.createComponent(snapshotSelectorCombo, false);
        mainPanel.add(comboComponent, BorderLayout.NORTH);

        final JPanel snapshotTablePanel = createSnapshotTablePanel();
        mainPanel.add(snapshotTablePanel, BorderLayout.CENTER);

        JPanel viewSettingsPanel = createViewSettingsPanel();
        mainPanel.add(viewSettingsPanel, BorderLayout.SOUTH);

        updateViewSettings();
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
            if (!smosFile.isBackgoundInitDone()) {
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
        updateViewSettings();
        updateTable(smosFile.getSnapshotIndex(snapshotSelectorCombo.getSnapshotId()));
    }


    private long getSelectedSnapshotId() {
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
                    long id = snapshotButtonModel.isSelected() ? getSelectedSnapshotId() : -1;
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
                    getPaneControl(), "Computing snapshot region") {
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
                            JOptionPane.showMessageDialog(getPaneControl(),
                                                          "No snapshot found with ID=" + getSelectedSnapshotId());
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(getPaneControl(), "Error:\n" + e.getMessage());
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

    private class ToggleSnapshotModeAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (getSelectedSnapshotId() != -1) {
                if (snapshotButtonModel.isSelected()) {
                    locateSnapshotIdOfView();
                }
                updateViewSettings();
                setSnapshotIdOfView();
            }
        }
    }

    private class LocateSnapshotAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (getSelectedSnapshotId() != -1) {
                locateSnapshotIdOfView();
            }
        }
    }

    private class SSSL implements SnapshotSelectionService.SelectionListener {

        @Override
        public void handleSnapshotIdChanged(Product product, long oldId, long newId) {
            realizeSnapshotIdChange(product);
        }
    }

    private JPanel createViewSettingsPanel() {
        JPanel viewSettingsPanel = new JPanel(new BorderLayout(4, 4));
        viewSettingsPanel.setBorder(BorderFactory.createTitledBorder("View Settings"));

        final JPanel dataSourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));

        JCheckBox synchroniseCheckBox = new JCheckBox("Synchronise with L1C view", false);
        synchroniseButtonModel =  synchroniseCheckBox.getModel();
        synchroniseButtonModel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateViewSettings();
                Product selectedSmosProduct = getSelectedSmosProduct();
                if (selectedSmosProduct != null) {
                    final SnapshotSelectionService service = SmosBox.getInstance().getSnapshotSelectionService();
                    service.setSelectedSnapshotId(selectedSmosProduct, snapshotSelectorCombo.getSnapshotId());
                }
            }
        });
        viewSettingsPanel.add(synchroniseCheckBox, BorderLayout.NORTH);


        final Border outerBorder = BorderFactory.createEmptyBorder(0, 11, 0, 0);
        final Border titledBorder = BorderFactory.createTitledBorder("Data Source");
        dataSourcePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, titledBorder));

        final ButtonGroup buttonGroup = new ButtonGroup();
        final JRadioButton browseButton = new JRadioButton("Browse", true);
        final JRadioButton snapshotButton = new JRadioButton("Snapshot", false);
        buttonGroup.add(browseButton);
        buttonGroup.add(snapshotButton);
        browseButtonModel = browseButton.getModel();
        snapshotButtonModel = snapshotButton.getModel();
        final ToggleSnapshotModeAction toggleSnapshotModeAction = new ToggleSnapshotModeAction();
        snapshotButtonModel.addActionListener(toggleSnapshotModeAction);
        browseButtonModel.addActionListener(toggleSnapshotModeAction);
        final JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonGroupPanel.add(browseButton);
        buttonGroupPanel.add(snapshotButton);
        dataSourcePanel.add(buttonGroupPanel);

        JCheckBox followModeCheckBox = new JCheckBox("Follow");
        followModeButtonModel = followModeCheckBox.getModel();
        final JPanel locateOptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locateOptionPanel.add(followModeCheckBox);

        JButton locateSnapshotButton = new JButton("Locate in view");
        locateSnapshotButton.addActionListener(new LocateSnapshotAction());
        locateSnapshotButton.setToolTipText("Locate selected snapshot in view");
        locateSnapshotButtonModel = locateSnapshotButton.getModel();
        locateOptionPanel.add(locateSnapshotButton);
        dataSourcePanel.add(locateOptionPanel);
        viewSettingsPanel.add(dataSourcePanel);
        return viewSettingsPanel;
    }

    private void updateViewSettings() {
        final boolean selected = synchroniseButtonModel.isSelected();
        browseButtonModel.setEnabled(selected);
        snapshotButtonModel.setEnabled(selected);

        followModeButtonModel.setEnabled(selected && snapshotButtonModel.isSelected());
        locateSnapshotButtonModel.setEnabled(selected && snapshotButtonModel.isSelected());
    }

    private JPanel createSnapshotTablePanel() {
        snapshotTable = new JTable(nullModel);
        snapshotTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    setHorizontalAlignment(RIGHT);
                }
                return this;
            }
        });

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
        final JPanel snapshotTablePanel = new JPanel(new BorderLayout());
        snapshotTablePanel.setBorder(BorderFactory.createTitledBorder("Snapshot Information"));
        snapshotTablePanel.add(new JScrollPane(snapshotTable), BorderLayout.CENTER);
        final JPanel tableButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        tableButtonPanel.add(exportButton);
        snapshotTablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        return snapshotTablePanel;
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