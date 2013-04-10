package org.esa.beam.smos.ee2netcdf.visat;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModelessDialog;
import org.esa.beam.framework.ui.RegionBoundsInputUI;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.DirectoryChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetCDFExportDialog extends ModelessDialog {

    private static final String TARGET_DIRECTORY_BINDING = "targetDirectory";
    private final ExportParameter exportParameter;
    private final PropertyContainer propertyContainer;
    private final AppContext appContext;
    private final BindingContext bindingContext;

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
    }

    private void init(PropertyContainer propertyContainer) throws ValidationException {
        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertyContainer.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final File defaultTargetDirectory = GuiHelper.getDefaultTargetDirectory(appContext);
        propertyContainer.setValue(TARGET_DIRECTORY_BINDING, defaultTargetDirectory);

        final Product selectedSmosProduct = DialogHelper.getSelectedSmosProduct(appContext);
        if (selectedSmosProduct != null) {
            final List<Geometry> geometries = GuiHelper.getPolygonGeometries(selectedSmosProduct);
            if (!geometries.isEmpty()) {
                GuiHelper.bindGeometries(geometries, propertyContainer);
            }
            propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, true);

            setSelectionToSelectedGeometry(propertyContainer);
        } else {
            propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, false);
            propertyContainer.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_PRODUCT);
        }
    }

    private void setAreaToGlobe(PropertyContainer propertyContainer) {
        propertyContainer.setValue(BindingConstants.NORTH, 90.0);
        propertyContainer.setValue(BindingConstants.EAST, 180.0);
        propertyContainer.setValue(BindingConstants.SOUTH, -90.0);
        propertyContainer.setValue(BindingConstants.WEST, -180.0);

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
        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<AbstractButton, Object>();
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
        final ConverterSwingWorker worker = new ConverterSwingWorker(appContext, exportParameter);

        super.onOK();

        worker.execute();
    }
}
