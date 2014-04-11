package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.apache.commons.lang.StringUtils;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cBrowseSmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.smos.ee2netcdf.variable.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class BrowseProductExporter extends AbstractFormatExporter {

    private Map<String, VariableDescriptor> variableDescriptors;
    private int nBtData;

    BrowseProductExporter() {
        createVariableMap();
    }

    @Override
    public void initialize(Product product) {
        super.initialize(product);

        // @todo 2 tb/tb extract method and write tests tb 2014-04-09
        final String productName = product.getName();
        nBtData = getBtDataDimension(productName);

    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
        nFileWriteable.addDimension("n_bt_data", nBtData);
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
            if (StringUtils.isNotBlank(flagMeanings))                 {
                nVariable.addAttribute("flag_meanings", flagMeanings);
            }
            if (variableDescriptor.isScaleFactorPresent()) {
                nVariable.addAttribute("scale_factor", variableDescriptor.getScaleFactor());
            }
            if (variableDescriptor.isUnsigned())                                            {
                nVariable.addAttribute("_Unsigned", "true");
            }
        }
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final VariableWriter[] variableWriters = createVariableWriters(nFileWriteable);

        final L1cBrowseSmosFile browseFile = (L1cBrowseSmosFile) explorerFile;

        for (int i = 0; i < gridPointCount; i++) {
            final SequenceData btDataList = browseFile.getBtDataList(i);
            final CompoundData gridPointData = explorerFile.getGridPointData(i);
            for (VariableWriter writer : variableWriters) {
                writer.write(gridPointData, btDataList, i);
            }
        }

        for (VariableWriter writer : variableWriters) {
            writer.close();
        }
    }

    // static access for testing only tb 2014-04-10
    static int getBtDataDimension(String productName) {
        final String productType = SmosUtils.getProductType(productName);
        if ("MIR_BWLF1C".equalsIgnoreCase(productType) || "MIR_BWNF1C".equalsIgnoreCase(productType)) {
            return 4;
        } else if ("MIR_BWLD1C".equalsIgnoreCase(productType) || "MIR_BWND1C".equalsIgnoreCase(productType)) {
            return 2;
        } else {
            throw new IllegalArgumentException("unsupported product: " + productName);
        }
    }

    private void createVariableMap() {
        variableDescriptors = new HashMap<>();
        final VariableDescriptor gpIdDescriptor = new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false, -1);
        gpIdDescriptor.setUnsigned(true);
        variableDescriptors.put("grid_point_id", gpIdDescriptor);

        final VariableDescriptor latDescriptor = new VariableDescriptor("Latitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        latDescriptor.setUnit("degrees_north");
        latDescriptor.setFillValue(-999.f);
        latDescriptor.setValidMin(-90.f);
        latDescriptor.setValidMax(90.f);
        latDescriptor.setOriginalName("Grid_Point_Latitude");
        latDescriptor.setStandardName("latitude");
        variableDescriptors.put("lat", latDescriptor);

        final VariableDescriptor lonDescriptor = new VariableDescriptor("Longitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        lonDescriptor.setUnit("degrees_north");
        lonDescriptor.setFillValue(-999.f);
        lonDescriptor.setValidMin(-180.f);
        lonDescriptor.setValidMax(180.f);
        lonDescriptor.setOriginalName("Grid_Point_Longitude");
        lonDescriptor.setStandardName("longitude");
        variableDescriptors.put("lon", lonDescriptor);

        final VariableDescriptor altitudeDescriptor = new VariableDescriptor("Altitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        altitudeDescriptor.setUnit("m");
        altitudeDescriptor.setFillValue(-999.f);
        variableDescriptors.put("grid_point_altitude", altitudeDescriptor);

        variableDescriptors.put("grid_point_mask", new VariableDescriptor("Grid_Point_Mask", true, DataType.BYTE, "n_grid_points", false, -1));
        variableDescriptors.put("bt_data_count", new VariableDescriptor("BT_Data_Counter", true, DataType.BYTE, "n_grid_points", false, -1));

        final VariableDescriptor flagsDescriptor = new VariableDescriptor("Flags", false, DataType.SHORT, "n_grid_points n_bt_data", true, 0);
        flagsDescriptor.setFlagMasks(new short[]{3, 3, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short)32768});
        flagsDescriptor.setFlagValues(new short[]{0, 1, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short)32768});
        flagsDescriptor.setFlagMeanings("pol_xx pol_yy sun_fov sun_glint_fov moon_glint_fov single_snapshot rfi_x sun_point sun_glint_area moon_point af_fov rfi_tails border_fov sun_tails rfi_y rfi_point_source");
        flagsDescriptor.setUnsigned(true);
        variableDescriptors.put("flags", flagsDescriptor);

        final VariableDescriptor btValueDescriptor = new VariableDescriptor("BT_Value", false, DataType.FLOAT, "n_grid_points n_bt_data", true, 1);
        btValueDescriptor.setUnit("K");
        btValueDescriptor.setFillValue(-999.f);
        variableDescriptors.put("bt_value", btValueDescriptor);

        final VariableDescriptor radAccDescriptor = new VariableDescriptor("Radiometric_Accuracy_of_Pixel", false, DataType.SHORT, "n_grid_points n_bt_data", true, 2);
        radAccDescriptor.setUnit("K");
        radAccDescriptor.setOriginalName("Radiometric_Accuracy_of_Pixel");
        radAccDescriptor.setScaleFactor(0.000762939453125);
        radAccDescriptor.setUnsigned(true);
        variableDescriptors.put("pixel_radiometric_accuracy", radAccDescriptor);

        final VariableDescriptor azimuthAngleDescriptor = new VariableDescriptor("Azimuth_Angle", false, DataType.SHORT, "n_grid_points n_bt_data", true, 3);
        azimuthAngleDescriptor.setUnit("degree");
        azimuthAngleDescriptor.setScaleFactor(0.0054931640625);
        azimuthAngleDescriptor.setUnsigned(true);
        variableDescriptors.put("azimuth_angle", azimuthAngleDescriptor);

        final VariableDescriptor fpAxis1Descriptor = new VariableDescriptor("Footprint_Axis1", false, DataType.SHORT, "n_grid_points n_bt_data", true, 4);
        fpAxis1Descriptor.setUnit("km");
        fpAxis1Descriptor.setScaleFactor(0.00152587890625);
        fpAxis1Descriptor.setUnsigned(true);
        variableDescriptors.put("footprint_axis_1", fpAxis1Descriptor);

        final VariableDescriptor fpAxis2Descriptor = new VariableDescriptor("Footprint_Axis2", false, DataType.SHORT, "n_grid_points n_bt_data", true, 5);
        fpAxis2Descriptor.setUnit("km");
        fpAxis2Descriptor.setScaleFactor(0.00152587890625);
        fpAxis2Descriptor.setUnsigned(true);
        variableDescriptors.put("footprint_axis_2", fpAxis2Descriptor);
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);

            variableWriters[index] = VariableWriterFactory.createVariableWriter(nVariable, variableDescriptor, gridPointCount, nBtData);
            index++;
        }
        return variableWriters;
    }

}
