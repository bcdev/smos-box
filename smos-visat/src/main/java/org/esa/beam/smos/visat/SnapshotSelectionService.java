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

import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.ui.product.ProductSceneView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SnapshotSelectionService {

    private final SceneViewSelectionService smosViewSelectionService;
    private final List<SelectionListener> selectionListenerList;
    private final Map<RasterDataNode, Long> snapshotIdMap;
    private final SceneViewSelectionService.SelectionListener smosViewSelectionListener;

    public SnapshotSelectionService(SceneViewSelectionService smosViewSelectionService) {
        this.smosViewSelectionService = smosViewSelectionService;
        this.selectionListenerList = new ArrayList<SelectionListener>();
        this.snapshotIdMap = new WeakHashMap<RasterDataNode, Long>();
        this.smosViewSelectionListener = new SceneViewSelectionService.SelectionListener() {
            @Override
            public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                if (SmosBox.isL1cScienceSmosView(newView)) {
                    synchronized (snapshotIdMap) {
                        final Long newId = snapshotIdMap.get(newView.getRaster());
                        if (newId != null) {
                            fireSelectionChange(newView, newId);
                        } else {
                            fireSelectionChange(newView, -1);
                        }
                    }
                }
            }
        };
        this.smosViewSelectionService.addSceneViewSelectionListener(smosViewSelectionListener);
    }

    public final synchronized void stop() {
        snapshotIdMap.clear();
        selectionListenerList.clear();
        smosViewSelectionService.removeSceneViewSelectionListener(smosViewSelectionListener);
    }

    public final long getSelectedSnapshotId(RasterDataNode raster) {
        final Long id;
        synchronized (snapshotIdMap) {
            id = snapshotIdMap.get(raster);
        }
        if (id != null) {
            return id;
        }
        return -1;
    }

    public void setSelectedSnapshotId(RasterDataNode raster, long id) {
        if (SmosBox.isL1cScienceSmosRaster(raster)) {
            if (id >= 0) {
                synchronized (snapshotIdMap) {
                    snapshotIdMap.put(raster, id);
                }
            } else {
                synchronized (snapshotIdMap) {
                    snapshotIdMap.remove(raster);
                }
            }
        }
    }

    public synchronized void addSnapshotIdChangeListener(SelectionListener selectionListener) {
        selectionListenerList.add(selectionListener);
    }

    public synchronized void removeSnapshotIdChangeListener(SelectionListener selectionListener) {
        selectionListenerList.remove(selectionListener);
    }

    private void fireSelectionChange(ProductSceneView newView, long newId) {
        final SelectionListener[] listeners;

        synchronized (selectionListenerList) {
            listeners = selectionListenerList.toArray(new SelectionListener[selectionListenerList.size()]);
        }

        for (final SelectionListener listener : listeners) {
            listener.handleSnapshotIdChanged(newView, newId);
        }
    }

    public interface SelectionListener {

        void handleSnapshotIdChanged(ProductSceneView newView, long newId);
    }
}
