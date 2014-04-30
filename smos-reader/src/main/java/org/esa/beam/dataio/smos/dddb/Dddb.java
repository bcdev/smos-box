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

package org.esa.beam.dataio.smos.dddb;

import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.binx.BinX;
import org.esa.beam.util.io.CsvReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Data descriptor data base for SMOS product files.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
public class Dddb {

    private static final String TAG_DATABLOCK_SCHEMA = "Datablock_Schema";
    // Reference: SO-MA-IDR-GS-0004, SMOS DPGS, XML Schema Guidelines
    private static final String SCHEMA_NAMING_CONVENTION = "DBL_\\w{2}_\\w{4}_\\w{10}_\\d{4}";

    private final Charset charset = Charset.forName("UTF-8");
    private final char[] separators = new char[]{'|'};

    private final ResourcePathBuilder pathBuilder;
    private final ConcurrentMap<String, DataFormat> dataFormatMap;
    private final ConcurrentMap<String, BandDescriptors> bandDescriptorMap;
    private final ConcurrentMap<String, FlagDescriptors> flagDescriptorMap;

    private Dddb() {
        dataFormatMap = new ConcurrentHashMap<>(17);
        bandDescriptorMap = new ConcurrentHashMap<>(17);
        flagDescriptorMap = new ConcurrentHashMap<>(17);

        pathBuilder = new ResourcePathBuilder();
    }

    public static Dddb getInstance() {
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
                    if (e.getChildText(TAG_DATABLOCK_SCHEMA, namespace) != null) {
                        return true;
                    }
                }

                return false;
            }
        });
        if (descendants.hasNext()) {
            final Element e = (Element) descendants.next();
            final String formatName = e.getChildText(TAG_DATABLOCK_SCHEMA, namespace).substring(0, 27);

            return getDataFormat(formatName);
        } else {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing datablock schema.", hdrFile.getPath()));
        }
    }

    public BandDescriptor findBandDescriptorForMember(String formatName, String memberName) {
        final Family<BandDescriptor> descriptors = getBandDescriptors(formatName);
        if (descriptors != null) {
            for (final BandDescriptor descriptor : descriptors.asList()) {
                if (descriptor.getMemberName().equals(memberName)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    public Family<BandDescriptor> getBandDescriptors(String formatName) {
        if (!bandDescriptorMap.containsKey(formatName)) {
            final InputStream inputStream = getBandDescriptorResource(formatName);

            if (inputStream != null) {
                final BandDescriptors descriptors;

                try {
                    descriptors = readBandDescriptors(inputStream);
                } catch (Throwable t) {
                    throw new IllegalStateException(MessageFormat.format(
                            "An error ocurred while reading band descriptors for format name ''{0}''.", formatName));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }

                bandDescriptorMap.putIfAbsent(formatName, descriptors);
            }
        }

        return bandDescriptorMap.get(formatName);
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
                            "An error ocurred while reading flag descriptors for identifier ''{0}''.", identifier));
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
        if (schemaName == null || !schemaName.matches(SCHEMA_NAMING_CONVENTION)) {
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
                if (name.matches("DBL_\\w{2}_\\w{4}_MIR_SC\\w{2}1C_\\d{4}")) {
                    binX.setTypeMembersInlined(getResourceAsProperties("structs_MIR_SCXX1C.properties"));
                }
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

    private InputStream getBandDescriptorResource(String formatName) {
        if (formatName == null || !formatName.matches(SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return getClass().getResourceAsStream(pathBuilder.buildPath(formatName, "bands", ".csv"));
    }

    private InputStream getFlagDescriptorResource(String identifier) {
        if (identifier == null || !identifier.matches(SCHEMA_NAMING_CONVENTION + "_.*")) {
            return null;
        }

        return getClass().getResourceAsStream(pathBuilder.buildPath(identifier, "flags", ".csv"));
    }

    // Initialization on demand holder idiom

    private static class Holder {

        private static final Dddb INSTANCE = new Dddb();
    }

    private static class BandDescriptors implements Family<BandDescriptor> {

        private final List<BandDescriptor> descriptorList;
        private final Map<String, BandDescriptor> descriptorMap;

        BandDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<>(recordList.size());
            descriptorMap = new HashMap<>(recordList.size());

            for (final String[] tokens : recordList) {
                final BandDescriptorImpl bandDescriptor = new BandDescriptorImpl(tokens, getInstance());
                descriptorList.add(bandDescriptor);
                descriptorMap.put(bandDescriptor.getBandName(), bandDescriptor);
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

    static class FlagDescriptors implements Family<FlagDescriptor> {

        private final List<FlagDescriptor> descriptorList;
        private final Map<String, FlagDescriptor> descriptorMap;

        FlagDescriptors(List<String[]> recordList) {
            descriptorList = new ArrayList<>(recordList.size());
            descriptorMap = new HashMap<>(recordList.size());

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
}
