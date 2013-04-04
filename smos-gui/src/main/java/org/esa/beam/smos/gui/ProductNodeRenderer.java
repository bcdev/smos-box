package org.esa.beam.smos.gui;

import org.esa.beam.framework.datamodel.ProductNode;

import javax.swing.*;
import java.awt.*;

public class ProductNodeRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
