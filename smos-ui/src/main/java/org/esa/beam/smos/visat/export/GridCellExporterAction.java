/*
 * $Id: $
 *
 * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.smos.visat.export;

import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.binding.ValueDescriptor;
import com.bc.ceres.binding.ValueModel;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.binding.swing.Binding;
import com.bc.ceres.binding.swing.BindingContext;
import com.bc.ceres.binding.swing.ComponentAdapter;
import com.bc.ceres.binding.swing.ValueEditor;
import com.bc.ceres.binding.swing.ValueEditorRegistry;
import com.bc.ceres.binding.swing.internal.FileEditor;
import com.bc.ceres.binding.swing.internal.SingleSelectionEditor;
import com.bc.ceres.binding.swing.internal.TextComponentAdapter;
import com.bc.ceres.binding.swing.internal.TextFieldEditor;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ROIDefinition;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 2.0
 */
public class GridCellExporterAction extends ExecCommand {

    @Override
    public void actionPerformed(final CommandEvent event) {
        openDialog(VisatApp.getApp(), "smosGridCellExport");
//TODO mz - 22090622 check why teh command has no helpId        
//        openDialog(VisatApp.getApp(), event.getCommand().getHelpId());
    }

    private void openDialog(VisatApp visatApp, String helpId) {
        final Product selectedProduct = visatApp.getSelectedProduct();
        GridCellExportDialog dialog = new GridCellExportDialog(visatApp, selectedProduct, helpId);
        dialog.show();
    }

    private static class GridCellExportDialog extends ModalDialog {

        private static final String LAST_DIR_KEY = "user.smos.import.dir";

        private final VisatApp visatApp;
        private final Product product;
        private final ValueContainer vc;
        private final BindingContext bindingContext;
        private final GridCellExportModel model;

        GridCellExportDialog(final VisatApp visatApp, Product selectedProduct, String helpId) {
            super(visatApp.getMainFrame(), "Export SMOS Grid Cells", ID_OK_CANCEL_HELP, helpId); /* I18N */
            this.visatApp = visatApp;
            this.product = selectedProduct;
            model = new GridCellExportModel();

            String smosDirPath = visatApp.getPreferences().getPropertyString(LAST_DIR_KEY,
                                                                             SystemUtils.getUserHomeDir().getPath());
            model.scanDirectory = new File(smosDirPath);
            model.output = new File(SystemUtils.getUserHomeDir(), "grid_cell_export.csv");

            vc = ValueContainer.createObjectBacked(model);
            bindingContext = new BindingContext(vc);
            try {
                prepareBinding();
            } catch (ValidationException e) {
                // should not happen
            }
            makeUI();
        }

