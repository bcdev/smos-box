package org.esa.beam.smos.gui;


import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.internal.AbstractButtonAdapter;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GuiHelper {

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
}
