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

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

class SnapshotSelectorModel {
    private final SpinnerListModel spinnerModel;
    private final DefaultBoundedRangeModel sliderModel;
    private final PlainDocument sliderInfoDocument;

    SnapshotSelectorModel(Integer[] snapshotIds) {
        spinnerModel = new SliderModelAwareSpinnerModel(snapshotIds);
        sliderModel = new SpinnerModelAwareSliderModel(0, 0, 0, snapshotIds.length - 1);
        sliderInfoDocument = new PlainDocument();
        updateSliderInfoDocument();
    }

    SpinnerModel getSpinnerModel() {
        return spinnerModel;
    }

    BoundedRangeModel getSliderModel() {
        return sliderModel;
    }

    Document getSliderInfoDocument() {
        return sliderInfoDocument;
    }

    private final class SliderModelAwareSpinnerModel extends SpinnerListModel {
        private SliderModelAwareSpinnerModel(Integer[] snapshotIds) {
            super(snapshotIds);
        }

        @Override
        public void setValue(Object spinnerValue) {
            if (super.getValue().equals(spinnerValue)) {
                return;
            }
            super.setValue(spinnerValue);

            // this cast is safe by construction of the {@code SnapshotSpinnerModel}
            @SuppressWarnings("unchecked")
            final List<Integer> list = (List<Integer>) getList();
            final int sliderValue = Collections.binarySearch(list, (Integer) spinnerValue);

            sliderModel.setValue(sliderValue);
        }
    }

    private final class SpinnerModelAwareSliderModel extends DefaultBoundedRangeModel {
        private SpinnerModelAwareSliderModel(int value, int extent, int min, int max) {
            super(value, extent, min, max);
        }

        @Override
        public void setValue(int sliderValue) {
            if (super.getValue() == sliderValue) {
                return;
            }
            super.setValue(sliderValue);
            updateSliderInfoDocument();

            final Object spinnerValue = spinnerModel.getList().get(sliderValue);
            spinnerModel.setValue(spinnerValue);
        }
    }

    private void updateSliderInfoDocument() {
        final String text = createSliderInfoText(sliderModel);
        try {
            sliderInfoDocument.replace(0, sliderInfoDocument.getLength(), text, null);
        } catch (BadLocationException e) {
            // cannot happen since the position within the document is always valid
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static String createSliderInfoText(BoundedRangeModel sliderModel) {
        return MessageFormat.format("{0} / {1}", sliderModel.getValue() + 1, sliderModel.getMaximum() + 1);
    }
}
