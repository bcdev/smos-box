package org.esa.beam.smos.dgg;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import org.esa.beam.glevel.TiledFileMultiLevelSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Providfes a {@link com.bc.ceres.glevel.MultiLevelImage multi level image} of
 * the SMOS Discrete global grid.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 * @since SMOS-Box 1.0
 */
public class SmosDgg {

    private static final String SMOS_DGG_DIR_PROPERTY_NAME = "org.esa.beam.smos.smosDggDir";
    private static volatile MultiLevelImage dggMultiLevelImage;

    private static final SmosDgg uniqueInstance = new SmosDgg();

    private SmosDgg() {
    }

    public static SmosDgg getInstance() {
        return uniqueInstance;
    }

    public static int smosGridPointIdToDggSeqnum(int gridPointId) {
        final int a = 1000000;
        final int b = 262144;

        return gridPointId < a ? gridPointId : b * ((gridPointId - 1) / a) + ((gridPointId - 1) % a) + 2;
    }

    public MultiLevelImage getDggMultiLevelImage() throws IOException {
        if (dggMultiLevelImage == null) {
            synchronized (uniqueInstance) {
                if (dggMultiLevelImage == null) {
                    createDggMultiLevelImage();
                }
            }
        }
        return dggMultiLevelImage;
    }

    private void createDggMultiLevelImage() throws IOException {
        String dirPath;
        try {
            dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            final File dir = new File(dirPath);
            final MultiLevelSource dggMultiLevelSource = TiledFileMultiLevelSource.create(dir);
            dggMultiLevelImage = new DefaultMultiLevelImage(dggMultiLevelSource);
        } catch (IOException e) {
            final String message = ""; //MessageFormat.format("Failed to load SMOS DDG.");
            throw new IOException(message, e);
        }
    }

    private static String getDirPathFromModule() throws IOException {
        try {
            final URL url = SmosDgg.class.getResource("image.properties");
            final URI uri = url.toURI();

            return new File(uri).getParent();
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static String getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_DGG_DIR_PROPERTY_NAME);
        if (dirPath != null) {
            final File dir = new File(dirPath);
            if (!dir.canRead()) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. Please set the property ''{0}'' to a readable directory.",
                        dir.getPath(), SMOS_DGG_DIR_PROPERTY_NAME));
            }
        }
        return dirPath;
    }

}
