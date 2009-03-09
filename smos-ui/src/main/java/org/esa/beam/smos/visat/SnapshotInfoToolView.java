package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.grender.Viewport;
import org.esa.beam.dataio.smos.GridPointValueProvider;
import org.esa.beam.dataio.smos.L1cFieldValueProvider;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosMultiLevelSource;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.jfree.layout.CenterLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.FlowLayout;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class SnapshotInfoToolView extends SmosToolView {
    public static final String ID = SnapshotInfoToolView.class.getName();
    private static final SnapshotTableModel NULL_MODEL = new SnapshotTableModel(new Object[0][0]);

    private SnapshotSelectorCombo snapshotSelectorCombo;
    private JTable snapshotTable;
    private ButtonModel locateSnapshotButtonModel;
    private ButtonModel browseButtonModel;
    private ButtonModel snapshotButtonModel;
    private ButtonModel followModeButtonModel;
    private ButtonModel synchronizeButtonModel;
    private SliderChangeListener snapshotSliderListener;

    @Override
    protected JComponent createClientComponent() {
        snapshotSliderListener = new SliderChangeListener();

        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        snapshotSelectorCombo = new SnapshotSelectorCombo();
        snapshotSelectorCombo.getSlider().addChangeListener(snapshotSliderListener);
        snapshotSelectorCombo.getComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable(snapshotSelectorCombo.getSnapshotId());
            }
        });
        JComponent comboComponent = SnapshotSelectorCombo.createComponent(snapshotSelectorCombo, false);
        mainPanel.add(comboComponent, BorderLayout.NORTH);

        final JPanel snapshotTablePanel = createSnapshotTablePanel();
        mainPanel.add(snapshotTablePanel, BorderLayout.CENTER);

        JPanel viewSettingsPanel = createViewSettingsPanel();
        mainPanel.add(viewSettingsPanel, BorderLayout.SOUTH);

