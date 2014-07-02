package org.esa.beam.smos.ee2netcdf;


import org.apache.commons.lang.StringUtils;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.DateTimeUtils;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import ucar.ma2.Array;

import java.io.IOException;
import java.util.*;

abstract class AbstractFormatExporter implements FormatExporter {

    protected int gridPointCount;
    protected SmosFile explorerFile;
    protected Map<String, VariableDescriptor> variableDescriptors;

    @Override
    public void initialize(Product product) {
        explorerFile = GridPointFormatExporter.getSmosFile(product);
        gridPointCount = explorerFile.getGridPointCount();
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
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        for (final String ncVariableName : variableNameKeys) {
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            final NVariable nVariable = nFileWriteable.addVariable(ncVariableName, variableDescriptor.getDataType(), true, null, variableDescriptor.getDimensionNames());
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
            final String originalName = variableDescriptor.getOriginalName();
            if (StringUtils.isNotBlank(originalName)) {
                nVariable.addAttribute("original_name", originalName);
            }
            final String standardName = variableDescriptor.getStandardName();
            if (StringUtils.isNotBlank(standardName)) {
                nVariable.addAttribute("standard_name", standardName);
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
            if (variableDescriptor.isScaleFactorPresent()) {
                nVariable.addAttribute("scale_factor", variableDescriptor.getScaleFactor());
            }
            if (variableDescriptor.isUnsigned()) {
                nVariable.addAttribute("_Unsigned", "true");
            }
        }
    }

    @Override
    abstract public void addDimensions(NFileWriteable nFileWriteable) throws IOException;

    // @todo 3 tb/tb rethink this. I want to force derived classes to implement this method - as a reminder to create the map.
    // But the method should be private ... tb 014-04-11
    abstract void createVariableDescriptors();

    // package access for testing only tb 2014-07-01
    static Properties extractMetadata(MetadataElement root) {
        final Properties properties = new Properties();
        extractAttributes(root, properties, "");

        return properties;
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
}
