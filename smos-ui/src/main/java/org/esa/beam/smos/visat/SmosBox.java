package org.esa.beam.smos.visat;

import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.VisatPlugIn;
import org.esa.beam.worldmap.BlueMarbleLayerType;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;

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
                        final LayerType layerType = LayerTypeRegistry.getLayerType(BlueMarbleLayerType.class.getName());
                        if (!hasLayer(rootLayer, layerType)) {
                            final PropertySet configuration = layerType.createLayerConfig(null);
                            final Layer worldMapLayer = layerType.createLayer(null, configuration);
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

    static boolean isL1cScienceSmosRaster(RasterDataNode raster) {
        return getL1cScienceSmosFile(raster) != null;
    }

    static boolean isL1cScienceSmosView(ProductSceneView smosView) {
        return getL1cScienceSmosFile(smosView) != null;
    }

    static L1cScienceSmosFile getL1cScienceSmosFile(RasterDataNode raster) {
        if (raster != null) {
            final ProductReader productReader = raster.getProductReader();
            if (productReader instanceof SmosProductReader) {
                final ExplorerFile smosFile = ((SmosProductReader) productReader).getExplorerFile();
                if (smosFile instanceof L1cScienceSmosFile) {
                    return (L1cScienceSmosFile) smosFile;
                }
            }
        }

        return null;
    }

    static L1cScienceSmosFile getL1cScienceSmosFile(ProductSceneView smosView) {
        if (smosView != null) {
            return getL1cScienceSmosFile(smosView.getRaster());
        }
        return null;
    }

    public static boolean hasLayer(Layer parentLayer, LayerType layerType) {
        for (final Layer childLayer : parentLayer.getChildren()) {
            if (layerType == childLayer.getLayerType()) {
                return true;
            }
        }
        return false;
    }
}
