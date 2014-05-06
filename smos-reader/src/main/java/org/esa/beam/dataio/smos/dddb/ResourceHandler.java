package org.esa.beam.dataio.smos.dddb;


import org.esa.beam.util.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

class ResourceHandler {

    static final String SMOS_DDDB_DIR_PROPERTY_NAME = "org.esa.beam.smos.dddbDir";

    static String buildPath(String identifier, String root, String appendix) {
        final String fc = identifier.substring(12, 16);
        final String sd = identifier.substring(16, 22);

        final StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(root).append("/").append(fc).append("/").append(sd).append("/").append(identifier);
        pathBuilder.append(appendix);

        return pathBuilder.toString();
    }

    InputStream getResourceStream(String path) throws FileNotFoundException {
        final String dddbDirFromProperty = System.getProperty(SMOS_DDDB_DIR_PROPERTY_NAME);
        if (StringUtils.isNotNullAndNotEmpty(dddbDirFromProperty)) {
            final File resourceFile = new File(dddbDirFromProperty, path);
            if (resourceFile.isFile()) {
                return new FileInputStream(resourceFile);
            }
        }
        return getClass().getResourceAsStream(path);
    }

    URL getResourceUrl(String path) throws MalformedURLException {
        final String dddbDirFromProperty = System.getProperty(SMOS_DDDB_DIR_PROPERTY_NAME);
        if (StringUtils.isNotNullAndNotEmpty(dddbDirFromProperty)) {
            final File resourceFile = new File(dddbDirFromProperty, path);
            return new URL("file", "", 0, resourceFile.getAbsolutePath());
        }
        return getClass().getResource(path);
    }

    Properties getResourceAsProperties(String path) throws IOException {
        final Properties properties = new Properties();
        final InputStream is = getResourceStream(path);

        if (is != null) {
            properties.load(is);
            is.close();
        }
        return properties;
    }
}
