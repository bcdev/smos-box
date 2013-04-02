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

package org.esa.beam.smos.visat;

import java.util.ArrayList;
import java.util.List;

public class GridPointSelectionService {
    private final List<SelectionListener> selectionListeners;
    private int selectedPointId;

    public GridPointSelectionService() {
        this.selectionListeners = new ArrayList<SelectionListener>();
        this.selectedPointId = -1;
    }

    public synchronized void stop() {
        selectionListeners.clear();
        selectedPointId = -1;
    }

    public synchronized int getSelectedGridPointId() {
        return selectedPointId;
    }

    public synchronized void setSelectedGridPointId(int id) {
        int oldId = this.selectedPointId;
        if (oldId != id) {
            this.selectedPointId = id;
            fireSelectionChange(oldId, id);
        }
    }

    public synchronized void addGridPointSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    public synchronized void removeGridPointSelectionListener(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    private void fireSelectionChange(int oldId, int newId) {
        for (SelectionListener selectionListener : selectionListeners) {
            selectionListener.handleGridPointSelectionChanged(oldId, newId);
        }
    }

    public interface SelectionListener {
        void handleGridPointSelectionChanged(int oldId, int newId);
    }
}