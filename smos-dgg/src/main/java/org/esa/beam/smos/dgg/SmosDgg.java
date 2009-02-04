package org.esa.beam.smos.dgg;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.net.URL;
import java.net.URISyntaxException;

import org.esa.beam.glevel.TiledFileMultiLevelSource;

/**
 * Providfes a {@link com.bc.ceres.glevel.MultiLevelImage multi level image} of
 * the SMOS Discrete global grid.
 *
 * @author Marco Peters
 * @version $Revision: $ $Date: $
 * @since BEAM 4.6
 */
public class SmosDgg {

    private static final String SMOS_DGG_DIR_PROPERTY_NAME = "org.esa.beam.pview.smosDggDir";
    private static MultiLevelImage dggridMultiLevelImage;
    private static final int A = 1000000;
    private static final int B = 262144;

    public static MultiLevelImage getDggridMultiLevelImage() throws IOException {
        if (dggridMultiLevelImage == null) {
            String dirPath = getDirPathFromProperty();
            if (dirPath == null) {
                dirPath = getDirPathFromModule();
            }
            try {
                MultiLevelSource dggridMultiLevelSource = TiledFileMultiLevelSource.create(new File(dirPath), false);
                dggridMultiLevelImage = new DefaultMultiLevelImage(dggridMultiLevelSource);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format("Failed to load SMOS DDG ''{0}''", dirPath), e);
            }
        }
        return dggridMultiLevelImage;
    }

    public static int smosGridPointIdToDggridSeqnum(int smosId) {
        return smosId < A ? smosId : B * ((smosId - 1) / A) + ((smosId - 1) % A) + 2;
    }

    private static String getDirPathFromModule() {
        final URL resource = SmosDgg.class.getResource("image.properties");
        try {
            return new File(resource.toURI()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getDirPathFromProperty() throws IOException {
        String dirPath = System.getProperty(SMOS_DGG_DIR_PROPERTY_NAME);
        if (dirPath != null && !new File(dirPath).exists()) {
            throw new IOException(
                    MessageFormat.format(
                            "SMOS products require a DGG image.\nPlease set system property ''{0}''to a valid DGG image directory.",
                            SMOS_DGG_DIR_PROPERTY_NAME));
        }
        return dirPath;
    }

}
