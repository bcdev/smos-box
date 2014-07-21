/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.beam.smos.visat.swing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JFrame;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SnapshotSelectorComboTest {

    private SnapshotSelectorCombo selectorCombo;

    @Before
    public void before() throws Exception {
        selectorCombo = createSnapshotSelectorCombo();
    }

    @After
    public void after() throws Exception {
        selectorCombo = null;
    }

    @Test
    public void initialState() {
        assertEquals(1L, selectorCombo.getSpinner().getValue());
        assertEquals(0L, selectorCombo.getSlider().getValue());
        assertEquals("Any", selectorCombo.getComboBox().getSelectedItem());
    }

    @Test
    public void spinnerEditor() throws ParseException {
        final JSpinner spinner = selectorCombo.getSpinner();
        assertTrue(spinner != null);
        
        final JComponent component = spinner.getEditor();
        assertTrue(component instanceof JSpinner.ListEditor);

        final JSpinner.ListEditor listEditor = (JSpinner.ListEditor) component;
        final JFormattedTextField textField = listEditor.getTextField();
        assertTrue(textField.isEditable());

        textField.setValue(2);
        assertEquals(2L, spinner.getValue());
    }

    private static SnapshotSelectorCombo createSnapshotSelectorCombo() {
        return new SnapshotSelectorCombo(SnapshotSelectorComboModelTest.createSnapshotSelectorComboModel());
    }

    public static void main(String[] args) {
        final SnapshotSelectorCombo combo = createSnapshotSelectorCombo();
        final JFrame frame = new JFrame();
        frame.add(SnapshotSelectorCombo.createComponent(combo, false));
        frame.pack();
        frame.setVisible(true);
    }

}
