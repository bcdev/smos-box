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
 * @author Ralf Quast
 * @version $Revision: $ $Date: $
 * @since SMOS-Box 1.0
 */
public class SmosDgg {

    private static final String SMOS_DGG_DIR_PROPERTY_NAME = "org.esa.beam.smos.smosDggDir";

    private volatile MultiLevelImage dggMultiLevelImage;

    private SmosDgg() {
    }

    public static SmosDgg getInstance() {
        return Holder.instance;
    }

    public static int smosGridPointIdToDggSeqnum(int gridPointId) {
        final int a = 1000000;
        final int b = 262144;

        return gridPointId < a ? gridPointId : b * ((gridPointId - 1) / a) + ((gridPointId - 1) % a) + 2;
    }

    public MultiLevelImage getDggMultiLevelImage() {
        if (dggMultiLevelImage == null) {
            synchronized (getInstance()) {
                if (dggMultiLevelImage == null) {
                    dggMultiLevelImage = createDggMultiLevelImage();
                }
            }
        }
        return dggMultiLevelImage;
    }

    private MultiLevelImage createDggMultiLevelImage() {
        String dirPath;
        try {
            dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            final File dir = new File(dirPath);
            final MultiLevelSource dggMultiLevelSource = TiledFileMultiLevelSource.create(dir);

            return new DefaultMultiLevelImage(dggMultiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS DDG multi-level image: {0}", e.getMessage()), e);
        }
    }

    private static String getDirPathFromModule() throws URISyntaxException {
        final URL url = SmosDgg.class.getResource("image.properties");
        final URI uri = url.toURI();

        return new File(uri).getParent();
    }

    private static String getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_DGG_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final File dir = new File(dirPath);
            if (!dir.canRead()) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir.getPath(), SMOS_DGG_DIR_PROPERTY_NAME));
            }
        }

        return dirPath;
    }

    // Initialization on demand holder idiom
    private static class Holder {
        private static final SmosDgg instance = new SmosDgg();
    }
}
