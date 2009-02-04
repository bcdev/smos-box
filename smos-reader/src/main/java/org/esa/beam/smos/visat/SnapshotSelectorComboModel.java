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
import javax.swing.event.ListDataListener;
import java.util.HashMap;
import java.util.Map;

class SnapshotSelectorComboModel implements ComboBoxModel {
    private final ComboBoxModel comboBoxModel;
    private final Map<Object, SnapshotSelectorModel> map;

    SnapshotSelectorComboModel(SnapshotProvider provider) {
        map = new HashMap<Object, SnapshotSelectorModel>();

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
    SnapshotSelectorModel getSelectedModel() {
        return map.get(comboBoxModel.getSelectedItem());
    }

    @Override
    public void setSelectedItem(Object newItem) {
        final Object oldItem = getSelectedItem();
        final SnapshotSelectorModel oldModel = map.get(oldItem);
        final SnapshotSelectorModel newModel = map.get(newItem);

        try {
            newModel.setSnapshotId(oldModel.getSnapshotId());
        } catch (IllegalArgumentException e) {
            // the value of the old model is not valid for the new model, so
            // the new model keeps its old value
        }

        comboBoxModel.setSelectedItem(newItem);
    }

    @Override
    public Object getSelectedItem() {
        return comboBoxModel.getSelectedItem();
    }

    @Override
    public int getSize() {
        return comboBoxModel.getSize();
    }

    @Override
    public Object getElementAt(int index) {
        return comboBoxModel.getElementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        comboBoxModel.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        comboBoxModel.removeListDataListener(l);
    }
}
