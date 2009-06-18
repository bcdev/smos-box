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
import com.bc.ceres.binding.swing.internal.TextFieldAdapter;
import com.bc.ceres.binding.swing.internal.TextFieldEditor;
import com.bc.ceres.swing.TableLayout;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.Pin;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;

import java.awt.BorderLayout;
import java.awt.Component;
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

/**
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 1.0
 */
public class GridCellExporterAction extends ExecCommand  {

    @Override
    public void actionPerformed(final CommandEvent event) {
        openDialog(VisatApp.getApp(), event.getCommand().getHelpId());
    }
    
    private void openDialog(VisatApp visatApp, String helpId) {
        final Product[] prods = visatApp.getProductManager().getProducts();
        GridCellExportDialog dialog = new GridCellExportDialog(visatApp, prods, helpId);
        dialog.show();
    }
    
    private static class GridCellExportDialog extends ModalDialog {
        
        private static final String LAST_DIR_KEY = "user.smos.import.dir";
        
        private final VisatApp visatApp;
        private final Product[] openProducts;
        private final ValueContainer vc;
        private final BindingContext bindingContext;
        private final GridCellExportModel model;

        GridCellExportDialog(final VisatApp visatApp, Product[] openProducts, String helpId) {
            super(visatApp.getMainFrame(), "SMOS Grid Cell Exporter", ID_OK_CANCEL_HELP, helpId); /* I18N */
            this.visatApp = visatApp;
            this.openProducts = openProducts;
            model = new GridCellExportModel();
            
            String smosDirPath = visatApp.getPreferences().getPropertyString(LAST_DIR_KEY, SystemUtils.getUserHomeDir().getPath());
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
                        + "Do you want to overwrite the existing file?", model.output.getPath());
                final int answer = JOptionPane.showConfirmDialog(getJDialog(), message,
                                                                 getTitle(), JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(model.output);
            } catch (FileNotFoundException e) {
                visatApp.showErrorDialog("Could not create CSV file :\n"+e.getMessage());
                return;
            }
            CsvGridExport csvGridExport = new CsvGridExport(printWriter);
            Area area = getArea();
            GridPointFilterStreamHandler streamHandler = new GridPointFilterStreamHandler(csvGridExport, area);
            try {
                if (model.useOpenProduct) {
                    streamHandler.processProductList(openProducts);
                } else {
                    streamHandler.processDirectory(model.scanDirectory, model.scanRecursive);
                }
            } catch (IOException e) {
                visatApp.showErrorDialog("An error occured while exporting the grid cell data:\n"+e.getMessage());
                return;
            } finally {
                try {
                    csvGridExport.close();
                } catch (IOException e) {
                    visatApp.showErrorDialog("An error occured while closing the CSV file:\n"+e.getMessage());
                    return;
                }
            }
            // TODO do in background
            super.onOK();
        }
        
