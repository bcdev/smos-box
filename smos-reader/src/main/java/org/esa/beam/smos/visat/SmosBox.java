package org.esa.beam.smos.visat;

import com.bc.ceres.glayer.Layer;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.VisatPlugIn;
import org.esa.beam.smos.worldmap.SmosWorldMapLayer;

public class SmosBox implements VisatPlugIn {
    private static SmosBox instance;
    private SnapshotSelectionService snapshotSelectionService;
    private GridPointSelectionService gridPointSelectionService;
    private SceneViewSelectionService sceneViewSelectionService;

    public SmosBox() {
    }

    public static SmosBox getInstance() {
        return instance;
    }

    public SnapshotSelectionService getSnapshotSelectionService() {
        return snapshotSelectionService;
    }

    public GridPointSelectionService getGridPointSelectionService() {
        return gridPointSelectionService;
    }

    public SceneViewSelectionService getSmosViewSelectionService() {
        return sceneViewSelectionService;
    }

    @Override
    public void start(VisatApp visatApp) {
        instance = this;
        sceneViewSelectionService = new SceneViewSelectionService(visatApp);
        snapshotSelectionService = new SnapshotSelectionService(visatApp.getProductManager());
        gridPointSelectionService = new GridPointSelectionService();

        sceneViewSelectionService.addSceneViewSelectionListener(new SceneViewSelectionService.SelectionListener() {
            @Override
            public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                if (newView != null) {
                    Layer rootLayer = newView.getRootLayer();
                    if (!SmosWorldMapLayer.hasWorldMapChildLayer(rootLayer)) {
                        Layer worldLayer = SmosWorldMapLayer.createWorldMapLayer();
                        if (worldLayer != null) {
                            rootLayer.getChildren().add(worldLayer);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void stop(VisatApp visatApp) {
        sceneViewSelectionService.stop();
        sceneViewSelectionService = null;
        snapshotSelectionService.stop();
        snapshotSelectionService = null;
        gridPointSelectionService.stop();
        gridPointSelectionService = null;
        instance = null;
    }

    @Override
    public void updateComponentTreeUI() {
    }

}
