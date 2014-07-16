package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;
import org.esa.beam.util.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class L1CFormatExporter extends AbstractFormatExporter {

    private int numSnapshots;
    private HashMap<String, Integer> dimensionMap;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

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

        dimensionMap = new HashMap<>();
        dimensionMap.put("n_grid_points", gridPointCount);
        dimensionMap.put("n_bt_data", 300);
        dimensionMap.put("n_radiometric_accuracy", 2);
        dimensionMap.put("n_snapshots", numSnapshots);
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final L1cScienceSmosFile l1cScienceSmosFile = (L1cScienceSmosFile) explorerFile;
        //final CompoundData snapshotData = l1cScienceSmosFile.getSnapshotData(1);

        final VariableWriter[] gridPointVariableWriters = createGridPointVariableWriters(nFileWriteable);

        for (int i = 0; i < gridPointCount; i++) {
            final CompoundData gridPointData = l1cScienceSmosFile.getGridPointData(i);
            final SequenceData btDataList = l1cScienceSmosFile.getBtDataList(i);

            for (VariableWriter writer : gridPointVariableWriters) {
                writer.write(gridPointData, btDataList, i);
            }
        }

        for (VariableWriter writer : gridPointVariableWriters) {
            writer.close();
        }

        final VariableWriter[] snapshotVariableWriters = createSnapshotVariableWriters(nFileWriteable);
        for (int i = 0; i < numSnapshots; i++) {
            final CompoundData snapshotData = l1cScienceSmosFile.getSnapshotData(i);

            for (VariableWriter writer : snapshotVariableWriters) {
                writer.write(snapshotData, null, i);
            }
        }

        for (VariableWriter writer : snapshotVariableWriters) {
            writer.close();
        }
    }

    private VariableWriter[] createGridPointVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();

        final ArrayList<Object> variableWriterList = new ArrayList<>(variableNameKeys.size());
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            if (!variableDescriptor.isGridPointData()) {
                continue;
            }
            final Dimension dimension = extractDimensions(variableDescriptor.getDimensionNames(), dimensionMap);

            variableWriterList.add(VariableWriterFactory.create(nVariable, variableDescriptor, dimension.width, dimension.height));
        }
        return variableWriterList.toArray(new VariableWriter[variableWriterList.size()]);
    }

    private VariableWriter[] createSnapshotVariableWriters(NFileWriteable nFileWriteable) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();

        final ArrayList<Object> variableWriterList = new ArrayList<>(variableNameKeys.size());
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            if (variableDescriptor.isGridPointData()) {
                continue;
            }
            final Dimension dimension = extractDimensions(variableDescriptor.getDimensionNames(), dimensionMap);

            variableWriterList.add(VariableWriterFactory.create(nVariable, variableDescriptor, dimension.width, dimension.height));
        }
        return variableWriterList.toArray(new VariableWriter[variableWriterList.size()]);
    }

    // package access for testing only tb 2014-07-15
    static Dimension extractDimensions(String dimensionNames, HashMap<String, Integer> dimensionMap) {
        final String[] dimNamesArray = StringUtils.split(dimensionNames, new char[]{' '}, true);

        final Dimension dimension = new Dimension();
        dimension.width = dimensionMap.get(dimNamesArray[0]);
        if (dimNamesArray.length > 1) {
            dimension.height = dimensionMap.get(dimNamesArray[1]);
        } else {
            dimension.height = -1;
        }
        return dimension;
    }
}
