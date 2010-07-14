package org.esa.beam.smos.visat.export;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
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
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;

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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GridPointExportDialog extends ModalDialog {

    private static final String LAST_SOURCE_DIR_KEY = "org.esa.beam.smos.export.sourceDir";
    private static final String LAST_TARGET_DIR_KEY = "org.esa.beam.smos.export.targetDir";

    static final String ALIAS_GEOMETRY = "geometry";
    static final String ALIAS_ROI_TYPE = "roiType";
    static final String ALIAS_RECURSIVE = "recursive";
    static final String ALIAS_SOURCE_DIRECTORY = "sourceDirectory";
    static final String ALIAS_TARGET_FILE = "targetFile";
    static final String ALIAS_USE_SELECTED_PRODUCT = "useSelectedProduct";
    static final String ALIAS_NORTH = "north";
    static final String ALIAS_SOUTH = "south";
    static final String ALIAS_EAST = "east";
    static final String ALIAS_WEST = "west";
    static final String ALIAS_EXPORT_TYPE = "EXPORT_TYPE";

    private final AppContext appContext;
    private final PropertyContainer propertyContainer;
    private final BindingContext bindingContext;
    private GridPointExportSwingWorker exportSwingWorker;

    GridPointExportDialog(final AppContext appContext, String helpId) {
        super(appContext.getApplicationWindow(), "Export SMOS Grid Points", ID_OK_CANCEL_HELP, helpId); /* I18N */
        exportSwingWorker = new GridPointExportSwingWorker(appContext);
        this.appContext = appContext;

        propertyContainer = PropertyContainer.createObjectBacked(exportSwingWorker, new ParameterDescriptorFactory());
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
        final File sourceDirectory = (File) propertyContainer.getValue(ALIAS_SOURCE_DIRECTORY);
        final File targetFile = (File) propertyContainer.getValue(ALIAS_TARGET_FILE);
        setDefaultSourceDirectory(sourceDirectory);
        setDefaultTargetDirectory(targetFile.getParentFile());

        if (targetFile.exists()) {
            final String message = MessageFormat.format(
                    "The selected target file\n''{0}''\n already exists.\n\n Do you want to overwrite the target file?",
                    targetFile.getPath());
            final int i = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(), JOptionPane.YES_NO_OPTION);
            if (i != JOptionPane.YES_OPTION) {
                return;
            }
        }
        super.onOK();
        exportSwingWorker.execute();
    }

    @Override
    protected boolean verifyUserInput() {
        final int roiType = (Integer) propertyContainer.getValue(ALIAS_ROI_TYPE);
        if (roiType == 2) {
            final double north = (Double) propertyContainer.getValue(ALIAS_NORTH);
            final double south = (Double) propertyContainer.getValue(ALIAS_SOUTH);
            if (north <= south) {
                showErrorDialog("The southern latitude must be less than the northern latitude.");
                return false;
            }
            final double east = (Double) propertyContainer.getValue(ALIAS_EAST);
            final double west = (Double) propertyContainer.getValue(ALIAS_WEST);
            if (east <= west) {
                showErrorDialog("The western longitude must be less than the eastern longitude.");
                return false;
            }
        }
        return true;
    }

    private void initPropertyContainer() throws ValidationException {
        propertyContainer.setDefaultValues();
        propertyContainer.setValue(ALIAS_SOURCE_DIRECTORY, getDefaultSourceDirectory());
        propertyContainer.setValue(ALIAS_TARGET_FILE, new File(getDefaultTargetDirectory(), "export.csv"));

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
        // todo - export type radio buttons (rq-20100714)
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

        final JCheckBox checkBox = new JCheckBox("Descend into subdirectories");
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
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
        final JLabel unitLabel = new JLabel("\u00b0");
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

    private File getDefaultSourceDirectory() {
        return new File(appContext.getPreferences().getPropertyString(LAST_SOURCE_DIR_KEY,
                                                                      System.getProperty("user.home", ".")));
    }

    private void setDefaultSourceDirectory(File sourceDirectory) {
        appContext.getPreferences().setPropertyString(LAST_SOURCE_DIR_KEY, sourceDirectory.getPath());
    }

    private File getDefaultTargetDirectory() {
        return new File(appContext.getPreferences().getPropertyString(LAST_TARGET_DIR_KEY,
                                                                      System.getProperty("user.home", ".")));
    }

    private void setDefaultTargetDirectory(File targetDirectory) {
        appContext.getPreferences().setPropertyString(LAST_TARGET_DIR_KEY, targetDirectory.getPath());
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
}
