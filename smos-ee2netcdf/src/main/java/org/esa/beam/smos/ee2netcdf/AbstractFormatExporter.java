package org.esa.beam.smos.ee2netcdf;


import org.apache.commons.lang.StringUtils;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.DateTimeUtils;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import ucar.ma2.Array;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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
    public void addGlobalAttributes(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addGlobalAttribute("Conventions", "CF-1.6");
        nFileWriteable.addGlobalAttribute("title", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("institution", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("contact", "TBD");  // @todo 2 tb/tb replace with meaningful value tb 2014-04-07
        nFileWriteable.addGlobalAttribute("creation_date", DateTimeUtils.toFixedHeaderFormat(new Date()));
        nFileWriteable.addGlobalAttribute("total_number_of_grid_points", Integer.toString(gridPointCount));
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
}
