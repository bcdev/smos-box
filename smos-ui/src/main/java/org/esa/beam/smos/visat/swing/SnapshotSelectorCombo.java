/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
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
package org.esa.beam.smos.visat.swing;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

public class SnapshotSelectorCombo {

    private final JComboBox comboBox;
    private final SnapshotSelector snapshotSelector;
    private SnapshotSelectorComboModel model;

    public SnapshotSelectorCombo() {
        comboBox = new SnapshotSelectorComboBox();
        snapshotSelector = new SnapshotSelector();

        comboBox.setEditable(false);
    }

    SnapshotSelectorCombo(final SnapshotSelectorComboModel model) {
        this();
        setModel(model);
    }

    JComboBox getComboBox() {
        return comboBox;
    }

    JSpinner getSpinner() {
        return snapshotSelector.getSpinner();
    }

    JSlider getSlider() {
        return snapshotSelector.getSlider();
    }

    JTextField getSliderInfo() {
        return snapshotSelector.getSliderInfo();
    }

    public void addSliderChangeListener(ChangeListener l) {
        getSlider().addChangeListener(l);
    }

    public boolean isAdjusting() {
        return getSlider().getValueIsAdjusting();
    }

    public long getSnapshotId() {
        return (Long) getSpinner().getValue();
    }

    public void setSnapshotId(long id) {
        getSpinner().setValue(id);
    }

    final public void setModel(SnapshotSelectorComboModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (this.model != model) {
            this.model = model;
            comboBox.setModel(model);
            snapshotSelector.setModel(model.getSelectedModel());
        }
    }

    public void setComboBoxEnabled(boolean enabled) {
        getComboBox().setEnabled(enabled);
    }

    public void setComboBoxSelectedIndex(int index) {
        getComboBox().setSelectedIndex(index);
    }

    public void addComboBoxActionListener(ActionListener l) {
        getComboBox().addActionListener(l);
    }

    public static JComponent createComponent(SnapshotSelectorCombo combo, boolean showSliderInfo) {
        final JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(new JLabel("ID: "), BorderLayout.WEST);
        westPanel.add(combo.getSpinner(), BorderLayout.EAST);

        final JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(combo.getSlider(), BorderLayout.CENTER);
        if (showSliderInfo) {
            centerPanel.add(combo.getSliderInfo(), BorderLayout.EAST);
        }

        final JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(new JLabel("Mode: "), BorderLayout.WEST);
        eastPanel.add(combo.getComboBox(), BorderLayout.EAST);

        final JComponent component = new JPanel(new BorderLayout(4, 4));
        component.add(westPanel, BorderLayout.WEST);
        component.add(centerPanel, BorderLayout.CENTER);
        component.add(eastPanel, BorderLayout.EAST);

        return component;
    }

    private class SnapshotSelectorComboBox extends JComboBox {

        @Override
        public void setSelectedItem(Object object) {
            if (model != null) {
                snapshotSelector.setModel(model.getModel(object));
            }
            super.setSelectedItem(object);
        }
    }
}
