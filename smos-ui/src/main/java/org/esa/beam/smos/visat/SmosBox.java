package org.esa.beam.smos.visat;

import com.bc.ceres.glayer.Layer;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.smos.worldmap.SmosWorldMapLayer;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.VisatPlugIn;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.SmosFile;

public class SmosBox implements VisatPlugIn {
    private static volatile SmosBox instance;

    private volatile SnapshotSelectionService snapshotSelectionService;
    private volatile GridPointSelectionService gridPointSelectionService;
    private volatile SceneViewSelectionService sceneViewSelectionService;

    public static SmosBox getInstance() {
        return instance;
    }

    public final SnapshotSelectionService getSnapshotSelectionService() {
        return snapshotSelectionService;
    }

    public final GridPointSelectionService getGridPointSelectionService() {
        return gridPointSelectionService;
    }

    public final SceneViewSelectionService getSmosViewSelectionService() {
        return sceneViewSelectionService;
    }

    @Override
    public final void start(VisatApp visatApp) {
        synchronized (this) {
            instance = this;
            sceneViewSelectionService = new SceneViewSelectionService(visatApp);
            snapshotSelectionService = new SnapshotSelectionService(sceneViewSelectionService);
            gridPointSelectionService = new GridPointSelectionService();

            sceneViewSelectionService.addSceneViewSelectionListener(new SceneViewSelectionService.SelectionListener() {
                @Override
                public void handleSceneViewSelectionChanged(ProductSceneView oldView, ProductSceneView newView) {
                    if (newView != null) {
                        final Layer rootLayer = newView.getRootLayer();
                        if (!SmosWorldMapLayer.hasWorldMapChildLayer(rootLayer)) {
                            final Layer worldMapLayer = SmosWorldMapLayer.createWorldMapLayer();
                            if (worldMapLayer != null) {
                                rootLayer.getChildren().add(worldMapLayer);
                                worldMapLayer.setVisible(true);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public final void stop(VisatApp visatApp) {
        synchronized (this) {
            sceneViewSelectionService.stop();
            sceneViewSelectionService = null;
            snapshotSelectionService.stop();
            snapshotSelectionService = null;
            gridPointSelectionService.stop();
            gridPointSelectionService = null;
            instance = null;
        }
    }

    @Override
    public final void updateComponentTreeUI() {
    }

    static boolean isL1cScienceSmosView(ProductSceneView smosView) {
        return getL1cScienceSmosFile(smosView) != null;
    }

    static L1cScienceSmosFile getL1cScienceSmosFile(ProductSceneView smosView) {
        if (smosView != null) {
            RasterDataNode raster = smosView.getRaster();
            if (raster != null) {
                final ProductReader productReader = raster.getProductReader();
                if (productReader instanceof SmosProductReader) {
                    final SmosFile smosFile = ((SmosProductReader) productReader).getSmosFile();
                    if (smosFile instanceof L1cScienceSmosFile) {
                        return (L1cScienceSmosFile) smosFile;
                    }
                }
            }
        }
        return null;
    }
}
