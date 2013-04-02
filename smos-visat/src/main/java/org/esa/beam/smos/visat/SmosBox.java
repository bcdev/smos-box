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

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.LayerTypeRegistry;
import com.bc.ceres.glayer.support.LayerUtils;
import org.esa.beam.dataio.smos.ExplorerFile;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.glayer.WorldMapLayerType;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.VisatPlugIn;

public class SmosBox implements VisatPlugIn {

    private static final String WORLDMAP_TYPE_PROPERTY_NAME = "worldmap.type";
    private static final String BLUE_MARBLE_LAYER_TYPE = "BlueMarbleLayerType";

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
                        Layer worldMapLayer = findWorldMapLayer(newView);
                        if (worldMapLayer == null) {
                            worldMapLayer = createWorldMapLayer();
                            final Layer rootLayer = newView.getRootLayer();
                            rootLayer.getChildren().add(worldMapLayer);
                        }
                        worldMapLayer.setVisible(true);
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
            try {
                return getL1cScienceSmosFile(smosView.getRaster()); // can produce NullPointerException
            } catch (NullPointerException e) {
                return null;
            }
        }
        return null;
    }

    private LayerType getWorldMapLayerType() {
        final VisatApp visatApp = VisatApp.getApp();
        String layerTypeClassName = visatApp.getPreferences().getPropertyString(WORLDMAP_TYPE_PROPERTY_NAME,
                                                                                BLUE_MARBLE_LAYER_TYPE);
        return LayerTypeRegistry.getLayerType(layerTypeClassName);
    }

    private Layer createWorldMapLayer() {
        final LayerType layerType = getWorldMapLayerType();
        final PropertySet template = layerType.createLayerConfig(null);
        return layerType.createLayer(null, template);
    }

    private Layer findWorldMapLayer(ProductSceneView view) {
        return LayerUtils.getChildLayer(view.getRootLayer(), LayerUtils.SearchMode.DEEP, new LayerFilter() {
            @Override
            public boolean accept(Layer layer) {
                return layer.getLayerType() instanceof WorldMapLayerType;
            }
        });
    }
}
