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

import junit.framework.TestCase;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.BorderLayout;

public class SnapshotSelectorComboTest extends TestCase {
    private SnapshotSelectorCombo selectorCombo;

    @Override
    protected void setUp() throws Exception {
        selectorCombo = createSnapshotSelectorCombo();
    }

    public void testInitialState() {
        assertEquals(1, selectorCombo.getSpinner().getValue());
        assertEquals(0, selectorCombo.getSlider().getValue());
        assertEquals("Any", selectorCombo.getComboBox().getSelectedItem());
    }

    public static void main(String[] args) {
        final SnapshotSelectorCombo selectorCombo = createSnapshotSelectorCombo();

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(selectorCombo.getSpinner(), BorderLayout.WEST);
        panel.add(selectorCombo.getSlider(), BorderLayout.CENTER);
        panel.add(selectorCombo.getSliderInfoField(), BorderLayout.EAST);
        panel.add(selectorCombo.getComboBox(), BorderLayout.SOUTH);

        final JFrame frame = new JFrame();
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private static SnapshotSelectorCombo createSnapshotSelectorCombo() {
        return new SnapshotSelectorCombo(SnapshotSelectorComboModelTest.createSnapshotSelectorComboModel());
    }
}
