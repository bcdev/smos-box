package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.apache.commons.lang.StringUtils;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.MemberDescriptor;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.DateTimeUtils;
import org.esa.beam.smos.ee2netcdf.geometry.GeometryFilter;
import org.esa.beam.smos.ee2netcdf.geometry.GeometryFilterFactory;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.*;

abstract class AbstractFormatExporter implements FormatExporter {

    private Family<MemberDescriptor> memberDescriptors;

    protected int gridPointCount;
    protected SmosFile explorerFile;
    protected Map<String, VariableDescriptor> variableDescriptors;
    protected ArrayList<Integer> gpIndexList;

    protected static boolean mustExport(String bandName, List<String> outputBandNames) {
        return outputBandNames.isEmpty() || outputBandNames.contains(bandName);
    }

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        explorerFile = getSmosFile(product);
        gridPointCount = explorerFile.getGridPointCount();

        memberDescriptors = Dddb.getInstance().getMemberDescriptors(explorerFile.getHeaderFile());
        createVariableDescriptors(exportParameter);


    }

    @Override
    public void prepareGeographicSubset(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException {
        if (exportParameter.getRegion() != null) {
            final GeometryFilter geometryFilter = GeometryFilterFactory.create(exportParameter.getRegion());
            gpIndexList = new ArrayList<>(gridPointCount);
            for (int i = 0; i < gridPointCount; i++) {
                final CompoundData gridPointData = explorerFile.getGridPointData(i);
                if (geometryFilter.accept(gridPointData)) {
                    gpIndexList.add(i);
                }
            }

            gridPointCount = gpIndexList.size();
        }
    }

    @Override
    public void addGlobalAttributes(NFileWriteable nFileWriteable, MetadataElement metadataRoot, ExportParameter exportParameter) throws IOException {
        final String institution = exportParameter.getInstitution();
        if (StringUtils.isNotBlank(institution)) {
            nFileWriteable.addGlobalAttribute("institution", institution);
        }

        final String contact = exportParameter.getContact();
        if (StringUtils.isNotBlank(contact)) {
            nFileWriteable.addGlobalAttribute("contact", contact);
        }
        nFileWriteable.addGlobalAttribute("creation_date", DateTimeUtils.toFixedHeaderFormat(new Date()));
        nFileWriteable.addGlobalAttribute("total_number_of_grid_points", Integer.toString(gridPointCount));

        final Properties fileMetadata = extractMetadata(metadataRoot);
        final Set<String> metaKeys = fileMetadata.stringPropertyNames();
        for (final String key : metaKeys) {
            final String value = fileMetadata.getProperty(key);
            nFileWriteable.addGlobalAttribute(key, value);
        }
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final List<String> outputBandNames = exportParameter.getVariableNames();
        for (final String ncVariableName : variableNameKeys) {
            if (!mustExport(ncVariableName, outputBandNames)) {
                continue;
            }
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            final NVariable nVariable = nFileWriteable.addVariable(ncVariableName,
                    variableDescriptor.getDataType(),
                    true,
                    null,
                    variableDescriptor.getDimensionNames(),
                    exportParameter.getCompressionLevel());
            final String unitValue = variableDescriptor.getUnit();
            if (StringUtils.isNotBlank(unitValue)) {
                nVariable.addAttribute("units", unitValue);
            }
            if (variableDescriptor.isFillValuePresent()) {
                nVariable.addAttribute("_FillValue", variableDescriptor.getFillValue());
            }
            if (variableDescriptor.isValidMinPresent()) {
                nVariable.addAttribute("valid_min", variableDescriptor.getValidMin());
            }
            if (variableDescriptor.isValidMaxPresent()) {
                nVariable.addAttribute("valid_max", variableDescriptor.getValidMax());
            }
            final short[] flagMasks = variableDescriptor.getFlagMasks();
            if (flagMasks != null) {
                nVariable.addAttribute("flag_masks", Array.factory(flagMasks));
            }
            final short[] flagValues = variableDescriptor.getFlagValues();
            if (flagValues != null) {
                nVariable.addAttribute("flag_values", Array.factory(flagValues));
            }
            final String flagMeanings = variableDescriptor.getFlagMeanings();
            if (StringUtils.isNotBlank(flagMeanings)) {
                nVariable.addAttribute("flag_meanings", flagMeanings);
            }
            if (variableDescriptor.isScaleFactorPresent() || variableDescriptor.isScaleOffsetPresent()) {
                nVariable.addAttribute("scale_factor", variableDescriptor.getScaleFactor());
                nVariable.addAttribute("scale_offset", variableDescriptor.getScaleOffset());
            }
            if (variableDescriptor.isUnsigned()) {
                nVariable.addAttribute("_Unsigned", "true");
            }
        }
    }

    @Override
    abstract public void addDimensions(NFileWriteable nFileWriteable) throws IOException;

    void createVariableDescriptors(ExportParameter exportParameter) {
        variableDescriptors = new HashMap<>();

        final List<String> outputBandNames = exportParameter.getVariableNames();

        final List<MemberDescriptor> memberDescriptorList = memberDescriptors.asList();
        for (final MemberDescriptor memberDescriptor : memberDescriptorList) {
            final String memberDescriptorName = memberDescriptor.getName();
            if (mustExport(memberDescriptorName, outputBandNames)) {
                final String dimensionNames = memberDescriptor.getDimensionNames();
                final int numDimensions = getNumDimensions(dimensionNames);
                final String variableName = ensureNetCDFName(memberDescriptorName);
                final VariableDescriptor variableDescriptor = new VariableDescriptor(variableName,
                        memberDescriptor.isGridPointData(),
                        DataType.OBJECT,
                        dimensionNames,
                        numDimensions == 2,
                        memberDescriptor.getMemberIndex());

                setDataType(variableDescriptor, memberDescriptor.getDataTypeName());

                variableDescriptor.setUnit(memberDescriptor.getUnit());
                variableDescriptor.setFillValue(memberDescriptor.getFillValue());

                final float scalingFactor = memberDescriptor.getScalingFactor();
                if (scalingFactor != 1.0) {
                    variableDescriptor.setScaleFactor(scalingFactor);
                }

                final float scalingOffset = memberDescriptor.getScalingOffset();
                if (scalingOffset != 0.0) {
                    variableDescriptor.setScaleOffset(memberDescriptor.getScalingOffset());
                }

                final short[] flagMasks = memberDescriptor.getFlagMasks();
                if (flagMasks != null) {
                    variableDescriptor.setFlagMasks(memberDescriptor.getFlagMasks());
                    variableDescriptor.setFlagValues(memberDescriptor.getFlagValues());
                    variableDescriptor.setFlagMeanings(memberDescriptor.getFlagMeanings());
                }

                variableDescriptors.put(variableName, variableDescriptor);
            }
        }
    }

    // package access for testing only tb 2014-07-01
    static Properties extractMetadata(MetadataElement root) {
        final Properties properties = new Properties();
        extractAttributes(root, properties, "");

        return properties;
    }

    private static SmosFile getSmosFile(Product product) {
        final SmosProductReader smosReader = (SmosProductReader) product.getProductReader();
        return (SmosFile) smosReader.getProductFile();
    }

    private static void extractAttributes(MetadataElement root, Properties properties, String prefix) {
        final MetadataAttribute[] attributes = root.getAttributes();
        for (MetadataAttribute attribute : attributes) {
            addAttributeTo(properties, prefix, attribute);
        }

        final MetadataElement[] elements = root.getElements();
        final HashMap<String, List<MetadataElement>> uniqueNamedElements = getListWithUniqueNamedElements(elements);

        final Set<String> nameSet = uniqueNamedElements.keySet();
        for (final String elementName : nameSet) {
            final List<MetadataElement> elementsWithSameName = uniqueNamedElements.get(elementName);
            if (elementsWithSameName.size() == 1) {
                final MetadataElement metadataElement = elementsWithSameName.get(0);
                final String nextRecursionPrefix = prefix + metadataElement.getName() + ":";
                extractAttributes(metadataElement, properties, nextRecursionPrefix);
            } else {
                int index = 0;
                for (final MetadataElement metadataElement : elementsWithSameName) {
                    final String nextRecursionPrefix = prefix + metadataElement.getName() + "_" + Integer.toString(index) + ":";
                    extractAttributes(metadataElement, properties, nextRecursionPrefix);
                    ++index;
                }
            }
        }
    }

    private static void addAttributeTo(Properties properties, String prefix, MetadataAttribute attribute) {
        final String attributeName = prefix + attribute.getName();
        final ProductData data = attribute.getData();
        properties.setProperty(attributeName, data.getElemString());
    }

    private static HashMap<String, List<MetadataElement>> getListWithUniqueNamedElements(MetadataElement[] elements) {
        final HashMap<String, List<MetadataElement>> uniqueNamedElements = new HashMap<>(elements.length);
        for (final MetadataElement element : elements) {
            final String elementName = element.getName();
            final List<MetadataElement> elementList = uniqueNamedElements.get(elementName);
            if (elementList == null) {
                final ArrayList<MetadataElement> uniqueNamedElementsList = new ArrayList<>();
                uniqueNamedElementsList.add(element);
                uniqueNamedElements.put(elementName, uniqueNamedElementsList);
            } else {
                elementList.add(element);
            }
        }
        return uniqueNamedElements;
    }

    // package access for testing only tb 2014-07-30
    static void setDataType(VariableDescriptor variableDescriptor, String dataTypeName) {
        if (StringUtils.isBlank(dataTypeName)) {
            throw new IllegalStateException("datatype not set for '" + variableDescriptor.getName() + "'");
        }

        if ("uint".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.INT);
            variableDescriptor.setUnsigned(true);
        } else if ("int".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.INT);
        } else if ("float".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.FLOAT);
        } else if ("ubyte".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.BYTE);
            variableDescriptor.setUnsigned(true);
        } else if ("ushort".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.SHORT);
            variableDescriptor.setUnsigned(true);
        } else if ("ulong".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.LONG);
            variableDescriptor.setUnsigned(true);
        } else if ("double".equalsIgnoreCase(dataTypeName)) {
            variableDescriptor.setDataType(DataType.DOUBLE);
        } else {
            throw new IllegalArgumentException("unsupported datatype: '" + dataTypeName + "'");
        }
    }

    // package access for testing only tb 2014-07-30
    static int getNumDimensions(String dimensionNames) {
        if (StringUtils.isBlank(dimensionNames)) {
            throw new IllegalArgumentException("empty dimension names");
        }
        final String[] splittednames = StringUtils.split(dimensionNames, ' ');
        return splittednames.length;
    }

    static String ensureNetCDFName(String variableName) {
        return variableName.replace(".", "_");
    }
}
