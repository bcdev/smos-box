package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.*;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.selection.SelectionManager;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.RegionBoundsInputUI;
import org.esa.beam.smos.ee2netcdf.ConverterOp;
import org.esa.beam.smos.ee2netcdf.ExportParameter;
import org.esa.beam.smos.gui.*;
import org.esa.beam.util.io.WildcardMatcher;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class NetCDFExportDialog extends ProductChangeAwareDialog {

    private static final String TARGET_DIRECTORY_BINDING = "targetDirectory";
    private final ExportParameter exportParameter;
    private final PropertyContainer propertyContainer;
    private final AppContext appContext;
    private final BindingContext bindingContext;
    private final ProductSelectionListener productSelectionListener;
    private GeometryListener geometryListener;

    public NetCDFExportDialog(AppContext appContext, String helpId) {
        super(appContext.getApplicationWindow(), "Convert SMOS EE File to NetCDF 4", ID_OK | ID_CLOSE | ID_HELP, helpId); /* I18N */

        this.appContext = appContext;
        exportParameter = new ExportParameter();

        propertyContainer = PropertyContainer.createObjectBacked(exportParameter);
        setAreaToGlobe(propertyContainer);

        bindingContext = new BindingContext(propertyContainer);

        createUi();

        bindingContext.bindEnabledState(BindingConstants.GEOMETRY, true, BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_GEOMETRY);
        try {
            init(propertyContainer);
        } catch (ValidationException e) {
            throw new IllegalStateException(e.getMessage());
        }

        final ProductManager productManager = appContext.getProductManager();
        productManager.addListener(new ProductManagerListener(this));

        geometryListener = new GeometryListener(this);

        final SelectionManager selectionManager = appContext.getApplicationPage().getSelectionManager();
        productSelectionListener = new ProductSelectionListener(this, selectionManager);
        selectionManager.addSelectionChangeListener(productSelectionListener);
    }

    // package access for testing only tb 2013-05-27
    static List<File> getTargetFiles(String filePath, File targetDir) throws IOException {
        final ArrayList<File> targetFiles = new ArrayList<>();

        final File file = new File(filePath);
        if (file.isFile()) {
            final File outputFile = ConverterOp.getOutputFile(file, targetDir);
            targetFiles.add(outputFile);
        } else {
            final TreeSet<File> sourceFileSet = new TreeSet<>();
            WildcardMatcher.glob(filePath, sourceFileSet);
            for (File aSourceFile : sourceFileSet) {
                final File outputFile = ConverterOp.getOutputFile(aSourceFile, targetDir);
                targetFiles.add(outputFile);
            }
        }

        return targetFiles;
    }

    // package access for testing only tb 2013-05-27
    static List<File> getExistingFiles(List<File> targetFiles) {
        final ArrayList<File> existingFiles = new ArrayList<>();

        for (File targetFile : targetFiles) {
            if (targetFile.isFile()) {
                existingFiles.add(targetFile);
            }

        }
        return existingFiles;
    }

    // package access for testing only tb 2013-05-27
    static String listToString(List<File> targetFiles) {
        int fileCount = 0;
        final StringBuilder stringBuilder = new StringBuilder();
        for (File targetFile : targetFiles) {
            stringBuilder.append(targetFile.getAbsolutePath());
            stringBuilder.append("\n");
            fileCount++;
            if (fileCount >= 10) {
                stringBuilder.append("...");
                break;
            }
        }
        return stringBuilder.toString();
    }

    private void init(PropertyContainer propertyContainer) throws ValidationException {
        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertyContainer.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final File defaultTargetDirectory = GuiHelper.getDefaultTargetDirectory(appContext);
        propertyContainer.setValue(TARGET_DIRECTORY_BINDING, defaultTargetDirectory);

        updateSelectedProductAndGeometries(propertyContainer);
    }

    @SuppressWarnings("ConstantConditions")
    private void updateSelectedProductAndGeometries(PropertyContainer propertyContainer) throws ValidationException {
        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct != null) {
            propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, true);
            final List<Geometry> geometries = GuiHelper.getPolygonGeometries(selectedSmosProduct);
            if (!geometries.isEmpty()) {
                GuiHelper.bindGeometries(geometries, propertyContainer);
            } else {
                removeGeometries();
            }
            setSelectedProductButtonEnabled(true);
            propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, true);

            setSelectionToSelectedGeometry(propertyContainer);

            selectedSmosProduct.addProductNodeListener(geometryListener);
        } else {
            propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, false);
            propertyContainer.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_PRODUCT);
        }
    }

    private void removeProductAndGeometries(Product product) {
        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct == null) {
            setSelectedProductButtonEnabled(false);

            final List<VectorDataNode> geometryNodeList = GuiHelper.getGeometries(product);
            if (!geometryNodeList.isEmpty()) {
                removeGeometries();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setSelectedProductButtonEnabled(boolean enabled) {
        if (bindingContext == null) {
            return;
        }
        final Binding binding = bindingContext.getBinding(BindingConstants.SELECTED_PRODUCT);
        final JComponent[] components = binding.getComponents();
        for (final JComponent component : components) {
            if (component instanceof JRadioButton) {
                if (((JRadioButton) component).getText().equals(BindingConstants.USE_SELECTED_PRODUCT_BUTTON_NAME)) {
                    component.setEnabled(enabled);
                    break;
                }
            }
        }
    }


    private void removeGeometries() {
        final Property geometryProperty = propertyContainer.getProperty(BindingConstants.GEOMETRY);
        geometryProperty.getDescriptor().setValueSet(new ValueSet(new VectorDataNode[0]));
        propertyContainer.setValue(BindingConstants.GEOMETRY, null);
        propertyContainer.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_AREA);
    }


    private void setAreaToGlobe(PropertyContainer propertyContainer) {
        propertyContainer.setValue(RegionBoundsInputUI.PROPERTY_NORTH_BOUND, 90.0);
        propertyContainer.setValue(RegionBoundsInputUI.PROPERTY_EAST_BOUND, 180.0);
        propertyContainer.setValue(RegionBoundsInputUI.PROPERTY_SOUTH_BOUND, -90.0);
        propertyContainer.setValue(RegionBoundsInputUI.PROPERTY_WEST_BOUND, -180.0);
    }

    private void setSelectionToSelectedGeometry(PropertyContainer propertyContainer) {
        final Geometry selectedGeometry = GuiHelper.getSelectedGeometry(appContext);
        if (selectedGeometry != null) {
            propertyContainer.setValue(BindingConstants.GEOMETRY, selectedGeometry);
        }
    }

    private void createUi() {
        final JPanel mainPanel = GuiHelper.createPanelWithBoxLayout();

        mainPanel.add(createSourceProductsPanel());
        mainPanel.add(createRoiPanel());
        mainPanel.add(createTargetDirPanel());

        setContent(mainPanel);
    }

    private JComponent createSourceProductsPanel() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        final JPanel sourceProductPanel = new JPanel(layout);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder("Source Products"));
        final boolean canProductSelectionBeEnabled = DialogHelper.canProductSelectionBeEnabled(appContext);

        GuiHelper.addSourceProductsButtons(sourceProductPanel, canProductSelectionBeEnabled, bindingContext);

        final PropertyDescriptor sourceDirectoryDescriptor = propertyContainer.getDescriptor(BindingConstants.SOURCE_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(sourceDirectoryDescriptor, new DefaultChooserFactory(), bindingContext);

        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        sourceProductPanel.add(fileEditor);

        return sourceProductPanel;
    }

    private JComponent createRoiPanel() {
        final JRadioButton wholeProductButton = new JRadioButton("Whole Product");

        final JRadioButton useGeometryButton = new JRadioButton("Geometry");
        final PropertyDescriptor geometryDescriptor = propertyContainer.getDescriptor(BindingConstants.GEOMETRY);
        if (geometryDescriptor.getValueSet() == null) {
            useGeometryButton.setEnabled(false);
        }

        final JRadioButton useAreaButton = new JRadioButton("Area");
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<>();
        buttonGroupValueSet.put(wholeProductButton, BindingConstants.ROI_TYPE_PRODUCT);
        buttonGroupValueSet.put(useGeometryButton, BindingConstants.ROI_TYPE_GEOMETRY);
        buttonGroupValueSet.put(useAreaButton, BindingConstants.ROI_TYPE_AREA);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(wholeProductButton);
        buttonGroup.add(useGeometryButton);
        buttonGroup.add(useAreaButton);
        bindingContext.bind(BindingConstants.ROI_TYPE, buttonGroup, buttonGroupValueSet);

        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        layout.setCellPadding(4, 0, new Insets(0, 24, 3, 3));

        final JPanel roiPanel = new JPanel(layout);
        roiPanel.setBorder(BorderFactory.createTitledBorder("Region of Interest"));

        final JComboBox geometryComboBox = GuiHelper.createGeometryComboBox(geometryDescriptor, bindingContext);

        roiPanel.add(wholeProductButton);
        roiPanel.add(useGeometryButton);
        roiPanel.add(geometryComboBox);
        roiPanel.add(useAreaButton);

        final RegionBoundsInputUI regionBoundsInputUI = new RegionBoundsInputUI(bindingContext);
        bindingContext.addPropertyChangeListener(BindingConstants.ROI_TYPE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final int roiType = (Integer) evt.getNewValue();
                if (roiType == BindingConstants.ROI_TYPE_AREA) {
                    regionBoundsInputUI.setEnabled(true);
                } else {
                    regionBoundsInputUI.setEnabled(false);
                }
            }
        });
        regionBoundsInputUI.setEnabled(false);
        roiPanel.add(regionBoundsInputUI.getUI());

        return roiPanel;
    }

    private JComponent createTargetDirPanel() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);

        final JPanel targetDirPanel = new JPanel(layout);
        targetDirPanel.setBorder(BorderFactory.createTitledBorder("Target Directory"));

        final JLabel label = new JLabel();
        label.setText("Save files to directory:");
        targetDirPanel.add(label);

        final PropertyDescriptor targetDirectoryDescriptor = propertyContainer.getDescriptor(TARGET_DIRECTORY_BINDING);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(targetDirectoryDescriptor, new DirectoryChooserFactory(), bindingContext, false);

        targetDirPanel.add(fileEditor);

        return targetDirPanel;
    }

    @Override
    protected void onOK() {
        try {
            final List<File> targetFiles;
            if (exportParameter.isUseSelectedProduct()) {
                targetFiles = getTargetFiles(appContext.getSelectedProduct().getFileLocation().getAbsolutePath(), exportParameter.getTargetDirectory());
            } else {
                targetFiles = getTargetFiles(exportParameter.getSourceDirectory().getAbsolutePath() + File.separator + "*", exportParameter.getTargetDirectory());
            }

            final List<File> existingFiles = getExistingFiles(targetFiles);
            if (!existingFiles.isEmpty()) {
                final String files = listToString(existingFiles);
                final String message = MessageFormat.format(
                        "The selected target file(s) already exists.\n\nDo you want to overwrite the target file(s)?\n\n" +
                                "{0}",
                        files
                );
                final int answer = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(),
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
                exportParameter.setOverwriteTarget(true);
            }
        } catch (IOException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        final ConverterSwingWorker worker = new ConverterSwingWorker(appContext, exportParameter);

        GuiHelper.setDefaultSourceDirectory(exportParameter.getSourceDirectory(), appContext);
        GuiHelper.setDefaultTargetDirectory(exportParameter.getTargetDirectory(), appContext);

        worker.execute();
    }

    @Override
    protected void onClose() {
        productSelectionListener.dispose();
        super.onClose();
    }

    @Override
    protected void productAdded() {
        try {
            updateSelectedProductAndGeometries(propertyContainer);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void productRemoved(Product product) {
        removeProductAndGeometries(product);
        product.removeProductNodeListener(geometryListener);
    }

    @Override
    protected void geometryAdded() {
        try {
            updateSelectedProductAndGeometries(propertyContainer);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void geometryRemoved() {
        try {
            updateSelectedProductAndGeometries(propertyContainer);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void productSelectionChanged() {
        try {
            updateSelectedProductAndGeometries(propertyContainer);
        } catch (ValidationException e) {
            showErrorDialog("Internal error: " + e.getMessage());
        }
    }
}
