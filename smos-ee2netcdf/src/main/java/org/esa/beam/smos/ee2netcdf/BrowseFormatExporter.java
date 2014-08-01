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

import java.io.IOException;
import java.util.Set;

class BrowseFormatExporter extends AbstractFormatExporter {

    private int nBtData;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

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

        if (gpIndexList == null) {
            for (int i = 0; i < gridPointCount; i++) {
                writeGridPointAt(i, i, variableWriters, browseFile);
            }
        } else {
            int writeIndex = 0;
            for (int index : gpIndexList) {
                writeGridPointAt(index, writeIndex, variableWriters, browseFile);
                ++writeIndex;
            }
        }

        for (VariableWriter writer : variableWriters) {
            writer.close();
        }
    }

    private void writeGridPointAt(int readIndex, int writeIndex, VariableWriter[] variableWriters, L1cBrowseSmosFile browseFile) throws IOException {
        final SequenceData btDataList = browseFile.getBtDataList(readIndex);
        final CompoundData gridPointData = explorerFile.getGridPointData(readIndex);

        for (VariableWriter writer : variableWriters) {
            writer.write(gridPointData, btDataList, writeIndex);
        }
    }

    // static access for testing only tb 2014-04-10
    static int getBtDataDimension(String productName) {
        final String productType = SmosUtils.getProductType(productName);
        if ("MIR_BWLF1C".equalsIgnoreCase(productType) || "MIR_BWNF1C".equalsIgnoreCase(productType) || "MIR_BWSF1C".equalsIgnoreCase(productType)) {
            return 4;
        } else if ("MIR_BWLD1C".equalsIgnoreCase(productType) || "MIR_BWND1C".equalsIgnoreCase(productType) || "MIR_BWSD1C".equalsIgnoreCase(productType)) {
            return 2;
        } else {
            throw new IllegalArgumentException("unsupported product: " + productName);
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
