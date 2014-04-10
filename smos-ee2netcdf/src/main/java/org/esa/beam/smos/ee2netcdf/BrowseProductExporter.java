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
        final String productType = SmosUtils.getProductType(product.getName());
        if ("MIR_BWLF1C".equalsIgnoreCase(productType) || "MIR_BWNF1C".equalsIgnoreCase(productType)) {
            nBtData = 4;
        } else if ("MIR_BWLD1C".equalsIgnoreCase(productType) || "MIR_BWND1C".equalsIgnoreCase(productType)) {
            nBtData = 2;
        } else {
            throw new IllegalArgumentException("unsupported product: " + product.getName());
        }

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
            // @todo 1 tb/tb replace unsigned with real data tb 2014-04-08
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

    private void createVariableMap() {
        variableDescriptors = new HashMap<>();
        final VariableDescriptor gpIdDescriptor = new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false, -1);
        variableDescriptors.put("grid_point_id", gpIdDescriptor);

        final VariableDescriptor latDescriptor = new VariableDescriptor("Latitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        latDescriptor.setUnit("degrees_north");
        latDescriptor.setFillValue(-999.f);
        latDescriptor.setValidMin(-90.f);
        variableDescriptors.put("lat", latDescriptor);       // this ia a dddb mapping name, real name is: Grid_Point_Latitude

        final VariableDescriptor lonDescriptor = new VariableDescriptor("Longitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        lonDescriptor.setUnit("degrees_north");
        lonDescriptor.setFillValue(-999.f);
        lonDescriptor.setValidMin(-180.f);
        variableDescriptors.put("lon", lonDescriptor);      // this ia a dddb mapping name, real name is: Grid_Point_Longitude

        final VariableDescriptor altitudeDescriptor = new VariableDescriptor("Altitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        altitudeDescriptor.setUnit("m");
        altitudeDescriptor.setFillValue(-999.f);
        variableDescriptors.put("grid_point_altitude", altitudeDescriptor);   // this ia a dddb mapping name, real name is: Grid_Point_Altitude

        variableDescriptors.put("grid_point_mask", new VariableDescriptor("Grid_Point_Mask", true, DataType.BYTE, "n_grid_points", false, -1));
        variableDescriptors.put("bt_data_count", new VariableDescriptor("BT_Data_Counter", true, DataType.BYTE, "n_grid_points", false, -1));
        variableDescriptors.put("flags", new VariableDescriptor("Flags", false, DataType.SHORT, "n_grid_points n_bt_data", true, 0));

        final VariableDescriptor btValueDescriptor = new VariableDescriptor("BT_Value", false, DataType.FLOAT, "n_grid_points n_bt_data", true, 1);
        btValueDescriptor.setUnit("K");
        btValueDescriptor.setFillValue(-999.f);
        variableDescriptors.put("bt_value", btValueDescriptor);

        final VariableDescriptor radAccDescriptor = new VariableDescriptor("Radiometric_Accuracy_of_Pixel", false, DataType.SHORT, "n_grid_points n_bt_data", true, 2);
        radAccDescriptor.setUnit("K");
        variableDescriptors.put("pixel_radiometric_accuracy", radAccDescriptor);

        final VariableDescriptor azimuthAngleDescriptor = new VariableDescriptor("Azimuth_Angle", false, DataType.SHORT, "n_grid_points n_bt_data", true, 3);
        azimuthAngleDescriptor.setUnit("degree");
        variableDescriptors.put("azimuth_angle", azimuthAngleDescriptor);

        final VariableDescriptor fpAxis1Descriptor = new VariableDescriptor("Footprint_Axis1", false, DataType.SHORT, "n_grid_points n_bt_data", true, 4);
        fpAxis1Descriptor.setUnit("km");
        variableDescriptors.put("footprint_axis_1", fpAxis1Descriptor);

        final VariableDescriptor fpAxis2Descriptor = new VariableDescriptor("Footprint_Axis2", false, DataType.SHORT, "n_grid_points n_bt_data", true, 5);
        fpAxis2Descriptor.setUnit("km");
        variableDescriptors.put("footprint_axis_2", fpAxis2Descriptor);
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            final DataType dataType = variableDescriptor.getDataType();
            if (variableDescriptor.isGridPointData()) {
                if (dataType == DataType.FLOAT) {
                    variableWriters[index] = new FloatVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                } else if (dataType == DataType.INT) {
                    variableWriters[index] = new IntVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                } else if (dataType == DataType.SHORT) {
                    variableWriters[index] = new ShortVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                } else {
                    variableWriters[index] = new ByteVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                }
            } else {
                if (dataType == DataType.FLOAT) {
                    if (variableDescriptor.isIs2d()) {
                        variableWriters[index] = new FloatVariableSequence2DWriter(nVariable, gridPointCount, nBtData, variableDescriptor.getBtDataMemberIndex());
                    } else {
                        variableWriters[index] = new FloatVariableSequenceWriter(nVariable, gridPointCount, variableDescriptor.getBtDataMemberIndex());
                    }
                } else if (dataType == DataType.INT) {
                    variableWriters[index] = new IntVariableSequenceWriter(nVariable, gridPointCount, variableDescriptor.getBtDataMemberIndex());
                } else {
                    if (variableDescriptor.isIs2d()) {
                        variableWriters[index] = new ShortVariableSequence2DWriter(nVariable, gridPointCount, nBtData, variableDescriptor.getBtDataMemberIndex());
                    } else {
                        variableWriters[index] = new ShortVariableSequenceWriter(nVariable, gridPointCount, variableDescriptor.getBtDataMemberIndex());
                    }
                }

            }
            index++;
        }
        return variableWriters;
    }
}
