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
import javax.swing.event.ChangeListener;

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

    JTextField getSliderInfoField() {
        return snapshotSelector.getSliderInfo();
    }

    final void setModel(SnapshotSelectorComboModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (this.model != model) {
            this.model = model;
            comboBox.setModel(model.getComboBoxModel());
            snapshotSelector.setModel(model.getSelectedSnapshotSelectorModel());
        }
    }

    private class SnapshotSelectorComboBox extends JComboBox {
        @Override
        public void setSelectedItem(Object object) {
            super.setSelectedItem(object);

            if (model != null) {
                snapshotSelector.setModel(model.getSelectedSnapshotSelectorModel());
            }
        }
    }
}