//        final AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Help24.gif"), false);
//        helpButton.setToolTipText("Help."); /*I18N*/
//        helpButton.setName("helpButton");

        if (getDescriptor().getHelpId() != null) {
//            HelpSys.enableHelpOnButton(helpButton, getDescriptor().getHelpId());
            HelpSys.enableHelpKey(mainPanel, getDescriptor().getHelpId());
        }

        return mainPanel;
    }

    @Override
    protected void updateClientComponent(ProductSceneView smosView) {
        final L1cScienceSmosFile smosFile = SmosBox.getL1cScienceSmosFile(smosView);
        if (smosFile != null) {
            if (!smosFile.isBackgroundInitStarted()) {
                smosFile.startBackgroundInit();
            }
            if (!smosFile.isBackgoundInitDone()) {
                startPolModeInitWaiting(smosView, smosFile);
                return;
            }
            updateUI(smosView, true);
        } else {
            super.realizeSmosView(null);
        }
    }

    private void updateTable(long snapshotId) {
        final SmosFile selectedSmosFile = getSelectedSmosFile();
        if (selectedSmosFile != null && selectedSmosFile instanceof L1cScienceSmosFile) {
            L1cScienceSmosFile l1cScienceSmosFile = (L1cScienceSmosFile) selectedSmosFile;
            final int snapshotIndex = l1cScienceSmosFile.getSnapshotIndex(snapshotId);
            if (snapshotIndex != -1) {
                try {
                    final CompoundData data = l1cScienceSmosFile.getSnapshotData(snapshotIndex);
                    snapshotTable.setModel(createSnapshotTableModel(data));
                    return;
                } catch (IOException e) {
                    snapshotTable.setModel(NULL_MODEL);
                    return;
                }
            }
        }
        snapshotTable.setModel(NULL_MODEL);
    }
    
    private TableModel createSnapshotTableModel(CompoundData data) {
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

        return new SnapshotTableModel(list.toArray(new Object[2][list.size()]));
    }

    private void updateImageLayer(ProductSceneView smosView) {
        ImageLayer imageLayer = smosView.getBaseImageLayer();
        RenderedImage sourceImage = smosView.getRaster().getSourceImage();
        if (sourceImage instanceof DefaultMultiLevelImage) {
            DefaultMultiLevelImage defaultMultiLevelImage = (DefaultMultiLevelImage) sourceImage;
            if (defaultMultiLevelImage.getSource() instanceof SmosMultiLevelSource) {
                SmosMultiLevelSource smosMultiLevelSource = (SmosMultiLevelSource) defaultMultiLevelImage.getSource();
                GridPointValueProvider gridPointValueProvider = smosMultiLevelSource.getValueProvider();
                if (gridPointValueProvider instanceof L1cFieldValueProvider) {
                    L1cFieldValueProvider l1cFieldValueProvider = (L1cFieldValueProvider) gridPointValueProvider;
                    long id = snapshotButtonModel.isSelected() ? snapshotSelectorCombo.getSnapshotId() : -1;
                    if (l1cFieldValueProvider.getSnapshotId() != id) {
                        l1cFieldValueProvider.setSnapshotId(id);
                        smosMultiLevelSource.reset();
                        smosView.getRaster().setValidMaskImage(null);
                        smosView.getRaster().setGeophysicalImage(null);
                        imageLayer.regenerate();
                    }
                }
            }
        }
    }

    private void locateSnapshotId(final ProductSceneView smosView, final long id) {
        final L1cScienceSmosFile smosFile = SmosBox.getL1cScienceSmosFile(smosView);
        if (smosFile != null) {
            Rectangle2D snapshotRegion = smosFile.getSnapshotRegion(id);
            if (snapshotRegion != null) {
                final Viewport vp = smosView.getLayerCanvas().getViewport();
                final AffineTransform m2v = vp.getModelToViewTransform();
                final Point2D.Double center = new Point2D.Double(snapshotRegion.getCenterX(), snapshotRegion.getCenterY());
                m2v.transform(center, center);
                final Rectangle viewBounds = vp.getViewBounds();
                final double vx = viewBounds.getCenterX();
                final double vy = viewBounds.getCenterY();
                vp.moveViewDelta(vx - center.getX(), vy - center.getY());
            } else {
                JOptionPane.showMessageDialog(getPaneControl(),
                                              MessageFormat.format("No snapshot found with ID = {0}", id));
            }
            
            
        }
    }

    private class SliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            final long id = snapshotSelectorCombo.getSnapshotId();
            updateTable(id);
            if (snapshotSelectorCombo.isAdjusting()) {
                return;
            }
            if (synchronizeButtonModel.isSelected() && snapshotButtonModel.isSelected()) {
                final ProductSceneView smosView = getSelectedSmosView();
                if (smosView != null) {
                    setSelectedSnapshotId(smosView, id);
                    if (followModeButtonModel.isSelected()) {
                        locateSnapshotId(smosView, id);
                    }
                    updateImageLayer(smosView);
                }
            }
        }
    }

    private class ToggleSnapshotModeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final ProductSceneView smosView = getSelectedSmosView();
            if (snapshotButtonModel.isSelected()) {
                final long id = snapshotSelectorCombo.getSnapshotId();
                setSelectedSnapshotId(smosView, id);
                if (followModeButtonModel.isSelected()) {
                    locateSnapshotId(smosView, id);
                }
            } else {
                setSelectedSnapshotId(smosView, -1);
            }
            updateUI(smosView, false);
            updateImageLayer(smosView);
        }
    }

    private class LocateSnapshotAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final ProductSceneView smosView = getSelectedSmosView();
            locateSnapshotId(smosView, getSelectedSnapshotId(smosView));
        }
    }

    private JPanel createViewSettingsPanel() {
        JCheckBox synchroniseCheckBox = new JCheckBox("Synchronise with view", false);
        synchronizeButtonModel = synchroniseCheckBox.getModel();
        synchronizeButtonModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUI(getSelectedSmosView(), false);
            }
        });


        final ToggleSnapshotModeAction toggleSnapshotModeAction = new ToggleSnapshotModeAction();

        final JRadioButton browseButton = new JRadioButton("Browse", true);
        browseButtonModel = browseButton.getModel();
        browseButtonModel.addActionListener(toggleSnapshotModeAction);

        final JRadioButton snapshotButton = new JRadioButton("Snapshot", false);
        snapshotButtonModel = snapshotButton.getModel();
        snapshotButtonModel.addActionListener(toggleSnapshotModeAction);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(browseButton);
        buttonGroup.add(snapshotButton);

        final JCheckBox followModeCheckBox = new JCheckBox("Follow", true);
        followModeButtonModel = followModeCheckBox.getModel();

        final JButton locateSnapshotButton = new JButton("Locate in view");
        locateSnapshotButton.addActionListener(new LocateSnapshotAction());
        locateSnapshotButton.setToolTipText("Locate selected snapshot in view");
        locateSnapshotButtonModel = locateSnapshotButton.getModel();

