package org.esa.beam.smos.visat;

import org.esa.beam.framework.ui.product.ProductSceneView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SnapshotSelectionService {
    private final SceneViewSelectionService smosViewSelectionService;
    private final List<SelectionListener> selectionListenerList;
    private final Map<ProductSceneView, Long> snapshotIdMap;
    private final SceneViewSelectionService.SelectionListener smosViewSelectionListener;

    public SnapshotSelectionService(SceneViewSelectionService smosViewSelectionService) {
        this.smosViewSelectionService = smosViewSelectionService;
        this.selectionListenerList = new ArrayList<SelectionListener>();
        this.snapshotIdMap = new WeakHashMap<ProductSceneView, Long>();
        this.smosViewSelectionListener = new SceneViewSelectionService.SelectionListener() {
            @Override
            public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                if (SmosBox.isL1cScienceSmosView(newView)) {
                    synchronized (snapshotIdMap) {
                        final Long newId = snapshotIdMap.get(newView);
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

    public final long getSelectedSnapshotId(ProductSceneView view) {
        final Long id;
        synchronized (snapshotIdMap) {
            id = snapshotIdMap.get(view);
        }
        if (id != null) {
            return id;
        }
        return -1;
    }

    public void setSelectedSnapshotId(ProductSceneView view, long id) {
        if (SmosBox.isL1cScienceSmosView(view)) {
            if (id >= 0) {
                synchronized (snapshotIdMap) {
                    snapshotIdMap.put(view, id);
                }
            } else {
                synchronized (snapshotIdMap) {
                    snapshotIdMap.remove(view);
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