        private Area getArea() {
            if (model.roiSource == 0) {
                final GeoCoding currentGeoCoding = model.roiRaster.getGeoCoding();
                Shape shape = model.roiRaster.getROIDefinition().getShapeFigure().getShape();
                GeneralPath geoPath = ProductUtils.convertToGeoPath(shape, currentGeoCoding);
                return new Area(geoPath);
            } else if (model.roiSource == 1) {
                Pin pin = model.roiPin;
                double lat = pin.getGeoPos().getLat();
                double lon = pin.getGeoPos().getLon();

                final double hw = 0.08;
                final double hh = 0.08;

                final double x = lon - hw;
                final double y = lat - hh;
                final double w = 0.16;
                final double h = 0.16;
                
                return new Area(new Rectangle2D.Double(x, y, w, h));
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
            List<Band> roiRdns = new ArrayList<Band>();
            List<Pin> roiPins = new ArrayList<Pin>();
            for (Product product : openProducts) {
                for (Band band : product.getBands()) {
                    if (band.isROIUsable()) {
                        roiRdns.add(band);
                    }
                }
                ProductNodeGroup<Pin> pinGroup = product.getPinGroup();
                for (int i = 0; i < pinGroup.getNodeCount(); i++) {
                    roiPins.add(pinGroup.get(i));
                }
            }
            ValueDescriptor roiRasterDesc = vc.getDescriptor("roiRaster");
            roiRasterDesc.setNotNull(true);
            roiRasterDesc.setNotEmpty(true);
            roiRasterDesc.setValueSet(new ValueSet(roiRdns.toArray()));
                
            ValueDescriptor roiPinDesc = vc.getDescriptor("roiPin");
            roiPinDesc.setNotNull(true);
            roiPinDesc.setNotEmpty(true);
            roiPinDesc.setValueSet(new ValueSet(roiPins.toArray()));
                
            if (!roiPins.isEmpty()) {
                roiSourceModel.setValue(1);
                vc.getModel("roiPin").setValue(roiPins.get(0));
            }
            if (!roiRdns.isEmpty()) {
                roiSourceModel.setValue(0);
                vc.getModel("roiRaster").setValue(roiRdns.get(0));
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
            useOpenModel.setValue((openProducts.length != 0));
            
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
            JRadioButton useOpen = new JRadioButton("Use currently open products");
            JRadioButton scanDir = new JRadioButton("Scan directory");
            Map<AbstractButton, Object> inputValueSet = new HashMap<AbstractButton, Object>();
            inputValueSet.put(useOpen, Boolean.TRUE);
            inputValueSet.put(scanDir, Boolean.FALSE);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(useOpen);
            buttonGroup.add(scanDir);
            if (openProducts.length == 0) {
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
            return panel;
        }
        
        private JComponent createFileSelectorComponent(ValueDescriptor valueDescriptor, BindingContext bindingContext) {
            JTextField textField = new JTextField();
            ComponentAdapter adapter = new TextFieldAdapter(textField);
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
            TableLayout layout = new TableLayout(1);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            layout.setTableWeightX(1.0);
            
            JPanel panel = new JPanel(layout);
            panel.setBorder(BorderFactory.createTitledBorder("Region of interest"));
            JRadioButton useROIButton = new JRadioButton("Shape from ROI");
            if (vc.getDescriptor("roiRaster").getValueSet().getItems().length == 0) {
                useROIButton.setEnabled(false);
            }
            JRadioButton usePinButton = new JRadioButton("Pin");
            if (vc.getDescriptor("roiPin").getValueSet().getItems().length == 0) {
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
            panel.add(useROIButton);
            layout.setCellPadding(1, 0, new Insets(0, 24, 3, 3));
            ValueEditor selectionEditor = ValueEditorRegistry.getInstance().getValueEditor(SingleSelectionEditor.class.getName());
            JComboBox roiCombo = (JComboBox) selectionEditor.createEditorComponent(vc.getDescriptor("roiRaster"), bindingContext);
            
            DefaultListCellRenderer listCellRenderer = new ProductNodeRenderer();
            roiCombo.setRenderer(listCellRenderer);
            panel.add(roiCombo);
            
            panel.add(usePinButton);
            layout.setCellPadding(3, 0, new Insets(0, 24, 3, 3));
            JComboBox pinCombo = (JComboBox) selectionEditor.createEditorComponent(vc.getDescriptor("roiPin"), bindingContext);
            pinCombo.setRenderer(listCellRenderer);
            
            panel.add(pinCombo);
            panel.add(useAreaButton);
            
            layout.setCellPadding(5, 0, new Insets(0, 24, 3, 3));
            panel.add(createLatLonPanel());
            
            return panel;
        }
        
        private Component createLatLonPanel() {
            TableLayout layout = new TableLayout(5);
            layout.setTableAnchor(TableLayout.Anchor.WEST);
            layout.setTableFill(TableLayout.Fill.HORIZONTAL);
            layout.setTablePadding(3, 3);
            layout.setColumnWeightX(0, 0.5);
            layout.setColumnWeightX(1, 1.0);
            layout.setColumnWeightX(2, 0.0);
            layout.setColumnWeightX(3, 0.5);
            layout.setColumnWeightX(4, 1.0);
            
            JPanel panel = new JPanel(layout);
            panel.setBorder(BorderFactory.createTitledBorder(""));
            layout.setCellColspan(0, 0, 5);
            panel.add(new JLabel("Latitude"));
            
            ValueEditor textEditor = ValueEditorRegistry.getInstance().getValueEditor(TextFieldEditor.class.getName());
            
            
            panel.add(new JLabel("North"));
            panel.add(textEditor.createEditorComponent(vc.getDescriptor("north"), bindingContext));
            layout.setCellWeightX(1, 2, 0.0);
            panel.add(new JLabel(""));
            panel.add(new JLabel("South"));
            panel.add(textEditor.createEditorComponent(vc.getDescriptor("south"), bindingContext));
            
            layout.setCellColspan(2, 0, 5);
            layout.setCellWeightX(2, 0, 0.0);
            panel.add(new JLabel(""));
            
            layout.setCellColspan(3, 0, 5);
            panel.add(new JLabel("Longitude"));
            
            panel.add(new JLabel("West"));
            layout.setCellWeightX(3, 2, 0.0);
            panel.add(textEditor.createEditorComponent(vc.getDescriptor("west"), bindingContext));
            panel.add(new JLabel(""));
            panel.add(new JLabel("East"));
            panel.add(textEditor.createEditorComponent(vc.getDescriptor("east"), bindingContext));

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
            panel.add(new JLabel("CSV-File"));
            ValueEditor fileEditor = ValueEditorRegistry.getInstance().getValueEditor(FileEditor.class.getName());
            panel.add(fileEditor.createEditorComponent(vc.getDescriptor("output"), bindingContext));
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
        private Pin roiPin;
        
        private double north = 90;
        private double south = -90;
        private double east = 180;
        private double west = -180;
        
        private File output;
    }
}