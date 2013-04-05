/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat.export;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.smos.gui.BindingConstants;
import org.esa.beam.smos.gui.ChooserFactory;
import org.esa.beam.smos.gui.DefaultChooserFactory;
import org.esa.beam.smos.gui.GuiHelper;
import org.esa.beam.util.io.FileChooserFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GridPointExportDialog extends ModalDialog {

    static final String ALIAS_RECURSIVE = "recursive";
    static final String ALIAS_TARGET_FILE = "targetFileOrDir";
    static final String ALIAS_EXPORT_FORMAT = "exportFormat";
    static final String NAME_CSV = "CSV";

    static final String NAME_EEF = "Earth Explorer subsets";
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
        bindingContext.bindEnabledState(BindingConstants.GEOMETRY, true, BindingConstants.ROI_TYPE, 0);
        GuiHelper.bindLonLatPanelToRoiType(2, bindingContext);

        createUI();
    }

    @Override
    protected void onOK() {
        final File sourceDirectory = (File) propertyContainer.getValue(BindingConstants.SOURCE_DIRECTORY);
        final File targetFile = (File) propertyContainer.getValue(ALIAS_TARGET_FILE);
        setDefaultSourceDirectory(sourceDirectory);
        setDefaultTargetFile(targetFile);
        if (targetFile.exists() && targetFile.isFile()) {
            final String message = MessageFormat.format(
                    "The selected target file\n''{0}''\nalready exists.\n\nDo you want to overwrite the target file?",
                    targetFile.getPath());
            final int answer = JOptionPane.showConfirmDialog(getJDialog(), message, getTitle(),
                    JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        super.onOK();
        exportSwingWorker.execute();
    }

    @Override
    protected boolean verifyUserInput() {
        final int roiType = (Integer) propertyContainer.getValue(BindingConstants.ROI_TYPE);
        if (roiType == 2) {
            final double north = (Double) propertyContainer.getValue(BindingConstants.NORTH);
            final double south = (Double) propertyContainer.getValue(BindingConstants.SOUTH);
            if (north <= south) {
                showErrorDialog("The southern latitude must be less than the northern latitude.");
                return false;
            }
            final double east = (Double) propertyContainer.getValue(BindingConstants.EAST);
            final double west = (Double) propertyContainer.getValue(BindingConstants.WEST);
            if (east <= west) {
                showErrorDialog("The western longitude must be less than the eastern longitude.");
                return false;
            }
        }
        final File targetFile = (File) propertyContainer.getValue(ALIAS_TARGET_FILE);
        final String exportFormat = (String) propertyContainer.getValue(ALIAS_EXPORT_FORMAT);
        if (NAME_CSV.equals(exportFormat)) {
            if (targetFile.exists() && !targetFile.isFile()) {
                showErrorDialog("The target file must be a normal file, not a directory.");
                return false;
            }
        } else {
            if (targetFile.exists() && !targetFile.isDirectory()) {
                showErrorDialog("The target file must be a directory, not a normal file.");
                return false;
            }
        }
        return true;
    }

    private void initPropertyContainer() throws ValidationException {
        propertyContainer.setDefaultValues();

        final File defaultSourceDirectory = GuiHelper.getDefaultSourceDirectory(appContext);
        propertyContainer.setValue(BindingConstants.SOURCE_DIRECTORY, defaultSourceDirectory);

        final File targetFile = getDefaultTargetFile();
        propertyContainer.setValue(ALIAS_TARGET_FILE, targetFile);

        final Product selectedProduct = getSelectedSmosProduct();
        if (selectedProduct != null) {
            final List<VectorDataNode> geometryNodeList = GuiHelper.getGeometries(selectedProduct);
            if (!geometryNodeList.isEmpty()) {
                GuiHelper.bindGeometries(geometryNodeList, propertyContainer);
            } else if (selectedProduct.getPinGroup().getNodeCount() != 0) {
                propertyContainer.setValue(BindingConstants.ROI_TYPE, 1);
            }
        }
        propertyContainer.setValue(BindingConstants.SELECTED_PRODUCT, selectedProduct != null);
        if (targetFile.isDirectory()) {
            propertyContainer.setValue(ALIAS_EXPORT_FORMAT, NAME_EEF);
        }
        propertyContainer.addPropertyChangeListener(ALIAS_EXPORT_FORMAT, new ExportFormatChangeListener());
    }

    private void createUI() {
        final JPanel mainPanel = GuiHelper.createPanelWithBoxLayout();

        mainPanel.add(createSourceProductPanel());
        mainPanel.add(createRoiPanel());
        mainPanel.add(createTargetFilePanel());

        setContent(mainPanel);
    }

    private JComponent createSourceProductPanel() {
        final boolean useSelectProductEnabled = getSelectedSmosProduct() != null;

        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        final JPanel sourceProductPanel = new JPanel(layout);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder("Source Products"));

        GuiHelper.addSourceProductsButtons(sourceProductPanel, useSelectProductEnabled, bindingContext);

        final JCheckBox checkBox = new JCheckBox("Descend into subdirectories");
        bindingContext.bind(ALIAS_RECURSIVE, checkBox);
        bindingContext.bindEnabledState(ALIAS_RECURSIVE, true, BindingConstants.SELECTED_PRODUCT, false);

        final PropertyDescriptor sourceDirectoryDescriptor = propertyContainer.getDescriptor(BindingConstants.SOURCE_DIRECTORY);
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(sourceDirectoryDescriptor, new DefaultChooserFactory(), bindingContext);

        layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
        sourceProductPanel.add(fileEditor);
        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
        sourceProductPanel.add(checkBox);

        return sourceProductPanel;
    }

    private Component createRoiPanel() {
        final JRadioButton useGeometryButton = new JRadioButton("Geometry");
        final PropertyDescriptor geometryDescriptor = propertyContainer.getDescriptor(BindingConstants.GEOMETRY);
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
        bindingContext.bind(BindingConstants.ROI_TYPE, buttonGroup, buttonGroupValueSet);

        final JComboBox geometryComboBox = GuiHelper.createGeometryComboBox(geometryDescriptor, bindingContext);

        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);
        final JPanel roiPanel = new JPanel(layout);
        roiPanel.setBorder(BorderFactory.createTitledBorder("Region of Interest"));

        roiPanel.add(useGeometryButton);
        roiPanel.add(geometryComboBox);
        roiPanel.add(usePinsButton);
        roiPanel.add(useAreaButton);
        final Component latLonPanel = GuiHelper.createLatLonPanel(propertyContainer, bindingContext);
        roiPanel.add(latLonPanel);

        layout.setCellPadding(1, 0, new Insets(0, 24, 3, 3));
        layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));

        return roiPanel;
    }

    private JComponent createTargetFilePanel() {
        final TableLayout layout = GuiHelper.createWeightedTablelayout(1);

        final JPanel targetFilePanel = new JPanel(layout);
        targetFilePanel.setBorder(BorderFactory.createTitledBorder("Target File"));

        final PropertyDescriptor formatDescriptor = propertyContainer.getDescriptor(ALIAS_EXPORT_FORMAT);
        final JComboBox formatComboBox = new JComboBox(formatDescriptor.getValueSet().getItems());
        bindingContext.bind(ALIAS_EXPORT_FORMAT, formatComboBox);

        final JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        formatPanel.add(new JLabel("Export as:"));
        formatPanel.add(formatComboBox);

        final JLabel label = new JLabel();
        if (NAME_CSV.equals(propertyContainer.getValue(ALIAS_EXPORT_FORMAT))) {
            label.setText("Save to file:");
        } else {
            label.setText("Save subset files to directory:");
        }
        final PropertyDescriptor targetFileDescriptor = propertyContainer.getDescriptor(ALIAS_TARGET_FILE);
        final ChooserFactory chooserFactory = new ChooserFactory() {
            @Override
            public JFileChooser createChooser(File file) {
                final FileChooserFactory chooserFactory = FileChooserFactory.getInstance();
                final JFileChooser fileChooser;
                if (NAME_CSV.equals(bindingContext.getBinding(ALIAS_EXPORT_FORMAT).getPropertyValue())) {
                    fileChooser = chooserFactory.createFileChooser(file);
                    fileChooser.setAcceptAllFileFilterUsed(true);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("CSV", "CSV"));
                } else {
                    fileChooser = chooserFactory.createDirChooser(file);
                }
                return fileChooser;
            }
        };
        final JComponent fileEditor = GuiHelper.createFileEditorComponent(targetFileDescriptor, chooserFactory, bindingContext);
        targetFilePanel.add(formatPanel);
        targetFilePanel.add(label);
        targetFilePanel.add(fileEditor);

        bindingContext.addPropertyChangeListener(ALIAS_EXPORT_FORMAT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NAME_CSV.equals(evt.getNewValue())) {
                    label.setText("Save to file:");
                } else {
                    label.setText("Save subset files to directory:");
                }
            }
        });
        return targetFilePanel;
    }

    private void setDefaultSourceDirectory(File sourceDirectory) {
        appContext.getPreferences().setPropertyString(GuiHelper.LAST_SOURCE_DIR_KEY, sourceDirectory.getPath());
    }

    private File getDefaultTargetFile() {
        final String def = new File(System.getProperty("user.home", "."), "export.csv").getPath();
        return new File(appContext.getPreferences().getPropertyString(GuiHelper.LAST_TARGET_FILE_KEY, def));
    }

    private void setDefaultTargetFile(File targetFile) {
        appContext.getPreferences().setPropertyString(GuiHelper.LAST_TARGET_FILE_KEY, targetFile.getPath());
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


    private class ExportFormatChangeListener implements PropertyChangeListener {

        File last;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NAME_CSV.equals(evt.getNewValue())) {
                final File dir = (File) propertyContainer.getValue(ALIAS_TARGET_FILE);
                if (last != null) {
                    propertyContainer.setValue(ALIAS_TARGET_FILE, new File(dir, last.getName()));
                }
            } else {
                last = (File) propertyContainer.getValue(ALIAS_TARGET_FILE);
                propertyContainer.setValue(ALIAS_TARGET_FILE, last.getParentFile());
            }
        }
    }
}
