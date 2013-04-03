package org.esa.beam.smos.gui;


import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.internal.AbstractButtonAdapter;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import org.esa.beam.framework.datamodel.PlainFeatureFactory;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiHelper {

    public static final String LAST_SOURCE_DIR_KEY = "org.esa.beam.smos.export.sourceDir";

    public static JPanel createPanelWithBoxLayout() {
        final JPanel mainPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(layout);
        return mainPanel;
    }

    public static TableLayout createTableLayout(int columnCount) {
        final TableLayout layout = new TableLayout(columnCount);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        return layout;
    }

    public static TableLayout createWeightedTablelayout(int columnCount) {
        final TableLayout layout = createTableLayout(columnCount);
        layout.setTableWeightX(1.0);
        return layout;
    }

    public static void addSourceProductsButtons(JPanel sourceProductPanel, boolean canProductSelectionBeEnabled, BindingContext bindingContext) {
        final JRadioButton useSelectedProductButton = new JRadioButton("Use selected SMOS product");
        final JRadioButton useAllProductsInDirectoryButton = new JRadioButton("Use all SMOS products in directory:");

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useSelectedProductButton);
        buttonGroup.add(useAllProductsInDirectoryButton);

        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<AbstractButton, Object>();
        buttonGroupValueSet.put(useSelectedProductButton, true);
        buttonGroupValueSet.put(useAllProductsInDirectoryButton, false);

        bindingContext.bind("useSelectedProduct", buttonGroup, buttonGroupValueSet);
        bindingContext.bindEnabledState(BindingConstants.SOURCE_DIRECTORY, true, BindingConstants.SELECTED_PRODUCT, false);
        bindingContext.bindEnabledState(BindingConstants.OPEN_FILE_DIALOG, true, BindingConstants.SELECTED_PRODUCT, false);

        useSelectedProductButton.setEnabled(canProductSelectionBeEnabled);

        sourceProductPanel.add(useSelectedProductButton);
        sourceProductPanel.add(useAllProductsInDirectoryButton);
    }

    public static JComponent createFileEditorComponent(PropertyDescriptor descriptor, final ChooserFactory cf, BindingContext bindingContext) {
        final JTextField textField = new JTextField();
        textField.setColumns(30);
        final ComponentAdapter adapter = new TextComponentAdapter(textField);
        final Binding binding = bindingContext.bind(descriptor.getName(), adapter);

        final JButton etcButton = new JButton("...");
        final Dimension size = new Dimension(26, 16);
        etcButton.setPreferredSize(size);
        etcButton.setMinimumSize(size);
        bindingContext.bind(BindingConstants.OPEN_FILE_DIALOG, new AbstractButtonAdapter(etcButton));

        final JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(textField, BorderLayout.CENTER);
        panel.add(etcButton, BorderLayout.EAST);

        etcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = cf.createChooser((File) binding.getPropertyValue());
                final int state = fileChooser.showDialog(panel, "Select");
                if (state == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                    binding.setPropertyValue(fileChooser.getSelectedFile());
                }
            }
        });

        return panel;
    }

    public static File getDefaultSourceDirectory(AppContext appContext) {
        final String def = System.getProperty("user.home", ".");
        return new File(appContext.getPreferences().getPropertyString(LAST_SOURCE_DIR_KEY, def));
    }

    public static java.util.List<VectorDataNode> getGeometries(Product selectedProduct) {
        final java.util.List<VectorDataNode> geometryNodeList = new ArrayList<VectorDataNode>();
        final ProductNodeGroup<VectorDataNode> vectorDataGroup = selectedProduct.getVectorDataGroup();
        for (VectorDataNode node : vectorDataGroup.toArray(new VectorDataNode[vectorDataGroup.getNodeCount()])) {
            if (node.getFeatureType().getTypeName().equals(PlainFeatureFactory.DEFAULT_TYPE_NAME)) {
                if (!node.getFeatureCollection().isEmpty()) {
                    geometryNodeList.add(node);
                }
            }
        }
        return geometryNodeList;
    }

    public static void bindGeometries(java.util.List<VectorDataNode> geometryNodeList, PropertyContainer propertyContainer) throws ValidationException {
        final PropertyDescriptor descriptor = propertyContainer.getDescriptor(BindingConstants.GEOMETRY);
        descriptor.setNotNull(true);
        descriptor.setNotEmpty(true);
        descriptor.setValueSet(new ValueSet(geometryNodeList.toArray()));

        propertyContainer.setValue(BindingConstants.ROI_TYPE, 0);
        propertyContainer.getProperty(BindingConstants.GEOMETRY).setValue(geometryNodeList.get(0));
    }
}
