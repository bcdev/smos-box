package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.GridPointValueProvider;
import org.esa.beam.dataio.smos.L1cFieldValueProvider;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosMultiLevelSource;
import org.esa.beam.dataio.smos.SnapshotProvider;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.datamodel.VirtualBand;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.esa.beam.glevel.TiledFileMultiLevelSource;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.smos.visat.swing.SnapshotSelectorCombo;
import org.esa.beam.smos.visat.swing.SnapshotSelectorComboModel;
import org.esa.beam.visat.VisatApp;
import org.jfree.layout.CenterLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glayer.swing.LayerCanvas;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.glevel.support.FileMultiLevelSource;
import com.bc.ceres.grender.Viewport;

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

    @SuppressWarnings({"FieldCanBeLocal"})
    private SnapshotIdListener snapshotIdListener;

    @Override
    protected JComponent createClientComponent() {
        snapshotIdListener = new SnapshotIdListener();

        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        snapshotSelectorCombo = new SnapshotSelectorCombo();
        snapshotSelectorCombo.addSliderChangeListener(snapshotIdListener);
        snapshotSelectorCombo.addComboBoxActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable(snapshotSelectorCombo.getSnapshotId());
            }
        });

        JComponent comboComponent = SnapshotSelectorCombo.createComponent(snapshotSelectorCombo, false);
        mainPanel.add(comboComponent, BorderLayout.NORTH);

        final JPanel snapshotTablePanel = createSnapshotTablePanel();
        mainPanel.add(snapshotTablePanel, BorderLayout.CENTER);

        final JPanel viewSettingsPanel = createViewSettingsPanel();

        final JPanel southPanel = new JPanel(new BorderLayout(6, 0));
        southPanel.add(viewSettingsPanel, BorderLayout.WEST);
        final AbstractButton helpButton = createHelpButton();
        southPanel.add(helpButton, BorderLayout.EAST);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        if (getDescriptor().getHelpId() != null) {
            HelpSys.enableHelpOnButton(helpButton, getDescriptor().getHelpId());
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
            final long snapshotId = getSelectedSnapshotId(smosView.getRaster());
            updateUI(smosView, snapshotId, true);
        } else {
            super.realizeSmosView(null);
        }
    }

    private void updateTable(long snapshotId) {
        final SmosFile selectedSmosFile = getSelectedSmosFile();
        if (selectedSmosFile != null && selectedSmosFile instanceof L1cScienceSmosFile) {
            final L1cScienceSmosFile l1cScienceSmosFile = (L1cScienceSmosFile) selectedSmosFile;
            final int snapshotIndex = l1cScienceSmosFile.getSnapshotIndex(snapshotId);
            if (snapshotIndex != -1) {
                final SwingWorker<TableModel, Object> worker = new SwingWorker<TableModel, Object>() {
                    @Override
                    protected TableModel doInBackground() throws Exception {
                        final CompoundData data = l1cScienceSmosFile.getSnapshotData(snapshotIndex);
                        return createSnapshotTableModel(data);
                    }

                    @Override
                    protected void done() {
                        try {
                            final TableModel tableModel = get();
                            snapshotTable.setModel(tableModel);
                        } catch (InterruptedException e) {
                            snapshotTable.setModel(NULL_MODEL);
                        } catch (ExecutionException e) {
                            snapshotTable.setModel(NULL_MODEL);
                        }
                    }
                };
                worker.execute();
                return;
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

    private void updateSmosImage(RasterDataNode raster, long snapshotId) {
        final MultiLevelImage sourceImage = raster.getSourceImage();
        if (sourceImage instanceof DefaultMultiLevelImage) {
            final DefaultMultiLevelImage multiLevelImage = (DefaultMultiLevelImage) sourceImage;
            if (multiLevelImage.getSource() instanceof SmosMultiLevelSource) {
                final SmosMultiLevelSource smosMultiLevelSource = (SmosMultiLevelSource) multiLevelImage.getSource();
                final GridPointValueProvider gridPointValueProvider = smosMultiLevelSource.getValueProvider();
                if (gridPointValueProvider instanceof L1cFieldValueProvider) {
                    final L1cFieldValueProvider l1cFieldValueProvider = (L1cFieldValueProvider) gridPointValueProvider;
                    if (l1cFieldValueProvider.getSnapshotId() != snapshotId) {
                        l1cFieldValueProvider.setSnapshotId(snapshotId);
                        resetRasterImages(raster);
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
                final Point2D.Double center = new Point2D.Double(snapshotRegion.getCenterX(),
                                                                 snapshotRegion.getCenterY());
                m2v.transform(center, center);
                final Rectangle viewBounds = vp.getViewBounds();
                final double vx = viewBounds.getCenterX();
                final double vy = viewBounds.getCenterY();
                vp.moveViewDelta(vx - center.getX(), vy - center.getY());
            } else {
                JOptionPane.showMessageDialog(getPaneControl(), MessageFormat.format(
                        "No snapshot found with ID = {0}", id));
            }
        }
    }

    private class SnapshotIdListener implements ChangeListener {
        
        private SnapshotRegionOverlay overlay = new SnapshotRegionOverlay();
        private boolean overlayAdded;

        @Override
        public void stateChanged(ChangeEvent e) {
            final long snapshotId = snapshotSelectorCombo.getSnapshotId();
            updateTable(snapshotId);
            if (snapshotSelectorCombo.isAdjusting()) {
                if (snapshotButtonModel.isSelected()) {
                    overlay.setId(snapshotId);
                    if (!overlayAdded) {
                        overlayAdded = true;
                        getSelectedSmosView().getLayerCanvas().addOverlay(overlay);
                    } else {
                        getSelectedSmosView().getLayerCanvas().repaint();
                    }
                }
                return;
            }
            getSelectedSmosView().getLayerCanvas().removeOverlay(overlay);
            overlayAdded = false;
            if (synchronizeButtonModel.isSelected() && snapshotButtonModel.isSelected()) {
                final ProductSceneView smosView = getSelectedSmosView();
                if (smosView != null) {
                    if (followModeButtonModel.isSelected()) {
                        locateSnapshotId(smosView, snapshotId);
                    }
                    updateAllImagesAndViews(smosView.getProduct(), snapshotId);
                }
            }
        }
    }
    
    private class SnapshotRegionOverlay implements LayerCanvas.Overlay {
        private long snapshotId;

        @Override
        public void paintOverlay(LayerCanvas canvas, Graphics2D graphics) {
            ProductSceneView view = getSelectedSmosView();
            L1cScienceSmosFile scienceSmosFile = SmosBox.getL1cScienceSmosFile(view);
            if (scienceSmosFile != null) {
                Rectangle2D snapshotRegion = scienceSmosFile.getSnapshotRegion(snapshotId);
                if (snapshotRegion != null) {
                    final Viewport vp = view.getLayerCanvas().getViewport();
                    final AffineTransform m2v = vp.getModelToViewTransform();
                    Shape transformedShape = m2v.createTransformedShape(snapshotRegion);
                    Color color = graphics.getColor();
                    graphics.setColor(Color.WHITE);
                    graphics.draw(transformedShape);
                    graphics.setColor(color);
                }
            }
        }

        void setId(long snapshotId) {
            this.snapshotId = snapshotId;
        }
    }

    private class ToggleSnapshotModeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final ProductSceneView smosView = getSelectedSmosView();
            final long snapshotId;

            if (snapshotButtonModel.isSelected()) {
                snapshotId = snapshotSelectorCombo.getSnapshotId();
                if (followModeButtonModel.isSelected()) {
                    locateSnapshotId(smosView, snapshotId);
                }
            } else {
                snapshotId = -1;
            }
            updateAllImagesAndViews(smosView.getProduct(), snapshotId);
            updateUI(smosView, snapshotId, false);
        }
    }

    private void updateAllImagesAndViews(Product smosProduct, long snapshotId) {
        final long xPolId;
        final long yPolId;
        final long crossPolId;

        if (snapshotId != -1) {
            final SnapshotProvider snapshotProvider = (SnapshotProvider) getSelectedSmosFile();
            xPolId = findSnapshotId(snapshotProvider.getXPolSnapshotIds(), snapshotId);
            yPolId = findSnapshotId(snapshotProvider.getYPolSnapshotIds(), snapshotId);
            crossPolId = findSnapshotId(snapshotProvider.getCrossPolSnapshotIds(), snapshotId);
        } else {
            xPolId = -1;
            yPolId = -1;
            crossPolId = -1;
        }

        for (final Band band : smosProduct.getBands()) {
            if (band instanceof VirtualBand) {
                resetRasterImages(band);
            } else if (band.getName().endsWith("_X")) {
                updateSmosImage(band, xPolId);
            } else if (band.getName().endsWith("_Y")) {
                updateSmosImage(band, yPolId);
            } else if (band.getName().contains("_XY")) {
                updateSmosImage(band, crossPolId);
            } else if (band.isFlagBand()) {
                updateSmosImage(band, snapshotId);
            } else {
                resetRasterImages(band);
            }
        }

        // a workaround for the problem that all displayed mask images are cached (rq-20090612)
        MaskImageCacheAccessor.removeAll(ImageManager.getInstance(), smosProduct);

        for (final Band band : smosProduct.getBands()) {
            if (band instanceof VirtualBand) {
                resetViews(band, snapshotId);
            } else if (band.getName().endsWith("_X")) {
                resetViews(band, xPolId);
            } else if (band.getName().endsWith("_Y")) {
                resetViews(band, yPolId);
            } else if (band.getName().contains("_XY")) {
                resetViews(band, crossPolId);
            } else {
                resetViews(band, snapshotId);
            }
        }
        for (final Band band : smosProduct.getBands()) {
            if (band instanceof VirtualBand) {
                setSelectedSnapshotId(band, snapshotId);
            } else if (band.getName().endsWith("_X")) {
                setSelectedSnapshotId(band, xPolId);
            } else if (band.getName().endsWith("_Y")) {
                setSelectedSnapshotId(band, yPolId);
            } else if (band.getName().contains("_XY")) {
                setSelectedSnapshotId(band, crossPolId);
            } else {
                setSelectedSnapshotId(band, snapshotId);
            }
        }
    }

    private void resetViews(RasterDataNode raster, long snapshotId) {
        for (final JInternalFrame internalFrame : VisatApp.getApp().findInternalFrames(raster)) {
            if (internalFrame != null) {
                if (internalFrame.getContentPane() instanceof ProductSceneView) {
                    final ProductSceneView view = (ProductSceneView) internalFrame.getContentPane();
                    if (getSelectedSnapshotId(view.getRaster()) != snapshotId) {
                        regenerateImageLayers(view.getRootLayer());
                    }
                }
            }
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
        final JCheckBox synchroniseCheckBox = new JCheckBox("Synchronise with view", false);
        synchronizeButtonModel = synchroniseCheckBox.getModel();
        synchronizeButtonModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ProductSceneView smosView = getSelectedSmosView();
                final long snapshotId = getSelectedSnapshotId(smosView);
                updateUI(smosView, snapshotId, false);
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

        final JPanel viewSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        viewSettingsPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        viewSettingsPanel.add(synchroniseCheckBox);
        viewSettingsPanel.add(browseButton);
        viewSettingsPanel.add(snapshotButton);
        viewSettingsPanel.add(followModeCheckBox);
        viewSettingsPanel.add(locateSnapshotButton);

        return viewSettingsPanel;
    }

    private void updateUI(ProductSceneView smosView, long snapshotId, boolean resetSelectorComboModel) {
        if (resetSelectorComboModel) {
            final L1cScienceSmosFile smosFile = SmosBox.getL1cScienceSmosFile(smosView);
            snapshotSelectorCombo.setModel(new SnapshotSelectorComboModel(smosFile));
        }
        final boolean sync = synchronizeButtonModel.isSelected();
        if (sync) {
            final String bandName = smosView.getRaster().getName();
            if (bandName.endsWith("_X")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(1);
            } else if (bandName.endsWith("_Y")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(2);
            } else if (bandName.contains("_XY")) {
                snapshotSelectorCombo.setComboBoxSelectedIndex(3);
            } else {
                snapshotSelectorCombo.setComboBoxSelectedIndex(0);
            }
            if (snapshotId != -1) {
                snapshotSelectorCombo.setSnapshotId(snapshotId);
                snapshotButtonModel.setSelected(true);
            } else {
                browseButtonModel.setSelected(true);
            }
        }

        updateTable(snapshotSelectorCombo.getSnapshotId());

        snapshotSelectorCombo.setComboBoxEnabled(!sync);
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

    private static long findSnapshotId(Long[] snapshotIds, long requestedId) {
        if (snapshotIds.length == 0) {
            return -1;
        }
        final int foundIndex = Arrays.binarySearch(snapshotIds, requestedId);
        if (foundIndex >= 0) {
            return snapshotIds[foundIndex];
        }
        final int lowerIndex = -(foundIndex + 1) - 1;
        final int upperIndex = -(foundIndex + 1);

        final long lowerId;
        if (lowerIndex < 0) {
            lowerId = snapshotIds[0];
        } else {
            lowerId = snapshotIds[lowerIndex];
        }
        final long upperId;
        if (upperIndex < snapshotIds.length) {
            upperId = snapshotIds[upperIndex];
        } else {
            upperId = snapshotIds[snapshotIds.length - 1];
        }
        final long foundId;
        if (requestedId - lowerId < upperId - requestedId) {
            foundId = lowerId;
        } else {
            foundId = upperId;
        }

        return foundId;
    }

    private static void regenerateImageLayers(Layer layer) {
        final List<Layer> children = layer.getChildren();
        for (int i = children.size(); i-- > 0;) {
            final Layer child = children.get(i);
            if (child instanceof ImageLayer) {
                final ImageLayer imageLayer = (ImageLayer) child;

                // skip static image layers
                if (imageLayer.getMultiLevelSource() instanceof FileMultiLevelSource) {
                    continue;
                }
                if (imageLayer.getMultiLevelSource() instanceof TiledFileMultiLevelSource) {
                    continue;
                }
                imageLayer.regenerate();
            } else {
                if (child instanceof CollectionLayer) {
                    regenerateImageLayers(child);
                }
            }
        }
    }

    private static void resetRasterImages(final RasterDataNode raster) {
        raster.getSourceImage().reset();
        if (raster.isValidMaskImageSet()) {
            raster.getValidMaskImage().reset();
        }
        if (raster.isGeophysicalImageSet()) {
            raster.getGeophysicalImage().reset();
        }
        raster.setStx(null);
        raster.getStx();
    }

}