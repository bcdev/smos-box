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

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;

class SnapshotSelector {
    private final JSpinner spinner;
    private final JSlider slider;
    private final JTextField sliderInfo;

    private SnapshotSelectorModel model;

    SnapshotSelector() {
        spinner = new JSpinner();
        final JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(8);
        }

        slider = new JSlider();
        sliderInfo = new JTextField(10);
        sliderInfo.setEditable(false);
    }

    SnapshotSelector(SnapshotSelectorModel model) {
        this();
        setModel(model);
    }

    final void setModel(SnapshotSelectorModel model) {
        if (this.model != model) {
            this.model = model;
            spinner.setModel(model.getSpinnerModel());
            slider.setModel(model.getSliderModel());
            sliderInfo.setDocument(model.getSliderInfoDocument());
        }
    }

    JSpinner getSpinner() {
        return spinner;
    }

    JSlider getSlider() {
        return slider;
    }

    JTextField getSliderInfo() {
        return sliderInfo;
    }
}
