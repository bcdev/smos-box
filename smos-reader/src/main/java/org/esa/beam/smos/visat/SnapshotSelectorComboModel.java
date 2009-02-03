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

import org.esa.beam.dataio.smos.SnapshotProvider;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class SnapshotSelectorComboModel {
    private final ComboBoxModel comboBoxModel;
    private final Map<String, SnapshotSelectorModel> map;

    SnapshotSelectorComboModel(SnapshotProvider provider) {
        map = new HashMap<String, SnapshotSelectorModel>();

        map.put("Any", new SnapshotSelectorModel(provider.getAllSnapshotIds()));
        map.put("X", new SnapshotSelectorModel(provider.getXPolSnapshotIds()));
        map.put("Y", new SnapshotSelectorModel(provider.getYPolSnapshotIds()));

        if (provider.getCrossPolSnapshotIds().length != 0) {
            map.put("XY", new SnapshotSelectorModel(provider.getCrossPolSnapshotIds()));
            comboBoxModel = new DefaultComboBoxModel(new String[]{"Any", "X", "Y", "XY"});
        } else {
            comboBoxModel = new DefaultComboBoxModel(new String[]{"Any", "X", "Y"});
        }
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    SnapshotSelectorModel getSelectedSnapshotSelectorModel() {
        return map.get(comboBoxModel.getSelectedItem());
    }

    ComboBoxModel getComboBoxModel() {
        return comboBoxModel;
    }
}
