package org.esa.beam.smos.worldmap;

import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.MultiLevelSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.List;

import org.esa.beam.glevel.TiledFileMultiLevelSource;

/**
 * Provides a world map layer for the SMOS-Box.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 * @since BEAM 4.6
 */
public class SmosWorldMapLayer {
    private static final String WORLD_IMAGE_DIR_PROPERTY_NAME = "org.esa.beam.pview.worldImageDir";
    private static final String WORLD_MAP_LAYER_NAME = "World Map (NASA Blue Marble)";

    private SmosWorldMapLayer() {
    }

    public static Layer createWorldMapLayer() {
        String dirPath = System.getProperty(WORLD_IMAGE_DIR_PROPERTY_NAME);
        if (dirPath == null || dirPath.isEmpty()) {
            dirPath = getDirPathFromModule();
        }
        if (dirPath == null) {
            return null;
        }
        MultiLevelSource multiLevelSource;
        try {
            multiLevelSource = TiledFileMultiLevelSource.create(new File(dirPath), false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        final ImageLayer worldMapLayer = new ImageLayer(multiLevelSource);
        worldMapLayer.setName(WORLD_MAP_LAYER_NAME);
        worldMapLayer.setVisible(true);
        worldMapLayer.getStyle().setOpacity(1.0);
        return worldMapLayer;

    }

    public static boolean hasWorldMapChildLayer(Layer layer) {
        final List<Layer> rootChildren = layer.getChildren();
        for (Layer child : rootChildren) {
            if (WORLD_MAP_LAYER_NAME.equals(child.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String getDirPathFromModule() {
        final URL resource = SmosWorldMapLayer.class.getResource("image.properties");
        try {
            return new File(resource.toURI()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}
