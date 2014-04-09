package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cBrowseSmosFile;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class BrowseProductExporter extends AbstractFormatExporter {
    private Map<String, VariableDescriptor> variableDescriptors;

    BrowseProductExporter() {
        createVariableMap();
    }

    private void createVariableMap() {
        variableDescriptors = new HashMap<>();
        variableDescriptors.put("grid_point_id", new VariableDescriptor("Grid_Point_ID", true, false, DataType.INT));
        variableDescriptors.put("lat", new VariableDescriptor("Latitude", true, true, DataType.FLOAT));       // this ia a dddb mapping name, real name is: Grid_Point_Latitude
        variableDescriptors.put("lon", new VariableDescriptor("Longitude", true, true, DataType.FLOAT));      // this ia a dddb mapping name, real name is: Grid_Point_Longitude
        variableDescriptors.put("grid_point_altitude", new VariableDescriptor("Altitude", true, true, DataType.FLOAT));   // this ia a dddb mapping name, real name is: Grid_Point_Altitude
        variableDescriptors.put("grid_point_mask", new VariableDescriptor("Grid_Point_Mask", true, false, DataType.CHAR));
        variableDescriptors.put("bt_data_count", new VariableDescriptor("BT_Data_Counter", true, false, DataType.CHAR));
        variableDescriptors.put("flags", new VariableDescriptor("Flags", false, false, DataType.SHORT));
        variableDescriptors.put("bt_value", new VariableDescriptor("BT_Value", false, true, DataType.FLOAT));
        variableDescriptors.put("pixel_radiometric_accuracy", new VariableDescriptor("Radiometric_Accuracy_of_Pixel", false, false, DataType.SHORT));
        variableDescriptors.put("azimuth_angle", new VariableDescriptor("Azimuth_Angle", false, false, DataType.SHORT));
        variableDescriptors.put("footprint_axis_1", new VariableDescriptor("Footprint_Axis1", false, false, DataType.SHORT));
        variableDescriptors.put("footprint_axis_2", new VariableDescriptor("Footprint_Axis2", false, false, DataType.SHORT));
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
        nFileWriteable.addDimension("n_bt_data", 255);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        for (final String ncVariableName : variableNameKeys) {
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            // @todo 1 tb/tb replace datatype, unsigned, dimensionality and dimension names with real data tb 2014-04-08
            if (variableDescriptor.isFloatValue() && variableDescriptor.isGridPointData()) {
                // @todo 1 tb/tb remove grid point constraint tb 2014-04-09
                nFileWriteable.addVariable(ncVariableName, DataType.FLOAT, true, null, "n_grid_points");
            } else {
                nFileWriteable.addVariable(ncVariableName, DataType.INT, true, null, "n_grid_points");
            }
        }
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            if (variableDescriptor.isGridPointData()) {
                if (variableDescriptor.isFloatValue()) {
                    variableWriters[index] = new FloatVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                } else {
                    variableWriters[index] = new IntVariableGridPointWriter(nVariable, variableDescriptor.getName(), gridPointCount);
                }
            } else {
                // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                variableWriters[index] = new IntVariableSequenceWriter(nVariable, gridPointCount, 0);
            }
            index++;
        }

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
}