//        code using tabular layout - replaced by flow layout (rq-20090209)
//        final TableLayout viewSettingsLayout = new TableLayout(4);
//        viewSettingsLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
//        viewSettingsLayout.setColumnWeightX(3, 1.0);  // spacer column
//        JPanel viewSettingsPanel = new JPanel(viewSettingsLayout);
//        viewSettingsPanel.add(synchroniseCheckBox, new TableLayout.Cell(0, 0));
//        viewSettingsPanel.add(browseButton, new TableLayout.Cell(0, 1));
//        viewSettingsPanel.add(followModeCheckBox, new TableLayout.Cell(0, 2));
//        viewSettingsPanel.add(snapshotButton, new TableLayout.Cell(1, 1));
//        viewSettingsPanel.add(locateSnapshotButton, new TableLayout.Cell(1, 2));
//        viewSettingsPanel.add(new JPanel(), new TableLayout.Cell(0, 3));    // spacer column

        JPanel viewSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        viewSettingsPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        viewSettingsPanel.add(synchroniseCheckBox);
        viewSettingsPanel.add(browseButton);
        viewSettingsPanel.add(snapshotButton);
        viewSettingsPanel.add(followModeCheckBox);
        viewSettingsPanel.add(locateSnapshotButton);

        return viewSettingsPanel;
    }

    private void updateUI(ProductSceneView smosView, boolean resetSelectorComboModel) {
        if (resetSelectorComboModel) {
            final L1cScienceSmosFile smosFile = SmosBox.getL1cScienceSmosFile(smosView);
            snapshotSelectorCombo.setModel(new SnapshotSelectorComboModel(smosFile));
        }
        final boolean sync = synchronizeButtonModel.isSelected();
        if (sync) {
            final long id = getSelectedSnapshotId(smosView);
            if (id != -1) {
                snapshotSelectorCombo.setSnapshotId(id);
                snapshotButtonModel.setSelected(true);
            } else {
                browseButtonModel.setSelected(true);
            }
            final String bandName = smosView.getRaster().getName();
            if (bandName.endsWith("_X")) {
                snapshotSelectorCombo.getComboBox().setSelectedIndex(1);
            } else if (bandName.endsWith("_Y")) {
                snapshotSelectorCombo.getComboBox().setSelectedIndex(2);
            } else if (bandName.contains("_XY")) {
                snapshotSelectorCombo.getComboBox().setSelectedIndex(3);
            }
        }

        updateTable(snapshotSelectorCombo.getSnapshotId());
        snapshotSelectorCombo.getComboBox().setEnabled(!sync);
        browseButtonModel.setEnabled(sync);
        snapshotButtonModel.setEnabled(sync);
        followModeButtonModel.setEnabled(sync && snapshotButtonModel.isSelected());
        locateSnapshotButtonModel.setEnabled(sync && snapshotButtonModel.isSelected());
    }

    private JPanel createSnapshotTablePanel() {
        snapshotTable = new JTable(NULL_MODEL);
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

        final JPopupMenu tablePopup = new JPopupMenu();
        final JMenuItem exportItem = new JMenuItem("Export...");
        exportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TableModelExportRunner(getPaneWindow(),
                                           getTitle(),
                                           snapshotTable.getModel(),
                                           snapshotTable.getColumnModel()).run();
            }
        });
        tablePopup.add(exportItem);
        snapshotTable.add(tablePopup);

        MouseListener popupListener = new PopupListener(tablePopup);
        snapshotTable.addMouseListener(popupListener);

        final JPanel snapshotTablePanel = new JPanel(new BorderLayout());
        final JLabel tableLabel = new JLabel("Snapshot Information:");
        tableLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        snapshotTablePanel.add(tableLabel, BorderLayout.NORTH);
        snapshotTablePanel.add(new JScrollPane(snapshotTable), BorderLayout.CENTER);
        return snapshotTablePanel;
    }

    private void startPolModeInitWaiting(ProductSceneView smosView, L1cScienceSmosFile smosFile) {
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
        final SwingWorker worker = new PolModeWaiter(smosView, smosFile, progressMonitor);
        worker.execute();
    }


    private class PolModeWaiter extends SwingWorker {
        private final ProductSceneView smosView;
        private final L1cScienceSmosFile smosFile;
        private final ProgressMonitor pm;

        private PolModeWaiter(ProductSceneView smosView, L1cScienceSmosFile smosFile, ProgressMonitor pm) {
            this.smosView = smosView;
            this.smosFile = smosFile;
            this.pm = pm;
        }

        @Override
        protected Object doInBackground() throws InterruptedException {
            pm.beginTask("Indexing snapshot polarisation modes...", 100);
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
            updateClientComponent(smosView);
        }
    }

    private static class PopupListener extends MouseAdapter {
        private final JPopupMenu popup;

        PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}