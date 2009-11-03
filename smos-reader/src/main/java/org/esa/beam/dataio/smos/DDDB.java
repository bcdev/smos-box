package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.binx.BinX;
import org.esa.beam.util.io.CsvReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Data descriptor data base for SMOS product files.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class DDDB {

    private final Charset charset = Charset.forName("UTF-8");
    private final char[] separators = new char[]{'|'};
    private final ResourcePathBuilder pathBuilder = new ResourcePathBuilder();

    private final ConcurrentMap<String, DataFormat> dataFormatMap;
    private final ConcurrentMap<String, BandDescriptors> bandDescriptorMap;
    private final ConcurrentMap<String, FlagDescriptors> flagDescriptorMap;

    private DDDB() {
        dataFormatMap = new ConcurrentHashMap<String, DataFormat>(17);
        bandDescriptorMap = new ConcurrentHashMap<String, BandDescriptors>(17);
        flagDescriptorMap = new ConcurrentHashMap<String, FlagDescriptors>(17);
    }

    public static DDDB getInstance() {
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

    public Family<BandDescriptor> getBandDescriptors(String identifier) {
        if (!bandDescriptorMap.containsKey(identifier)) {
            final InputStream inputStream = getBandDescriptorResource(identifier);

            if (inputStream != null) {
                final BandDescriptors descriptors;

                try {
                    descriptors = readBandDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Band descriptor resource for identifier ''{0}'': {1}", identifier, e.getMessage()));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                bandDescriptorMap.putIfAbsent(identifier, descriptors);
            }
        }

        return bandDescriptorMap.get(identifier);
    }

    public Family<FlagDescriptor> getFlagDescriptors(String identifier) {
        if (!flagDescriptorMap.containsKey(identifier)) {
            final InputStream inputStream = getFlagDescriptorResource(identifier);

            if (inputStream != null) {
                final FlagDescriptors descriptors;

                try {
                    descriptors = readFlagDescriptors(inputStream);
                } catch (Throwable e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Flag descriptor resource for identifier ''{0}'': {1}", identifier, e.getMessage()));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                flagDescriptorMap.putIfAbsent(identifier, descriptors);
            }
        }

        return flagDescriptorMap.get(identifier);
    }

    private URL getSchemaResource(String schemaName) {
        if (schemaName == null || !schemaName.matches(SmosConstants.SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return getClass().getResource(pathBuilder.buildPath(schemaName, "schemas", ".binXschema.xml"));
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
        final InputStream is = getClass().getResourceAsStream(name);

        if (is != null) {
            properties.load(is);
        }

        return properties;
    }

    private BandDescriptors readBandDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, charset), separators, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new BandDescriptors(recordList);
    }

    private FlagDescriptors readFlagDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, charset), separators, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new FlagDescriptors(recordList);
    }

    private InputStream getBandDescriptorResource(String identifier) {
        if (identifier == null || !identifier.matches(SmosConstants.SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return getClass().getResourceAsStream(pathBuilder.buildPath(identifier, "bands", ".csv"));
    }

    private InputStream getFlagDescriptorResource(String identifier) {
        if (identifier == null || !identifier.matches(SmosConstants.SCHEMA_NAMING_CONVENTION + "_.*")) {
            return null;
        }

        return getClass().getResourceAsStream(pathBuilder.buildPath(identifier, "flags", ".csv"));
    }

    private static class ResourcePathBuilder {

        String buildPath(String identifier, String root, String appendix) {
            final String fc = identifier.substring(12, 16);
            final String sd = identifier.substring(16, 22);

            final StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(root).append("/").append(fc).append("/").append(sd).append("/").append(identifier);
            pathBuilder.append(appendix);

            return pathBuilder.toString();
        }
    }

    // Initialization on demand holder idiom
    private static class Holder {

        private static final DDDB INSTANCE = new DDDB();
    }

    private static class BandDescriptors implements Family<BandDescriptor> {

        private final List<BandDescriptor> descriptorList;
        private final Map<String, BandDescriptor> descriptorMap;

        BandDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<BandDescriptor>(recordList.size());
            descriptorMap = new HashMap<String, BandDescriptor>(recordList.size());

            for (final String[] tokens : recordList) {
                final BandDescriptorImpl descriptor = new BandDescriptorImpl(tokens);
                descriptorList.add(descriptor);
                descriptorMap.put(descriptor.getBandName(), descriptor);
            }
        }

        @Override
        public final List<BandDescriptor> asList() {
            return Collections.unmodifiableList(descriptorList);
        }

        @Override
        public final BandDescriptor getMember(String bandName) {
            return descriptorMap.get(bandName);
        }
    }

    private static class FlagDescriptors implements Family<FlagDescriptor> {

        private final List<FlagDescriptor> descriptorList;
        private final Map<String, FlagDescriptor> descriptorMap;

        FlagDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<FlagDescriptor>(recordList.size());
            descriptorMap = new HashMap<String, FlagDescriptor>(recordList.size());

            for (final String[] tokens : recordList) {
                final FlagDescriptorImpl record = new FlagDescriptorImpl(tokens);
                descriptorList.add(record);
                descriptorMap.put(record.getFlagName(), record);
            }
        }

        @Override
        public final List<FlagDescriptor> asList() {
            return Collections.unmodifiableList(descriptorList);
        }

        @Override
        public final FlagDescriptor getMember(String flagName) {
            return descriptorMap.get(flagName);
        }
    }

    private static class BandDescriptorImpl implements BandDescriptor {

        private final boolean visible;
        private final String bandName;
        private final String memberName;
        private final int index;
        private final int sampleModel;
        private final double scalingOffset;
        private final double scalingFactor;
        private final double typicalMin;
        private final double typicalMax;
        private final boolean cyclic;
        private final double fillValue;
        private final String validPixelExpression;
        private final String unit;
        private final String description;
        private final String flagCodingName;
        private final Family<FlagDescriptor> flagDescriptors;

        private BandDescriptorImpl(String[] tokens) {
            visible = parseBoolean(tokens[0], true);
            bandName = parseString(tokens[1]);
            memberName = parseString(tokens[2], bandName);
            index = parseInt(tokens[3], -1);
            sampleModel = parseInt(tokens[4], 0);

            scalingOffset = parseDouble(tokens[5], 0.0);
            scalingFactor = parseDouble(tokens[6], 1.0);

            typicalMin = parseDouble(tokens[7], Double.NEGATIVE_INFINITY);
            typicalMax = parseDouble(tokens[8], Double.POSITIVE_INFINITY);
            cyclic = parseBoolean(tokens[9], false);

            fillValue = parseDouble(tokens[10], Double.NaN);
            validPixelExpression = parseString(tokens[11], "").replaceAll("x", bandName);

            unit = parseString(tokens[12], "");
            description = parseString(tokens[13], "");
            flagCodingName = parseString(tokens[14], "");
            flagDescriptors = getFlagDescriptors(tokens[15]);
        }

        private Family<FlagDescriptor> getFlagDescriptors(String token) {
            if (flagCodingName.isEmpty()) {
                return null;
            }
            return getInstance().getFlagDescriptors(parseString(token));
        }

        @Override
        public final String getBandName() {
            return bandName;
        }

        @Override
        public final String getMemberName() {
            return memberName;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public final int getSampleModel() {
            return sampleModel;
        }

        @Override
        public final double getScalingOffset() {
            return scalingOffset;
        }

        @Override
        public final double getScalingFactor() {
            return scalingFactor;
        }

        @Override
        public final boolean hasTypicalMin() {
            return !Double.isInfinite(typicalMin);
        }

        @Override
        public final boolean hasTypicalMax() {
            return !Double.isInfinite(typicalMax);
        }

        @Override
        public final boolean hasFillValue() {
            return !Double.isNaN(fillValue);
        }

        @Override
        public final double getTypicalMin() {
            return typicalMin;
        }

        @Override
        public final double getTypicalMax() {
            return typicalMax;
        }

        @Override
        public final double getFillValue() {
            return fillValue;
        }

        @Override
        public final String getValidPixelExpression() {
            return validPixelExpression;
        }

        @Override
        public final String getUnit() {
            return unit;
        }

        @Override
        public final boolean isCyclic() {
            return cyclic;
        }

        @Override
        public final String getDescription() {
            return description;
        }

        @Override
        public final String getFlagCodingName() {
            return flagCodingName;
        }

        @Override
        public final Family<FlagDescriptor> getFlagDescriptors() {
            return flagDescriptors;
        }
    }

    private static class FlagDescriptorImpl implements FlagDescriptor {

        private final boolean visible;
        private final String flagName;
        private final int mask;
        private final Color color;
        private final double transparency;
        private final String description;

        FlagDescriptorImpl(String[] tokens) {
            visible = parseBoolean(tokens[1], false);
            flagName = parseString(tokens[1]);
            mask = parseHex(tokens[2], 0);
            color = parseColor(tokens[3], null);
            transparency = parseDouble(tokens[4], 0.5);
            description = parseString(tokens[5], "");
        }

        @Override
        public final String getFlagName() {
            return flagName;
        }

        @Override
        public final int getMask() {
            return mask;
        }

        @Override
        public final boolean isVisible() {
            return visible;
        }

        @Override
        public final Color getColor() {
            return color;
        }

        @Override
        public final double getTransparency() {
            return transparency;
        }

        @Override
        public final String getDescription() {
            return description;
        }
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private static boolean parseBoolean(String token, boolean defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Boolean.parseBoolean(token);
    }

    private static Color parseColor(String token, Color defaultColor) {
        if ("*".equals(token.trim())) {
            return defaultColor;
        }

        return new Color(Integer.parseInt(token, 16));
    }

    private static double parseDouble(String token, double defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Double.parseDouble(token);
    }

    private static int parseHex(String token, int defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Integer.parseInt(token, 16);
    }

    private static int parseInt(String token, int defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return Integer.parseInt(token);
    }

    private static String parseString(String token) {
        return token.trim();
    }

    private static String parseString(String token, String defaultValue) {
        if ("*".equals(token.trim())) {
            return defaultValue;
        }
        return token.trim();
    }
}
