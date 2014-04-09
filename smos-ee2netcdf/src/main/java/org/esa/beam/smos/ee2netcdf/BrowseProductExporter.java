package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cBrowseSmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.SmosUtils;
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

    private void createVariableMap() {
        variableDescriptors = new HashMap<>();
        variableDescriptors.put("grid_point_id", new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false));
        variableDescriptors.put("lat", new VariableDescriptor("Latitude", true, DataType.FLOAT, "n_grid_points", false));       // this ia a dddb mapping name, real name is: Grid_Point_Latitude
        variableDescriptors.put("lon", new VariableDescriptor("Longitude", true, DataType.FLOAT, "n_grid_points", false));      // this ia a dddb mapping name, real name is: Grid_Point_Longitude
        variableDescriptors.put("grid_point_altitude", new VariableDescriptor("Altitude", true, DataType.FLOAT, "n_grid_points", false));   // this ia a dddb mapping name, real name is: Grid_Point_Altitude
        variableDescriptors.put("grid_point_mask", new VariableDescriptor("Grid_Point_Mask", true, DataType.BYTE, "n_grid_points", false));
        variableDescriptors.put("bt_data_count", new VariableDescriptor("BT_Data_Counter", true, DataType.BYTE, "n_grid_points", false));
        variableDescriptors.put("flags", new VariableDescriptor("Flags", false, DataType.SHORT, "n_grid_points n_bt_data", true));
        variableDescriptors.put("bt_value", new VariableDescriptor("BT_Value", false, DataType.FLOAT, "n_grid_points n_bt_data", true));
        variableDescriptors.put("pixel_radiometric_accuracy", new VariableDescriptor("Radiometric_Accuracy_of_Pixel", false, DataType.SHORT, "n_grid_points n_bt_data", true));
        variableDescriptors.put("azimuth_angle", new VariableDescriptor("Azimuth_Angle", false, DataType.SHORT, "n_grid_points n_bt_data", true));
        variableDescriptors.put("footprint_axis_1", new VariableDescriptor("Footprint_Axis1", false, DataType.SHORT, "n_grid_points n_bt_data", true));
        variableDescriptors.put("footprint_axis_2", new VariableDescriptor("Footprint_Axis2", false, DataType.SHORT, "n_grid_points n_bt_data", true));
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
            nFileWriteable.addVariable(ncVariableName, variableDescriptor.getDataType(), true, null, variableDescriptor.getDimensionNames());
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
                        // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                        variableWriters[index] = new FloatVariableSequence2DWriter(nVariable, gridPointCount, nBtData, 0);
                    } else {
                        // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                        variableWriters[index] = new FloatVariableSequenceWriter(nVariable, gridPointCount, 0);
                    }
                } else if (dataType == DataType.INT) {
                    // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                    variableWriters[index] = new IntVariableSequenceWriter(nVariable, gridPointCount, 0);
                } else {
                    if (variableDescriptor.isIs2d()) {
                        // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                        variableWriters[index] = new ShortVariableSequence2DWriter(nVariable, gridPointCount, nBtData, 0);
                    } else {
                        // @todo 1 tb/tb move member index to VariableDescriptor tb 2014-04-09
                        variableWriters[index] = new ShortVariableSequenceWriter(nVariable, gridPointCount, 0);
                    }
                }

            }
            index++;
        }
        return variableWriters;
    }
}
