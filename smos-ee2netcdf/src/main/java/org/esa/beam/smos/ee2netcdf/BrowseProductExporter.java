package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
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
    private final Map<String, String> variableNames;

    // ncVariableName               compound member name
    // grid_point_id                Grid_Point_ID
    // lat                          Grid_Point_Latitude
    // lon                          Grid_Point_Longitude
    // grid_point_altitude          Grid_Point_Altitude
    // grid_point_mask              Grid_Point_Mask
    // bt_data_count                BT_Data_Counter
    // flags                        Flags
    // bt_value                     BT_Value
    // pixel_radiometric_accuracy   Radiometric_Accuracy_of_Pixel
    // azimuth_angle                Azimuth_Angle
    // footprint_axis_1             Footprint_Axis1
    // footprint_axis_2             Footprint_Axis2

    BrowseProductExporter() {
        variableNames = new HashMap<>();
        variableNames.put("grid_point_id", "Grid_Point_ID");
        variableNames.put("lat", "Latitude");       // this ia a dddb mapping name, real name is: Grid_Point_Latitude
        variableNames.put("lon", "Longitude");      // this ia a dddb mapping name, real name is: Grid_Point_Longitude
        variableNames.put("grid_point_altitude", "Altitude");   // this ia a dddb mapping name, real name is: Grid_Point_Altitude
        variableNames.put("grid_point_mask", "Grid_Point_Mask");
        variableNames.put("bt_data_count", "BT_Data_Counter");
        variableNames.put("flags", "Flags");
        variableNames.put("bt_value", "BT_Value");
        variableNames.put("pixel_radiometric_accuracy", "Radiometric_Accuracy_of_Pixel");
        variableNames.put("azimuth_angle", "Azimuth_Angle");
        variableNames.put("footprint_axis_1", "Footprint_Axis1");
        variableNames.put("footprint_axis_2", "Footprint_Axis2");
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.addDimension("bt_data_count", 255);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {
        final Set<String> variableNameKeys = variableNames.keySet();
        for (final String ncVariableName : variableNameKeys) {
            final String s = variableNames.get(ncVariableName);
            // @todo 1 tb/tb replace datatype, unsigned, dimensionality and dimension names with real data tb 2014-04-08
            nFileWriteable.addVariable(ncVariableName, DataType.INT, true, null, "grid_point_count");
        }
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final Set<String> variableNameKeys = variableNames.keySet();
        final IntVariableWriter[] variableWriters = new IntVariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName: variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            variableWriters[index] = new IntVariableWriter(nVariable, variableNames.get(ncVariableName), gridPointCount);
            index++;
        }

        final L1cBrowseSmosFile browseFile = (L1cBrowseSmosFile) explorerFile;

        for (int i = 0; i < gridPointCount; i++) {
            final SequenceData btDataList = browseFile.getBtDataList(i);
            final CompoundData gridPointData = explorerFile.getGridPointData(i);
            for (IntVariableWriter writer : variableWriters) {
                writer.write(gridPointData, i);
            }
        }

        for (IntVariableWriter writer : variableWriters) {
            writer.close();
        }
    }
}
