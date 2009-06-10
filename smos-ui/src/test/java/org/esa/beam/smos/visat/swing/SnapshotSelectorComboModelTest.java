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

import junit.framework.TestCase;
import org.esa.beam.dataio.smos.SnapshotProvider;

public class SnapshotSelectorComboModelTest extends TestCase {
    private SnapshotSelectorComboModel selectorComboModel;

    @Override
    protected void setUp() throws Exception {
        selectorComboModel = createSnapshotSelectorComboModel();
    }

    public void testInitialState() {
        assertEquals(3, selectorComboModel.getSize());
        assertEquals("Any", selectorComboModel.getSelectedItem());
    }

    public void testSnapshotSelectorModelSelection() {
        final SnapshotSelectorModel modelA = selectorComboModel.getSelectedModel();
        selectorComboModel.setSelectedItem("X");
        final SnapshotSelectorModel modelX = selectorComboModel.getSelectedModel();
        selectorComboModel.setSelectedItem("Y");
        final SnapshotSelectorModel modelY = selectorComboModel.getSelectedModel();

        assertNotSame(modelA, modelX);
        assertNotSame(modelA, modelY);
        assertNotSame(modelX, modelY);
    }

    public void testSnapshotIdIsPreservedIfPossible() {
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
        final SnapshotProvider provider = new SnapshotProvider() {
            @Override
            public Long[] getAllSnapshotIds() {
                return new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L};
            }

            @Override
            public Long[] getXPolSnapshotIds() {
                return new Long[]{1L, 3L, 5L, 7L};
            }

            @Override
            public Long[] getYPolSnapshotIds() {
                return new Long[]{2L, 4L, 6L, 8L};
            }

            @Override
            public Long[] getCrossPolSnapshotIds() {
                return new Long[0];
            }
        };

        return new SnapshotSelectorComboModel(provider);
    }
}
