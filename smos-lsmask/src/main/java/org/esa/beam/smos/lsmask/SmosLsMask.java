package org.esa.beam.smos.lsmask;


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

public class SmosLsMask {

    private static final String SMOS_LS_MASK_DIR_PROPERTY_NAME = "org.esa.beam.smos.lsMaskDir";

    private volatile MultiLevelImage multiLevelImage;

    public static SmosLsMask getInstance() {
        return Holder.INSTANCE;
    }

    public MultiLevelImage getMultiLevelImage() {
        if (multiLevelImage == null) {
            synchronized (getInstance()) {
                if (multiLevelImage == null) {
                    multiLevelImage = createMultiLevelImage();
                }
            }
        }
        return multiLevelImage;
    }

    private MultiLevelImage createMultiLevelImage() {
        String dirPath;
        try {
            dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            final File dir = new File(dirPath);
            final MultiLevelSource multiLevelSource = TiledFileMultiLevelSource.create(dir);

            return new DefaultMultiLevelImage(multiLevelSource);
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot create SMOS Land/Sea Mask multi-level image: {0}", e.getMessage()), e);
        }
    }

    private static String getDirPathFromModule() throws URISyntaxException {
        final URL url = SmosLsMask.class.getResource("image.properties");
        final URI uri = url.toURI();

        return new File(uri).getParent();
    }

    private static String getDirPathFromProperty() throws IOException {
        final String dirPath = System.getProperty(SMOS_LS_MASK_DIR_PROPERTY_NAME);

        if (dirPath != null) {
            final File dir = new File(dirPath);
            if (!dir.canRead()) {
                throw new IOException(MessageFormat.format(
                        "Cannot read directory ''{0}''. System property ''{0}'' must point to a readable directory.",
                        dir.getPath(), SMOS_LS_MASK_DIR_PROPERTY_NAME));
            }
        }

        return dirPath;
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final SmosLsMask INSTANCE = new SmosLsMask();
    }

}
