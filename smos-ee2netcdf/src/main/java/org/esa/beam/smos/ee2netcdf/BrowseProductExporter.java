package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cBrowseSmosFile;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class BrowseProductExporter extends AbstractFormatExporter {

    private int nBtData;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);
        createVariableDescriptors(exportParameter);

        final String productName = product.getName();
        nBtData = getBtDataDimension(productName);
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
        nFileWriteable.addDimension("n_bt_data", nBtData);
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

    void createVariableDescriptors(ExportParameter exportParameter) throws IOException {
        variableDescriptors = new HashMap<>();

        final List<String> outputBandNames = exportParameter.getOutputBandNames();

        if (mustExport("grid_point_id", outputBandNames)) {
            final VariableDescriptor gpIdDescriptor = new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false, -1);
            gpIdDescriptor.setUnsigned(true);
            variableDescriptors.put("grid_point_id", gpIdDescriptor);
        }

        if (mustExport("lat", outputBandNames)) {
            final VariableDescriptor latDescriptor = new VariableDescriptor("Latitude", true, DataType.FLOAT, "n_grid_points", false, -1);
            latDescriptor.setUnit("degrees_north");
            latDescriptor.setFillValue(-999.f);
            latDescriptor.setValidMin(-90.f);
            latDescriptor.setValidMax(90.f);
            latDescriptor.setOriginalName("Grid_Point_Latitude");
            latDescriptor.setStandardName("latitude");
            variableDescriptors.put("lat", latDescriptor);
        }

        if (mustExport("lon", outputBandNames)) {
            final VariableDescriptor lonDescriptor = new VariableDescriptor("Longitude", true, DataType.FLOAT, "n_grid_points", false, -1);
            lonDescriptor.setUnit("degrees_east");
            lonDescriptor.setFillValue(-999.f);
            lonDescriptor.setValidMin(-180.f);
            lonDescriptor.setValidMax(180.f);
            lonDescriptor.setOriginalName("Grid_Point_Longitude");
            lonDescriptor.setStandardName("longitude");
            variableDescriptors.put("lon", lonDescriptor);
        }


        if (mustExport("grid_point_altitude", outputBandNames)) {
            final VariableDescriptor altitudeDescriptor = new VariableDescriptor("Altitude", true, DataType.FLOAT, "n_grid_points", false, -1);
            altitudeDescriptor.setUnit("m");
            altitudeDescriptor.setFillValue(-999.f);
            variableDescriptors.put("grid_point_altitude", altitudeDescriptor);
        }

        if (mustExport("grid_point_mask", outputBandNames)) {
            final VariableDescriptor gpMaskDescriptor = new VariableDescriptor("Grid_Point_Mask", true, DataType.BYTE, "n_grid_points", false, -1);
            variableDescriptors.put("grid_point_mask", gpMaskDescriptor);
        }

        if (mustExport("bt_data_count", outputBandNames)) {
            final VariableDescriptor btDataCounterDescriptor = new VariableDescriptor("BT_Data_Counter", true, DataType.BYTE, "n_grid_points", false, -1);
            variableDescriptors.put("bt_data_count", btDataCounterDescriptor);
        }

        if (mustExport("flags", outputBandNames)) {
            final VariableDescriptor flagsDescriptor = new VariableDescriptor("Flags", false, DataType.SHORT, "n_grid_points n_bt_data", true, 0);
            flagsDescriptor.setFlagMasks(new short[]{3, 3, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768});
            flagsDescriptor.setFlagValues(new short[]{0, 1, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, (short) 32768});
            flagsDescriptor.setFlagMeanings("pol_xx pol_yy sun_fov sun_glint_fov moon_glint_fov single_snapshot rfi_x sun_point sun_glint_area moon_point af_fov rfi_tails border_fov sun_tails rfi_y rfi_point_source");
            flagsDescriptor.setUnsigned(true);
            variableDescriptors.put("flags", flagsDescriptor);
        }

        if (mustExport("bt_value", outputBandNames)) {
            final VariableDescriptor btValueDescriptor = new VariableDescriptor("BT_Value", false, DataType.FLOAT, "n_grid_points n_bt_data", true, 1);
            btValueDescriptor.setUnit("K");
            btValueDescriptor.setFillValue(-999.f);
            variableDescriptors.put("bt_value", btValueDescriptor);
        }

        if (mustExport("pixel_radiometric_accuracy", outputBandNames)) {
            final VariableDescriptor radAccDescriptor = new VariableDescriptor("Radiometric_Accuracy_of_Pixel", false, DataType.SHORT, "n_grid_points n_bt_data", true, 2);
            radAccDescriptor.setUnit("K");
            radAccDescriptor.setOriginalName("Radiometric_Accuracy_of_Pixel");
            radAccDescriptor.setScaleFactor(0.000762939453125);
            radAccDescriptor.setUnsigned(true);
            variableDescriptors.put("pixel_radiometric_accuracy", radAccDescriptor);
        }

        if (mustExport("azimuth_angle", outputBandNames)) {
            final VariableDescriptor azimuthAngleDescriptor = new VariableDescriptor("Azimuth_Angle", false, DataType.SHORT, "n_grid_points n_bt_data", true, 3);
            azimuthAngleDescriptor.setUnit("degree");
            azimuthAngleDescriptor.setScaleFactor(0.0054931640625);
            azimuthAngleDescriptor.setUnsigned(true);
            variableDescriptors.put("azimuth_angle", azimuthAngleDescriptor);
        }

        if (mustExport("footprint_axis_1", outputBandNames)) {
            final VariableDescriptor fpAxis1Descriptor = new VariableDescriptor("Footprint_Axis1", false, DataType.SHORT, "n_grid_points n_bt_data", true, 4);
            fpAxis1Descriptor.setUnit("km");
            fpAxis1Descriptor.setScaleFactor(0.00152587890625);
            fpAxis1Descriptor.setUnsigned(true);
            variableDescriptors.put("footprint_axis_1", fpAxis1Descriptor);
        }

        if (mustExport("footprint_axis_2", outputBandNames)) {
            final VariableDescriptor fpAxis2Descriptor = new VariableDescriptor("Footprint_Axis2", false, DataType.SHORT, "n_grid_points n_bt_data", true, 5);
            fpAxis2Descriptor.setUnit("km");
            fpAxis2Descriptor.setScaleFactor(0.00152587890625);
            fpAxis2Descriptor.setUnsigned(true);
            variableDescriptors.put("footprint_axis_2", fpAxis2Descriptor);
        }
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);

            variableWriters[index] = VariableWriterFactory.create(nVariable, variableDescriptor, gridPointCount, nBtData);
            index++;
        }
        return variableWriters;
    }
}
