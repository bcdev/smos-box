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
import static junit.framework.Assert.assertNotSame;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;

public class SnapshotSelectorComboModelTest {

    private SnapshotSelectorComboModel selectorComboModel;

    @Before
    public void before() throws Exception {
        selectorComboModel = createSnapshotSelectorComboModel();
    }

    @Test
    public void initialState() {
        assertEquals(3, selectorComboModel.getSize());
        assertEquals("Any", selectorComboModel.getSelectedItem());
    }

    @Test
    public void snapshotSelectorModelSelection() {
        final SnapshotSelectorModel modelA = selectorComboModel.getSelectedModel();
        selectorComboModel.setSelectedItem("X");
        final SnapshotSelectorModel modelX = selectorComboModel.getSelectedModel();
        selectorComboModel.setSelectedItem("Y");
        final SnapshotSelectorModel modelY = selectorComboModel.getSelectedModel();

        assertNotSame(modelA, modelX);
        assertNotSame(modelA, modelY);
        assertNotSame(modelX, modelY);
    }

    @Test
    public void isSnapshotIdPreservedIfPossible() {
        final SnapshotSelectorModel modelA = selectorComboModel.getSelectedModel();
        modelA.setSnapshotId(3);

        // snapshot ID can be preserved
        selectorComboModel.setSelectedItem("X");
        final SnapshotSelectorModel modelX = selectorComboModel.getSelectedModel();
        assertEquals(3, modelX.getSnapshotId());

        // snapshot ID cannot be preserved
        selectorComboModel.setSelectedItem("Y");
        final SnapshotSelectorModel modelY = selectorComboModel.getSelectedModel();
        assertEquals(2, modelY.getSnapshotId());

        // snapshot ID can be preserved
        selectorComboModel.setSelectedItem("Any");
        assertEquals(2, modelA.getSnapshotId());
    }

    static SnapshotSelectorComboModel createSnapshotSelectorComboModel() {
        return new SnapshotSelectorComboModel(new SnapshotInfo(
                Collections.<Long, Integer>emptyMap(),
                Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L),
                Arrays.asList(1L, 3L, 5L, 7L),
                Arrays.asList(2L, 4L, 6L, 8L),
                Arrays.<Long>asList(),
                Collections.<Long, Rectangle2D>emptyMap()));
    }
}
