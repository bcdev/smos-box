package org.esa.beam.smos.visat.export;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.PropertyEditor;
import com.bc.ceres.swing.binding.PropertyEditorRegistry;
import com.bc.ceres.swing.binding.internal.FileEditor;
import com.bc.ceres.swing.binding.internal.SingleSelectionEditor;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import com.bc.ceres.swing.binding.internal.TextFieldEditor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.PlacemarkGroup;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.util.SystemUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.LiteShape2;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GridPointExportDialog extends ModalDialog {

    private static final String LAST_DIR_KEY = "user.smos.import.dir";

    private static final String ALIAS_GEOMETRY = "geometry";
    private static final String ALIAS_ROI_TYPE = "roiType";
    private static final String ALIAS_RECURSIVE = "recursive";
    private static final String ALIAS_SOURCE_DIRECTORY = "sourceDirectory";
    private static final String ALIAS_TARGET_FILE = "targetFile";
    private static final String ALIAS_USE_SELECTED_PRODUCT = "useSelectedProduct";
    private static final String ALIAS_NORTH = "north";
    private static final String ALIAS_SOUTH = "south";
    private static final String ALIAS_EAST = "east";
    private static final String ALIAS_WEST = "west";

    private final AppContext appContext;
    private final ParameterBlock parameterBlock;
    private final PropertyContainer propertyContainer;

    private final BindingContext bindingContext;

    GridPointExportDialog(final AppContext appContext, String helpId) {
        super(appContext.getApplicationWindow(), "Export SMOS Grid Points", ID_OK_CANCEL_HELP, helpId); /* I18N */
        this.appContext = appContext;
        parameterBlock = new ParameterBlock();

        final String smosDirPath =
                appContext.getPreferences().getPropertyString(LAST_DIR_KEY, SystemUtils.getUserHomeDir().getPath());
        parameterBlock.sourceDirectory = new File(smosDirPath);
        parameterBlock.targetFile = new File(SystemUtils.getUserHomeDir(), "grid_point_export.csv");

        propertyContainer = PropertyContainer.createObjectBacked(parameterBlock, new ParameterDescriptorFactory());
        try {
            initPropertyContainer();
        } catch (ValidationException e) {
            throw new IllegalStateException(e); // cannot happen
        }

        bindingContext = new BindingContext(propertyContainer);
        bindingContext.bindEnabledState(ALIAS_GEOMETRY, true, ALIAS_ROI_TYPE, 0);
        bindingContext.bindEnabledState(ALIAS_NORTH, true, ALIAS_ROI_TYPE, 2);
        bindingContext.bindEnabledState(ALIAS_SOUTH, true, ALIAS_ROI_TYPE, 2);
        bindingContext.bindEnabledState(ALIAS_EAST, true, ALIAS_ROI_TYPE, 2);
        bindingContext.bindEnabledState(ALIAS_WEST, true, ALIAS_ROI_TYPE, 2);

        createUI();
    }

    @Override
    protected void onOK() {
        if (parameterBlock.targetFile.exists()) {
            final String message = MessageFormat.format(
                    "The selected target file\n''{0}''\n already exists.\n\n Do you want to overwrite the existing file?",
                    parameterBlock.targetFile.getPath());
            final int answer = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(),
                                                             JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        final PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(parameterBlock.targetFile);
        } catch (FileNotFoundException e) {
            appContext.handleError(MessageFormat.format("Cannot create target file: {0}", e.getMessage()), e);
            return;
        }
        final CsvExportStream csvExportStream = new CsvExportStream(printWriter, ";");
        final SwingWorker<Void, Void> swingWorker = new ProgressMonitorSwingWorker<Void, Void>(getJDialog(),
                                                                                               "Exporting grid points...") {
            @Override
            protected Void doInBackground(ProgressMonitor pm) throws Exception {
                final Area area = getArea();
                final GridPointFilterStreamHandler streamHandler =
                        new GridPointFilterStreamHandler(csvExportStream, area);
                try {
                    if (parameterBlock.useSelectedProduct) {
                        streamHandler.processProduct(appContext.getSelectedProduct(), pm);
                    } else {
                        streamHandler.processDirectory(parameterBlock.sourceDirectory, parameterBlock.recursive, pm);
                    }
                } catch (IOException e) {
                    appContext.handleError(MessageFormat.format(
                            "An I/O error occurred: {0}", e.getMessage()), e);
                    return null;
                } finally {
                    try {
                        csvExportStream.close();
                    } catch (IOException e) {
                        // ignore;
                    }
                }
                return null;
            }
        };
        super.onOK();
        swingWorker.execute();
    }

    @Override
    protected boolean verifyUserInput() {
        if (parameterBlock.roiType == 2) {
            if (parameterBlock.north <= parameterBlock.south) {
                showErrorDialog("The southern latitude must be less than the northern latitude.");
                return false;
            }
            if (parameterBlock.east <= parameterBlock.west) {
                showErrorDialog("The western longitude must be less than the eastern longitude.");
                return false;
            }
        }
        return true;
    }

    private void initPropertyContainer() throws ValidationException {
        propertyContainer.setDefaultValues();
        final Product selectedProduct = getSelectedSmosProduct();
        if (selectedProduct != null) {
            final List<VectorDataNode> geometryNodeList = new ArrayList<VectorDataNode>();
            final ProductNodeGroup<VectorDataNode> vectorDataGroup = selectedProduct.getVectorDataGroup();
            for (VectorDataNode node : vectorDataGroup.toArray(new VectorDataNode[vectorDataGroup.getNodeCount()])) {
                if (node.getFeatureType() != Placemark.getFeatureType()) {
                    if (!node.getFeatureCollection().isEmpty()) {
                        geometryNodeList.add(node);
                    }
                }
            }
            if (selectedProduct.getPinGroup().getNodeCount() != 0) {
                propertyContainer.setValue(ALIAS_ROI_TYPE, 1);
            }
            if (!geometryNodeList.isEmpty()) {
                final PropertyDescriptor descriptor = propertyContainer.getDescriptor(ALIAS_GEOMETRY);
                descriptor.setNotNull(true);
                descriptor.setNotEmpty(true);
                descriptor.setValueSet(new ValueSet(geometryNodeList.toArray()));

                propertyContainer.setValue(ALIAS_ROI_TYPE, 0);
                propertyContainer.getProperty(ALIAS_GEOMETRY).setValue(geometryNodeList.get(0));
            }
        }
        propertyContainer.setValue(ALIAS_USE_SELECTED_PRODUCT, selectedProduct != null);
    }

    private void createUI() {
        final JPanel mainPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(layout);
        mainPanel.add(createSourceProductPanel());
        mainPanel.add(createRoiPanel());
        mainPanel.add(createTargetFilePanel());

        setContent(mainPanel);
    }

    private JComponent createSourceProductPanel() {
        final TableLayout layout = new TableLayout(1);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        layout.setTableWeightX(1.0);

        final JRadioButton useSelectedProductButton = new JRadioButton("Use selected SMOS product");
        final JRadioButton useAllProductsInDirectoryButton = new JRadioButton("Use all SMOS products in directory");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<AbstractButton, Object>();
        buttonGroupValueSet.put(useSelectedProductButton, true);
        buttonGroupValueSet.put(useAllProductsInDirectoryButton, false);
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useSelectedProductButton);
        buttonGroup.add(useAllProductsInDirectoryButton);
        if (getSelectedSmosProduct() == null) {
            useSelectedProductButton.setEnabled(false);
        }
        bindingContext.bind(ALIAS_USE_SELECTED_PRODUCT, buttonGroup, buttonGroupValueSet);
        bindingContext.bindEnabledState(ALIAS_SOURCE_DIRECTORY, true, ALIAS_USE_SELECTED_PRODUCT, false);

        final JPanel sourceProductPanel = new JPanel(layout);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder("Source Product(s)"));
        sourceProductPanel.add(useSelectedProductButton);
        sourceProductPanel.add(useAllProductsInDirectoryButton);
        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));

        final JCheckBox checkBox = new JCheckBox("Descend into sub-directories");
        bindingContext.bind(ALIAS_RECURSIVE, checkBox);
        bindingContext.bindEnabledState(ALIAS_RECURSIVE, true, ALIAS_USE_SELECTED_PRODUCT, false);

        sourceProductPanel.add(checkBox);
        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));

        final PropertyDescriptor sourceDirectoryDescriptor = propertyContainer.getDescriptor(ALIAS_SOURCE_DIRECTORY);
        final JComponent component = createFileSelectorComponent(sourceDirectoryDescriptor);
        sourceProductPanel.add(component);

        final JTextField textField = (JTextField) component.getComponent(0);
        textField.setColumns(30);

        return sourceProductPanel;
    }

    private JComponent createFileSelectorComponent(PropertyDescriptor descriptor) {
        final JTextField textField = new JTextField();
        final ComponentAdapter adapter = new TextComponentAdapter(textField);
        final Binding binding = bindingContext.bind(descriptor.getName(), adapter);

        final JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(textField, BorderLayout.CENTER);

        final JButton etcButton = new JButton("...");
        panel.add(etcButton, BorderLayout.EAST);

        etcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                final int state = fileChooser.showDialog(panel, "Select");
                if (state == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                    binding.setPropertyValue(fileChooser.getSelectedFile());
                }
            }
        });

        return panel;
    }

    private Component createRoiPanel() {
        final JRadioButton useGeometryButton = new JRadioButton("Geometry");
        final PropertyDescriptor geometryDescriptor = propertyContainer.getDescriptor(ALIAS_GEOMETRY);
        if (geometryDescriptor.getValueSet() == null) {
            useGeometryButton.setEnabled(false);
        }

        final JRadioButton usePinsButton = new JRadioButton("Pins");
        final Product selectedProduct = getSelectedSmosProduct();
        if (selectedProduct == null || selectedProduct.getPinGroup().getNodeCount() == 0) {
            usePinsButton.setEnabled(false);
        }

        final JRadioButton useAreaButton = new JRadioButton("Area");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<AbstractButton, Object>();
        buttonGroupValueSet.put(useGeometryButton, 0);
        buttonGroupValueSet.put(usePinsButton, 1);
        buttonGroupValueSet.put(useAreaButton, 2);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useGeometryButton);
        buttonGroup.add(usePinsButton);
        buttonGroup.add(useAreaButton);
        bindingContext.bind(ALIAS_ROI_TYPE, buttonGroup, buttonGroupValueSet);

        final PropertyEditor selectionEditor =
                PropertyEditorRegistry.getInstance().getPropertyEditor(SingleSelectionEditor.class.getName());
        final JComboBox geometryComboBox =
                (JComboBox) selectionEditor.createEditorComponent(geometryDescriptor, bindingContext);

        final DefaultListCellRenderer listCellRenderer = new ProductNodeRenderer();
        geometryComboBox.setRenderer(listCellRenderer);

        final TableLayout layout = new TableLayout(1);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        layout.setTableWeightX(1.0);

        final JPanel roiPanel = new JPanel(layout);
        roiPanel.setBorder(BorderFactory.createTitledBorder("Region of Interest"));

        roiPanel.add(useGeometryButton);
        roiPanel.add(geometryComboBox);
        roiPanel.add(usePinsButton);
        roiPanel.add(useAreaButton);
        roiPanel.add(createLatLonPanel());

        layout.setCellPadding(1, 0, new Insets(0, 24, 3, 3));
        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));

        return roiPanel;
    }


    private Component createLatLonPanel() {
        final TableLayout layout = new TableLayout(3);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);

        final JPanel areaPanel = new JPanel(layout);
        final JLabel emptyLabel = new JLabel(" ");
        areaPanel.add(emptyLabel);
        areaPanel.add(createLatLonCoordinatePanel(ALIAS_NORTH, "North:", 4));
        areaPanel.add(emptyLabel);

        areaPanel.add(createLatLonCoordinatePanel(ALIAS_WEST, "West:", 5));
        areaPanel.add(emptyLabel);
        areaPanel.add(createLatLonCoordinatePanel(ALIAS_EAST, "East:", 5));

        areaPanel.add(emptyLabel);
        areaPanel.add(createLatLonCoordinatePanel(ALIAS_SOUTH, "South:", 4));
        areaPanel.add(emptyLabel);

        return areaPanel;
    }

    private Component createLatLonCoordinatePanel(String name, String displayName, int numColumns) {
        final PropertyEditor editor =
                PropertyEditorRegistry.getInstance().getPropertyEditor(TextFieldEditor.class.getName());

        final JTextField textField = (JTextField) editor.createEditorComponent(propertyContainer.getDescriptor(name),
                                                                               bindingContext);

        final JLabel nameLabel = new JLabel(displayName);
        final JLabel unitLabel = new JLabel("°");
        nameLabel.setEnabled(textField.isEnabled());
        unitLabel.setEnabled(textField.isEnabled());

        textField.setColumns(numColumns);
        textField.addPropertyChangeListener("enabled", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final Boolean enabled = (Boolean) evt.getNewValue();
                nameLabel.setEnabled(enabled);
                unitLabel.setEnabled(enabled);
            }
        });

        final JPanel panel = new JPanel(new FlowLayout());
        panel.add(nameLabel);
        panel.add(textField);
        panel.add(unitLabel);

        return panel;
    }

    private JComponent createTargetFilePanel() {
        final TableLayout layout = new TableLayout(1);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        layout.setTableWeightX(1.0);

        final JPanel targetFilePanel = new JPanel(layout);
        targetFilePanel.setBorder(BorderFactory.createTitledBorder("Target File"));
        targetFilePanel.add(new JLabel("Target file:"));

        final PropertyEditor fileEditor =
                PropertyEditorRegistry.getInstance().getPropertyEditor(FileEditor.class.getName());
        final PropertyDescriptor propertyDescriptor = propertyContainer.getDescriptor(ALIAS_TARGET_FILE);
        final JComponent editor = fileEditor.createEditorComponent(propertyDescriptor, bindingContext);
        targetFilePanel.add(editor);

        final JTextField textField = (JTextField) editor.getComponent(0);
        textField.setColumns(30);

        return targetFilePanel;
    }

    private Product getSelectedSmosProduct() {
        final Product selectedProduct = appContext.getSelectedProduct();

        if (selectedProduct != null) {
            final ProductReader productReader = selectedProduct.getProductReader();
            if (productReader instanceof SmosProductReader) {
                final SmosProductReader smosProductReader = (SmosProductReader) productReader;
                final ExplorerFile explorerFile = smosProductReader.getExplorerFile();
                if (explorerFile instanceof SmosFile) {
                    return selectedProduct;
                }
            }
        }

        return null;
    }

    private Area getArea() {
        switch (parameterBlock.roiType) {
        case 0: {
            final Area area = new Area();
            final FeatureIterator<SimpleFeature> featureIterator = parameterBlock.geometry.getFeatureCollection().features();

            while (featureIterator.hasNext()) {
                final Area featureArea;
                final SimpleFeature feature = featureIterator.next();
                if (feature.getFeatureType() == Placemark.getFeatureType()) {
                    featureArea = getAreaForPlacemarkFeature(feature);
                } else {
                    featureArea = getArea(feature);
                }
                if (featureArea != null && !featureArea.isEmpty()) {
                    area.add(featureArea);
                }
            }
            return area;
        }
        case 1: {
            final Area area = new Area();
            final PlacemarkGroup pinGroup = appContext.getSelectedProduct().getPinGroup();

            for (Placemark pin : pinGroup.toArray(new Placemark[pinGroup.getNodeCount()])) {
                area.add(getAreaForPlacemarkFeature(pin.getFeature()));
            }
            return area;
        }
        case 2: {
            return new Area(new Rectangle2D.Double(parameterBlock.west, parameterBlock.south,
                                                   parameterBlock.east - parameterBlock.west,
                                                   parameterBlock.north - parameterBlock.south));
        }
        default:
            // cannot happen
            throw new IllegalStateException(MessageFormat.format("Illegal ROI type: {0}", parameterBlock.roiType));
        }
    }

    private static Area getArea(SimpleFeature feature) {
        Shape shape = null;
        try {
            final Object geometry = feature.getDefaultGeometry();
            if (geometry instanceof Geometry) {
                shape = new LiteShape2((Geometry) geometry, null, null, true);
            }
        } catch (TransformException e) {
            // ignore
        } catch (FactoryException e) {
            // ignore
        }
        return shape != null ? new Area(shape) : null;
    }

    private static Area getAreaForPlacemarkFeature(SimpleFeature feature) {
        final Point geometry = (Point) feature.getDefaultGeometry();
        double lon = geometry.getX();
        double lat = geometry.getY();

        final double x = lon - 0.08;
        final double y = lat - 0.08;
        final double w = 0.16;
        final double h = 0.16;

        return new Area(new Rectangle2D.Double(x, y, w, h));
    }

    private static final class ProductNodeRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            final Component component =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!(component instanceof JLabel)) {
                return component;
            }
            final JLabel label = (JLabel) component;
            if (value instanceof ProductNode) {
                label.setText(((ProductNode) value).getDisplayName());
            } else {
                label.setText("");
            }
            return label;
        }
    }

    private static final class ParameterBlock {

        @Parameter(alias = ALIAS_USE_SELECTED_PRODUCT)
        private boolean useSelectedProduct;

        @Parameter(alias = ALIAS_ROI_TYPE, defaultValue = "2", valueSet = {"0", "1", "2"})
        private int roiType;

        @Parameter(alias = ALIAS_SOURCE_DIRECTORY)
        private File sourceDirectory;

        @Parameter(alias = ALIAS_RECURSIVE, defaultValue = "false")
        private boolean recursive;

        @Parameter(alias = ALIAS_GEOMETRY)
        private VectorDataNode geometry;

        @Parameter(alias = ALIAS_NORTH, defaultValue = "90.0", interval = "[-90.0, 90.0]")
        private double north;

        @Parameter(alias = ALIAS_SOUTH, defaultValue = "-90.0", interval = "[-90.0, 90.0]")
        private double south;

        @Parameter(alias = ALIAS_EAST, defaultValue = "180.0", interval = "[-180.0, 180.0]")
        private double east;

        @Parameter(alias = ALIAS_WEST, defaultValue = "-180.0", interval = "[-180.0, 180.0]")
        private double west;

        @Parameter(alias = ALIAS_TARGET_FILE, notNull = true, notEmpty = true)
        private File targetFile;
    }
}
