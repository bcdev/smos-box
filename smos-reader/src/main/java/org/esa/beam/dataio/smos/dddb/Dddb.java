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

import com.bc.ceres.binio.*;
import com.bc.ceres.binio.binx.BinX;
import org.esa.beam.util.io.CsvReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.net.MalformedURLException;
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

    private final ResourceHandler resourceHandler;
    private final ConcurrentMap<String, DataFormat> dataFormatMap;
    private final ConcurrentMap<String, BandDescriptors> bandDescriptorMap;
    private final ConcurrentMap<String, FlagDescriptors> flagDescriptorMap;

    private Dddb() {
        dataFormatMap = new ConcurrentHashMap<>(17);
        bandDescriptorMap = new ConcurrentHashMap<>(17);
        flagDescriptorMap = new ConcurrentHashMap<>(17);

        resourceHandler = new ResourceHandler();
    }

    public static Dddb getInstance() {
        return Holder.INSTANCE;
    }

    DataFormat getDataFormat(String formatName) {
        if (!dataFormatMap.containsKey(formatName)) {
            try {
                final URL url = getSchemaResource(formatName);
                if (url != null) {
                    final DataFormat format = createBinX(formatName).readDataFormat(url.toURI(), formatName);
                    format.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    dataFormatMap.putIfAbsent(formatName, format);
                    return format;
                }
            } catch (Throwable e) {
                throw new IllegalStateException(MessageFormat.format("Schema resource ''{0}'': {1}", formatName, e.getMessage()));
            }
        }

        return dataFormatMap.get(formatName);
    }

    public DataFormat getDataFormat(File hdrFile) throws IOException {
        final String formatName = extractFormatName(hdrFile);

        return getDataFormat(formatName);
    }

    private String extractFormatName(File hdrFile) throws IOException {
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
        final String formatName;
        if (descendants.hasNext()) {
            final Element e = (Element) descendants.next();
            formatName = e.getChildText(TAG_DATABLOCK_SCHEMA, namespace).substring(0, 27);
        } else {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing datablock schema.", hdrFile.getPath()));
        }
        return formatName;
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
            InputStream inputStream = null;
            try {
                inputStream = getBandDescriptorResource(formatName);
                if (inputStream != null) {
                    final BandDescriptors descriptors = readBandDescriptors(inputStream);
                    bandDescriptorMap.putIfAbsent(formatName, descriptors);
                }
            } catch (Throwable t) {
                throw new IllegalStateException(MessageFormat.format(
                        "An error occurred while reading band descriptors for format name ''{0}''.", formatName));
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }

        return bandDescriptorMap.get(formatName);
    }

    public Family<FlagDescriptor> getFlagDescriptors(String identifier) {
        if (!flagDescriptorMap.containsKey(identifier)) {
            InputStream inputStream = null;

            try {
                inputStream = getFlagDescriptorResource(identifier);
                if (inputStream != null) {
                    final FlagDescriptors descriptors = readFlagDescriptors(inputStream);

                    flagDescriptorMap.putIfAbsent(identifier, descriptors);
                }
            } catch (Throwable e) {
                throw new IllegalStateException(MessageFormat.format(
                        "An error occurred while reading flag descriptors for identifier ''{0}''.", identifier));
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }

        return flagDescriptorMap.get(identifier);
    }

    public Family<MemberDescriptor> getMemberDescriptors(File hdrFile) throws IOException {
        final MemberDescriptors memberDescriptors = new MemberDescriptors();

        final DataFormat dataFormat = getDataFormat(hdrFile);
        final CompoundType type = dataFormat.getType();

        final Family<BandDescriptor> bandDescriptors = getBandDescriptors(dataFormat.getName());
        final Map<String, BandDescriptor> uniqueMemberMap = extractUniqueMembers(bandDescriptors);

        extractMembers(type, memberDescriptors);

        final List<MemberDescriptor> memberDescriptorList = memberDescriptors.asList();
        final ArrayList<MemberDescriptor> toRemoveList = new ArrayList<>();
        for (MemberDescriptor memberDescriptor : memberDescriptorList) {
            if (!uniqueMemberMap.containsKey(memberDescriptor.getName())) {
                toRemoveList.add(memberDescriptor);
            }
        }

        for (MemberDescriptor toRemove : toRemoveList) {
            memberDescriptors.remove(toRemove.getName());
        }

        final String formatName = extractFormatName(hdrFile);
        final Properties mappingProperties = getMappingProperties(formatName);

        for (final MemberDescriptor memberDescriptor : memberDescriptorList) {
            final String memberDescriptorName = memberDescriptor.getName();
            if (mappingProperties != null) {
                final String originalName = findOriginalName(mappingProperties, memberDescriptorName);
                if (originalName != null) {
                    memberDescriptor.setName(originalName);
                }
            }

            final BandDescriptor bandDescriptor = uniqueMemberMap.get(memberDescriptorName);
            memberDescriptor.setGridPointData(bandDescriptor.isGridPointData());
            memberDescriptor.setDimensionNames(bandDescriptor.getDimensionNames());
        }

        return memberDescriptors;
    }

    // package access for testing only tb 2014-07-09
    static String findOriginalName(Properties mappingProperties, String searchName) {
        final Set<Map.Entry<Object, Object>> entries = mappingProperties.entrySet();
        for (Map.Entry<Object, Object> next : entries) {
            if (searchName.equalsIgnoreCase((String) next.getValue())) {
                return (String) next.getKey();
            }
        }

        return null;
    }

    private Map<String, BandDescriptor> extractUniqueMembers(Family<BandDescriptor> bandDescriptors) {
        final HashMap<String, BandDescriptor> uniqueMemberMap = new HashMap<>();

        final List<BandDescriptor> bandDescriptorsList = bandDescriptors.asList();
        for (BandDescriptor bandDescriptor : bandDescriptorsList) {
            final String memberName = bandDescriptor.getMemberName();
            if (uniqueMemberMap.containsKey(memberName)) {
                continue;
            }

            uniqueMemberMap.put(memberName, bandDescriptor);
        }
        return uniqueMemberMap;
    }

    private void extractMembers(CompoundType type, MemberDescriptors memberDescriptors) {
        final CompoundMember[] members = type.getMembers();
        for (CompoundMember member : members) {
            final Type subType = member.getType();

            if (subType.isSimpleType()) {
                final MemberDescriptor memberDescriptor = new MemberDescriptor();
                memberDescriptor.setName(member.getName());
                memberDescriptor.setDataTypeName(subType.getName());
                memberDescriptors.add(memberDescriptor);
            } else if (subType.isCompoundType()) {
                extractMembers((CompoundType) subType, memberDescriptors);
            } else if (subType.isSequenceType()) {
                final SequenceType sequenceType = (SequenceType) subType;
                final Type elementType = sequenceType.getElementType();
                if (elementType.isSimpleType()) {
                    final MemberDescriptor memberDescriptor = new MemberDescriptor();
                    memberDescriptor.setName(member.getName());
                    memberDescriptors.add(memberDescriptor);
                } else if (elementType.isCompoundType()) {
                    extractMembers((CompoundType) elementType, memberDescriptors);
                }
            }
        }
    }

    private URL getSchemaResource(String schemaName) throws MalformedURLException {
        if (schemaName == null || !schemaName.matches(SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return resourceHandler.getResourceUrl(ResourceHandler.buildPath(schemaName, "schemas", ".binXschema.xml"));
    }

    private BinX createBinX(String name) {
        final BinX binX = new BinX();
        binX.setSingleDatasetStructInlined(true);
        binX.setArrayVariableInlined(true);


        try {
            final Properties mappingProperties = getMappingProperties(name);
            if (mappingProperties != null) {
                binX.setVarNameMappings(mappingProperties);
            }

            if (name.matches("DBL_\\w{2}_\\w{4}_MIR_SC\\w{2}1C_\\d{4}")) {
                binX.setTypeMembersInlined(resourceHandler.getResourceAsProperties("structs_MIR_SCXX1C.properties"));
            } else if (name.contains("MIR_OSDAP2")) {
                binX.setTypeMembersInlined(resourceHandler.getResourceAsProperties("structs_MIR_OSDAP2.properties"));
            } else if (name.contains("MIR_OSUDP2")) {
                binX.setTypeMembersInlined(resourceHandler.getResourceAsProperties("structs_MIR_OSUDP2.properties"));
            } else if (name.contains("MIR_SMDAP2")) {
                binX.setTypeMembersInlined(resourceHandler.getResourceAsProperties("structs_MIR_SMDAP2.properties"));
            } else if (name.contains("MIR_SMUDP2")) {
                binX.setTypeMembersInlined(resourceHandler.getResourceAsProperties("structs_MIR_SMUDP2.properties"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        return binX;
    }

    private Properties getMappingProperties(String name) throws IOException {
        Properties mappingProperties = null;
        if (name.contains("AUX_ECMWF_")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_AUX_ECMWF_.properties");
        } else if (name.matches("DBL_\\w{2}_\\w{4}_MIR_\\w{4}1C_\\d{4}")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_MIR_XXXX1C.properties");
        } else if (name.contains("MIR_OSDAP2")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_MIR_OSDAP2.properties");
        } else if (name.contains("MIR_OSUDP2")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_MIR_OSUDP2.properties");
        } else if (name.contains("MIR_SMDAP2")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_MIR_SMDAP2.properties");
        } else if (name.contains("MIR_SMUDP2")) {
            mappingProperties = resourceHandler.getResourceAsProperties("mappings_MIR_SMUDP2.properties");
        }
        return mappingProperties;
    }

    private BandDescriptors readBandDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, charset), separators, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new BandDescriptors(recordList, this);
    }

    private FlagDescriptors readFlagDescriptors(InputStream inputStream) throws IOException {
        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, charset), separators, true, "#");
        final List<String[]> recordList = reader.readStringRecords();

        return new FlagDescriptors(recordList);
    }

    private InputStream getBandDescriptorResource(String formatName) throws FileNotFoundException {
        if ("BUFR".equals(formatName)) {
            return resourceHandler.getResourceStream("bands/BUFR/BUFR.csv");
        }
        if (formatName == null || !formatName.matches(SCHEMA_NAMING_CONVENTION)) {
            return null;
        }

        return resourceHandler.getResourceStream(ResourceHandler.buildPath(formatName, "bands", ".csv"));
    }

    private InputStream getFlagDescriptorResource(String identifier) throws FileNotFoundException {
        if ("BUFR_flags".equals(identifier)) {
            return resourceHandler.getResourceStream("flags/BUFR/BUFR_flags.csv");
        }
        if (identifier == null || !identifier.matches(SCHEMA_NAMING_CONVENTION + "_.*")) {
            return null;
        }

        return resourceHandler.getResourceStream(ResourceHandler.buildPath(identifier, "flags", ".csv"));
    }

    // Initialization on demand holder idiom
    private static class Holder {
        private static final Dddb INSTANCE = new Dddb();
    }
}