        @Override
        protected void onOK() {
            if (model.output.exists()) {
                String message = MessageFormat.format("The specified output file\n\"{0}\"\n already exists.\n\n"
                                                      + "Do you want to overwrite the existing file?",
                                                      model.output.getPath());
                final int answer = JOptionPane.showConfirmDialog(getJDialog(), message,
                                                                 getTitle(), JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            if (model.roiSource == 2) {
                if (model.north <= model.south) {
                    String message = "The specified value for north can not be smaller than value for south.";
                    visatApp.showErrorDialog(getTitle(), message);
                    return;
                }
                if (model.east <= model.west) {
                    String message = "The specified value for east can not be smaller than value for west.";
                    visatApp.showErrorDialog(getTitle(), message);
                    return;
                }
            }
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(model.output);
            } catch (FileNotFoundException e) {
                visatApp.showErrorDialog("Could not create CSV file :\n" + e.getMessage());
                return;
            }
            final CsvExport csvExport = new CsvExport(printWriter, ";");
            ProgressMonitorSwingWorker<Void, Void> swingWorker = new ProgressMonitorSwingWorker<Void, Void>(
                    getJDialog(), "Exporting grid cells") {

                @Override
                protected Void doInBackground(ProgressMonitor pm) throws Exception {
                    Area area = getArea();
                    GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(csvExport, area);
                    try {
                        if (model.useOpenProduct) {
                            streamHandler.processProduct(product, pm);
                        } else {
                            streamHandler.processDirectory(model.scanDirectory, model.scanRecursive, pm);
                        }
                    } catch (IOException e) {
                        visatApp.showErrorDialog(
                                "An error occured while exporting the grid cell data:\n" + e.getMessage());
                        return null;
                    } finally {
                        try {
                            csvExport.close();
                        } catch (IOException e) {
                            visatApp.showErrorDialog("An error occured while closing the CSV file:\n" + e.getMessage());
                        }
                    }
                    return null;
                }
            };
            super.onOK();
            swingWorker.execute();
        }

        private Area getArea() {
            if (model.roiSource == 0) {
                final GeoCoding currentGeoCoding = model.roiRaster.getGeoCoding();
                Shape shape = model.roiRaster.getROIDefinition().getShapeFigure().getShape();
                GeneralPath geoPath = ProductUtils.convertToGeoPath(shape, currentGeoCoding);
                return new Area(geoPath);
            } else if (model.roiSource == 1) {
                Area wholeArea = null;
                for (Pin pin : product.getPinGroup().getSelectedNodes()) {
                    double lat = pin.getGeoPos().getLat();
                    double lon = pin.getGeoPos().getLon();

                    final double hw = 0.08;
                    final double hh = 0.08;

                    final double x = lon - hw;
                    final double y = lat - hh;
                    final double w = 0.16;
                    final double h = 0.16;

                    Area area = new Area(new Rectangle2D.Double(x, y, w, h));
                    if (wholeArea == null) {
                        wholeArea = area;
                    } else {
                        wholeArea.add(area);
                    }
                }

                return wholeArea;
            } else if (model.roiSource == 2) {
                final double x = model.west;
                final double y = model.south;
                final double w = model.east - model.west;
                final double h = model.north - model.south;
                return new Area(new Rectangle2D.Double(x, y, w, h));
            }
            throw new IllegalArgumentException("roiSource must be in range [0,2], is " + model.roiSource);
        }

        private void prepareBinding() throws ValidationException {
            ValueModel roiSourceModel = vc.getModel("roiSource");
            roiSourceModel.setValue(2);
            if (product != null) {
                List<Band> roiRdns = new ArrayList<Band>();
                for (Band band : product.getBands()) {
                    if (band.isROIUsable()) {
                        ROIDefinition roiDef = band.getROIDefinition();
                        if (roiDef.isShapeEnabled() && roiDef.getShapeFigure() != null) {
                            roiRdns.add(band);
                        }
                    }
                }

                if (!product.getPinGroup().getSelectedNodes().isEmpty()) {
                    roiSourceModel.setValue(1);
                }
                if (!roiRdns.isEmpty()) {
                    ValueDescriptor roiRasterDesc = vc.getDescriptor("roiRaster");
                    roiRasterDesc.setNotNull(true);
                    roiRasterDesc.setNotEmpty(true);
                    roiRasterDesc.setValueSet(new ValueSet(roiRdns.toArray()));

                    roiSourceModel.setValue(0);
                    vc.getModel("roiRaster").setValue(roiRdns.get(0));
                }
            }
            ValueRange northSouthRange = new ValueRange(-90, 90);
            vc.getDescriptor("north").setValueRange(northSouthRange);
            vc.getDescriptor("south").setValueRange(northSouthRange);
            ValueRange eastWestRange = new ValueRange(-180, 180);
            vc.getDescriptor("east").setValueRange(eastWestRange);
            vc.getDescriptor("west").setValueRange(eastWestRange);
            ValueDescriptor outputdesc = vc.getDescriptor("output");
            outputdesc.setNotEmpty(true);
            outputdesc.setNotNull(true);
            ValueModel useOpenModel = vc.getModel("useOpenProduct");
            useOpenModel.setValue((product != null));

            bindingContext.bindEnabledState("roiRaster", true, "roiSource", 0);
            bindingContext.bindEnabledState("roiPin", true, "roiSource", 1);
        }

        private void makeUI() {
            JPanel mainPanel = new JPanel();
            BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
            mainPanel.setLayout(layout);
            mainPanel.add(createInputPanel());
            mainPanel.add(createRoiPanel());
            mainPanel.add(createOutputPanel());
            setContent(mainPanel);
        }


        private JComponent createInputPanel() {
            TableLayout layout = new TableLayout(1);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            layout.setTableWeightX(1.0);

            JPanel panel = new JPanel(layout);
            panel.setBorder(BorderFactory.createTitledBorder("Input"));
            JRadioButton useOpen = new JRadioButton("Use selected product");
            JRadioButton scanDir = new JRadioButton("Scan directory");
            Map<AbstractButton, Object> inputValueSet = new HashMap<AbstractButton, Object>();
            inputValueSet.put(useOpen, Boolean.TRUE);
            inputValueSet.put(scanDir, Boolean.FALSE);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(useOpen);
            buttonGroup.add(scanDir);
            if (product == null) {
                useOpen.setEnabled(false);
            }
            bindingContext.bind("useOpenProduct", buttonGroup, inputValueSet);
            panel.add(useOpen);
            panel.add(scanDir);
            layout.setCellPadding(2, 0, new Insets(0, 24, 3, 3));
            JCheckBox scanRecursive = new JCheckBox("Scan recursive");
            bindingContext.bind("scanRecursive", scanRecursive);
            bindingContext.bindEnabledState("scanRecursive", true, "useOpenProduct", Boolean.FALSE);
            panel.add(scanRecursive);
            layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
            bindingContext.bindEnabledState("scanDirectory", true, "useOpenProduct", Boolean.FALSE);
            panel.add(createFileSelectorComponent(vc.getDescriptor("scanDirectory"), bindingContext));
            setFileEditorWidth("scanDirectory");
            return panel;
        }

        private void setFileEditorWidth(String name) {
            JTextField textField = (JTextField) bindingContext.getBinding(
                    name).getComponentAdapter().getComponents()[0];
            textField.setColumns(30);
        }

        private JComponent createFileSelectorComponent(ValueDescriptor valueDescriptor, BindingContext bindingContext) {
            JTextField textField = new JTextField();
            ComponentAdapter adapter = new TextComponentAdapter(textField);
            final Binding binding = bindingContext.bind(valueDescriptor.getName(), adapter);
            final JPanel subPanel = new JPanel(new BorderLayout(2, 2));
            subPanel.add(textField, BorderLayout.CENTER);
            JButton etcButton = new JButton("...");
            etcButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    int i = fileChooser.showDialog(subPanel, "Select");
                    if (i == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                        binding.setPropertyValue(fileChooser.getSelectedFile());
                    }
                }
            });
            subPanel.add(etcButton, BorderLayout.EAST);
            return subPanel;
        }

        private Component createRoiPanel() {
            JRadioButton useROIButton = new JRadioButton("Shape from ROI definition of band");
            if (vc.getDescriptor("roiRaster").getValueSet() == null) {
                useROIButton.setEnabled(false);
            }
            JRadioButton usePinButton = new JRadioButton("Selected Pins");
            if (product == null || product.getPinGroup().getSelectedNodes().isEmpty()) {
                usePinButton.setEnabled(false);
            }
            JRadioButton useAreaButton = new JRadioButton("Specify area");
            Map<AbstractButton, Object> inputValueSet = new HashMap<AbstractButton, Object>();
            inputValueSet.put(useROIButton, 0);
            inputValueSet.put(usePinButton, 1);
            inputValueSet.put(useAreaButton, 2);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(useROIButton);
            buttonGroup.add(usePinButton);
            buttonGroup.add(useAreaButton);
            bindingContext.bind("roiSource", buttonGroup, inputValueSet);

            ValueEditor selectionEditor = ValueEditorRegistry.getInstance().getValueEditor(
                    SingleSelectionEditor.class.getName());
            JComboBox roiCombo = (JComboBox) selectionEditor.createEditorComponent(vc.getDescriptor("roiRaster"),
                                                                                   bindingContext);

            DefaultListCellRenderer listCellRenderer = new ProductNodeRenderer();
            roiCombo.setRenderer(listCellRenderer);

            TableLayout layout = new TableLayout(1);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            layout.setTableWeightX(1.0);

            JPanel panel = new JPanel(layout);
            panel.setBorder(BorderFactory.createTitledBorder("Region of interest"));

            panel.add(useROIButton);
            layout.setCellPadding(1, 0, new Insets(0, 24, 3, 3));
            panel.add(roiCombo);

            panel.add(usePinButton);

            panel.add(useAreaButton);
            layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
            panel.add(createLatLonPanel());

            return panel;
        }

        private Component createLatLonPanel() {
            TableLayout layout = new TableLayout(3);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            JPanel panel = new JPanel(layout);
            JLabel emptyLabel = new JLabel(" ");
            panel.add(emptyLabel);
            panel.add(createLatLonInputElement("north", "North:", 4));
            panel.add(emptyLabel);

            panel.add(createLatLonInputElement("west", "West:", 5));
            panel.add(emptyLabel);
            panel.add(createLatLonInputElement("east", "East:", 5));

            panel.add(emptyLabel);
            panel.add(createLatLonInputElement("south", "South:", 4));
            panel.add(emptyLabel);
            return panel;
        }

        private Component createLatLonInputElement(String name, String displayName, int numColumns) {
            ValueEditor textEditor = ValueEditorRegistry.getInstance().getValueEditor(TextFieldEditor.class.getName());
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel(displayName));
            JTextField editor = (JTextField) textEditor.createEditorComponent(vc.getDescriptor(name), bindingContext);
            editor.setColumns(numColumns);
            panel.add(editor);
            panel.add(new JLabel("\u00b0"));
            return panel;
        }

        private JComponent createOutputPanel() {
            TableLayout layout = new TableLayout(1);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            layout.setTableWeightX(1.0);

            JPanel panel = new JPanel(layout);
            panel.setBorder(BorderFactory.createTitledBorder("Output"));
            panel.add(new JLabel("CSV-File:"));
            ValueEditor fileEditor = ValueEditorRegistry.getInstance().getValueEditor(FileEditor.class.getName());
            panel.add(fileEditor.createEditorComponent(vc.getDescriptor("output"), bindingContext));
            setFileEditorWidth("output");
            return panel;
        }
    }

    private static class ProductNodeRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index,
                                                                             isSelected, cellHasFocus);
            if (value != null && value instanceof ProductNode) {
                label.setText(((ProductNode) value).getDisplayName());
            } else {
                label.setText(" ");
            }
            return label;
        }
    }


    private static class GridCellExportModel {

        private boolean useOpenProduct;
        private boolean scanRecursive;
        private File scanDirectory;

        private int roiSource;
        private Band roiRaster;

        private double north = 90;
        private double south = -90;
        private double east = 180;
        private double west = -180;

        private File output;
    }
}
