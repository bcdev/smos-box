package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

class L1CProductExporter extends AbstractFormatExporter {

    private int numSnapshots;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);
        createVariableDescriptors(exportParameter);

        final L1cScienceSmosFile scienceSmosFile = (L1cScienceSmosFile) explorerFile;
        final SnapshotInfo snapshotInfo = scienceSmosFile.getSnapshotInfo();
        numSnapshots = snapshotInfo.getSnapshotIds().size();
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
        nFileWriteable.addDimension("n_bt_data", 300);
        nFileWriteable.addDimension("n_radiometric_accuracy", 2);
        nFileWriteable.addDimension("n_snapshots", numSnapshots);
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final VariableWriter[] variableWriters = createVariableWriters(nFileWriteable);

        for (int i = 0; i < gridPointCount; i++) {
            final CompoundData gridPointData = explorerFile.getGridPointData(i);
            for (VariableWriter writer : variableWriters) {
                writer.write(gridPointData, null, i);
            }
        }

        for (VariableWriter writer : variableWriters) {
            writer.close();
        }
    }

    void createVariableDescriptors(ExportParameter exportParameter) {
        variableDescriptors = new HashMap<>();

        final VariableDescriptor gpIdDescriptor = new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false, -1);
        gpIdDescriptor.setUnsigned(true);
        variableDescriptors.put("grid_point_id", gpIdDescriptor);
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();
        final VariableWriter[] variableWriters = new VariableWriter[variableNameKeys.size()];
        int index = 0;
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);

            variableWriters[index] = VariableWriterFactory.create(nVariable, variableDescriptor, gridPointCount, -1);
            index++;
        }
        return variableWriters;
    }
}
