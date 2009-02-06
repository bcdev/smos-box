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

class SnapshotSelector {
    private final JSpinner spinner;
    private final JSlider slider;
    private final JTextField sliderInfo;

    private SnapshotSelectorModel model;

    SnapshotSelector() {
        spinner = new JSpinner();
        slider = new JSlider();
        slider.setSnapToTicks(true);
        sliderInfo = new JTextField(10);
        sliderInfo.setEditable(false);
    }

    SnapshotSelector(SnapshotSelectorModel model) {
        this();
        setModel(model);
    }

    /**
     * Returns the spinner component.
     * <p/>
     * Note that registered <code>ChangeListener</code>s  are notified
     * each time a <code>ChangeEvent</code> is received from the model
     * <em>or</em> the model is changed.
     *
     * @return the spinner component.
     */
    JSpinner getSpinner() {
        return spinner;
    }

    /**
     * Returns the slider component.
     * <p/>
     * Note that registered <code>ChangeListener</code>s  are notified
     * each time a <code>ChangeEvent</code> is received from the model
     * <em>or</em> the model is changed.
     *
     * @return the slider component.
     */
    JSlider getSlider() {
        return slider;
    }

    JTextField getSliderInfo() {
        return sliderInfo;
    }

    final void setModel(SnapshotSelectorModel model) {
        if (model == null) {
            throw new IllegalArgumentException("null model");
        }
        if (this.model != model) {
            this.model = model;
            spinner.setModel(model.getSpinnerModel());
            slider.setModel(model.getSliderModel());
            sliderInfo.setDocument(model.getSliderInfoDocument());
        }
    }
}
