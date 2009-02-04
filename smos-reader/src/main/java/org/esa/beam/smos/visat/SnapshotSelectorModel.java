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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

class SnapshotSelectorModel {
    private final SpinnerListModel spinnerModel;
    private final BoundedRangeModel sliderModel;
    private final PlainDocument sliderInfoDocument;

    SnapshotSelectorModel(Integer[] snapshotIds) {
        spinnerModel = new SpinnerListModel(snapshotIds);
        sliderModel = new DefaultBoundedRangeModel(0, 0, 0, snapshotIds.length - 1);
        sliderInfoDocument = new PlainDocument();

        spinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final int sliderValue = Collections.binarySearch(getSpinnerModelList(), spinnerModel.getValue());
                sliderModel.setValue(sliderValue);
            }
        });
        sliderModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSliderInfoDocument();
                final Object spinnerValue = getSpinnerModelList().get(sliderModel.getValue());
                spinnerModel.setValue(spinnerValue);
            }
        });
        updateSliderInfoDocument();
    }

    /**
     * Returns the current snapshot ID.
     *
     * @return the current snapshot ID.
     */
    int getSnapshotId() {
        return (Integer) spinnerModel.getValue();
    }

    /**
     * Sets the current snapshot ID to a new value.
     *
     * @param id the new value.
     *
     * @throws IllegalArgumentException if the specified ID isn't allowed
     */
    void setSnapshotId(int id) {
        spinnerModel.setValue(id);
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

    @SuppressWarnings({"unchecked"})
    private List<? extends Comparable<? super Object>> getSpinnerModelList() {
        // the cast made here is safe by construction of the {@code spinnerModel}
        return (List<? extends Comparable<? super Object>>) spinnerModel.getList();
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
