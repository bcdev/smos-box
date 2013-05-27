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
import org.esa.beam.smos.ee2netcdf.ConverterOp;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.DirectoryChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;
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

    // package access for testing only tb 2013-05-27
    static List<File> getTargetFiles(String filePath, File targetDir) throws IOException {
        final ArrayList<File> targetFiles = new ArrayList<File>();

        final File file = new File(filePath);
        if (file.isFile()) {
            final File outputFile = ConverterOp.getOutputFile(file, targetDir);
            targetFiles.add(outputFile);
        } else {
            final TreeSet<File> sourceFileSet = new TreeSet<File>();
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
        final ArrayList<File> existingFiles = new ArrayList<File>();

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
                        files);
                final int answer = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(),
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
                exportParameter.setOverwriteTarget(true);
            }
        } catch (IOException e) {
            // @todo 1 tb/tb handle this 2013-05-27
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        final ConverterSwingWorker worker = new ConverterSwingWorker(appContext, exportParameter);

        super.onOK();

        GuiHelper.setDefaultSourceDirectory(exportParameter.getSourceDirectory(), appContext);
        GuiHelper.setDefaultTargetDirectory(exportParameter.getTargetDirectory(), appContext);

        worker.execute();
    }
}
