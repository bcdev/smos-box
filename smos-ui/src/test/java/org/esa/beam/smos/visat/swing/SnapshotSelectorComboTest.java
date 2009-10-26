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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JFrame;
import java.text.ParseException;

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
