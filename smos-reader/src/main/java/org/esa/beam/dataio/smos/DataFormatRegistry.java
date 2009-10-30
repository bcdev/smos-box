package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.binx.BinX;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for SMOS data formats.
 */
public class DataFormatRegistry {

    private final ResourcePathBuilder pathBuilder;
    private final ConcurrentMap<String, DataFormat> dataFormatMap;

    private DataFormatRegistry() {
        pathBuilder = new ResourcePathBuilder();
        dataFormatMap = new ConcurrentHashMap<String, DataFormat>(17);
    }

    public static DataFormatRegistry getInstance() {
        return Holder.INSTANCE;
    }

    public DataFormat getDataFormat(String formatName) {
        if (!dataFormatMap.containsKey(formatName)) {
            final URL url = getSchemaResource(formatName);

            if (url != null) {
                final DataFormat format;

                try {
                    format = createBinX(formatName).readDataFormat(url.toURI(), formatName);
                } catch (Throwable e) {
                    throw new IllegalStateException(
                            MessageFormat.format("Schema resource ''{0}'': {1}", url, e.getMessage()));
                }

                format.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                dataFormatMap.putIfAbsent(formatName, format);
            }
        }

        return dataFormatMap.get(formatName);
    }

    public DataFormat getDataFormat(File hdrFile) throws IOException {
        final Document document;

        try {
            document = new SAXBuilder().build(hdrFile);
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", hdrFile.getPath()), e);
        }
        final Namespace namespace = document.getRootElement().getNamespace();
        if (namespace == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing namespace", hdrFile.getPath()));
        }
        final Iterator descendants = document.getDescendants(new Filter() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof Element) {
                    final Element e = (Element) o;
                    if (e.getChildText("Datablock_Schema", namespace) != null) {
                        return true;
                    }
                }

                return false;
            }
        });
        if (descendants.hasNext()) {
            final Element e = (Element) descendants.next();
            final String formatName = e.getChildText("Datablock_Schema", namespace).substring(0, 27);

            return getDataFormat(formatName);
        } else {
            throw new IOException(MessageFormat.format("File ''{0}'': Missing datablock schema.", hdrFile.getPath()));
        }
    }

    private URL getSchemaResource(String formatName) {
        if (formatName == null || !formatName.matches(SmosConstants.SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return DataFormatRegistry.class.getResource(pathBuilder.buildPath(formatName, "schemas", ".binXschema.xml"));
    }

    private BinX createBinX(String name) {
        final BinX binX = new BinX();
        binX.setSingleDatasetStructInlined(true);
        binX.setArrayVariableInlined(true);

        try {
            if (name.contains("AUX_ECMWF_")) {
                binX.setVarNameMappings(getResourceAsProperties("mappings_AUX_ECMWF_.properties"));
            }
            if (name.matches("DBL_\\w{2}_\\w{4}_MIR_\\w{4}1C_\\d{4}")) { 
                binX.setVarNameMappings(getResourceAsProperties("mappings_MIR_XXXX1C.properties"));
            }
            if (name.contains("MIR_OSDAP2")) {
                binX.setVarNameMappings(getResourceAsProperties("mappings_MIR_OSDAP2.properties"));
                binX.setTypeMembersInlined(getResourceAsProperties("structs_MIR_OSDAP2.properties"));
            }
            if (name.contains("MIR_OSUDP2")) {
                binX.setVarNameMappings(getResourceAsProperties("mappings_MIR_OSUDP2.properties"));
                binX.setTypeMembersInlined(getResourceAsProperties("structs_MIR_OSUDP2.properties"));
            }
            if (name.contains("MIR_SMDAP2")) {
                binX.setVarNameMappings(getResourceAsProperties("mappings_MIR_SMDAP2.properties"));
                binX.setTypeMembersInlined(getResourceAsProperties("structs_MIR_SMDAP2.properties"));
            }
            if (name.contains("MIR_SMUDP2")) {
                binX.setVarNameMappings(getResourceAsProperties("mappings_MIR_SMUDP2.properties"));
                binX.setTypeMembersInlined(getResourceAsProperties("structs_MIR_SMUDP2.properties"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        return binX;
    }

    private Properties getResourceAsProperties(String name) throws IOException {
        final Properties properties = new Properties();
        final InputStream is = DataFormatRegistry.class.getResourceAsStream(name);

        if (is != null) {
            properties.load(is);
        }

        return properties;
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final DataFormatRegistry INSTANCE = new DataFormatRegistry();
    }
}
