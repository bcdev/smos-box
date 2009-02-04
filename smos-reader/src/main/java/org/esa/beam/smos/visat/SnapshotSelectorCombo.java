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
package org.esa.beam.smos.visat;

import javax.swing.*;
import java.awt.BorderLayout;

class SnapshotSelectorCombo {
    private final JComboBox comboBox;
    private final SnapshotSelector snapshotSelector;
    private SnapshotSelectorComboModel model;

    SnapshotSelectorCombo() {
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

    boolean isAdjusting() {
        return getSlider().getValueIsAdjusting();
    }

    long getSnapshotId() {
        return (Long) getSpinner().getValue();
    }

    void setSnapshotId(long id) {
        getSpinner().setValue(id);
    }

    final void setModel(SnapshotSelectorComboModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (this.model != model) {
            this.model = model;
            comboBox.setModel(model);
            snapshotSelector.setModel(model.getSelectedModel());
        }
    }

    static JComponent createComponent(SnapshotSelectorCombo combo, boolean showSliderInfo) {
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
        component.setBorder(BorderFactory.createTitledBorder("Snapshot Selection"));
        
        return component;
    }

    private class SnapshotSelectorComboBox extends JComboBox {
        @Override
        public void setSelectedItem(Object object) {
            super.setSelectedItem(object);

            if (model != null) {
                snapshotSelector.setModel(model.getSelectedModel());
            }
        }
    }
}
