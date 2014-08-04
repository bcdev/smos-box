package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.SmosUtils;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;

import java.io.IOException;
import java.util.Set;

class L2FormatExporter extends AbstractFormatExporter {


    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

        applyChiSquareScalingIfNecessary(product);
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final VariableWriter[] variableWriters = createVariableWriters(nFileWriteable);

        if (gpIndexList == null) {
            for (int i = 0; i < gridPointCount; i++) {
                writeCompound(i, i, variableWriters);
            }
        } else {
            int writeIndex = 0;
            for (int index : gpIndexList) {
                writeCompound(index, writeIndex, variableWriters);
                ++writeIndex;
            }
        }

        for (VariableWriter writer : variableWriters) {
            writer.close();
        }
    }

    private void applyChiSquareScalingIfNecessary(Product product) {
        final String productType = product.getProductType();
        if (SmosUtils.isSmUserFormat(productType)) {
            final MetadataElement metadataRoot = product.getMetadataRoot();
            final MetadataElement variableHeader = metadataRoot.getElement("Variable_Header");
            if (variableHeader == null) {
                return;
            }

            final MetadataElement specificProductHeader = variableHeader.getElement("Specific_Product_Header");
            if (specificProductHeader == null) {
                return;
            }

            final MetadataAttribute chi2ScaleAttribute = specificProductHeader.getAttribute("Chi_2_Scale");
            if (chi2ScaleAttribute == null) {
                return;
            }

            final double scaleFactor = chi2ScaleAttribute.getData().getElemDouble();
            if (scaleFactor != 1.0) {
                final VariableDescriptor chi_2_variable = variableDescriptors.get("Chi_2");
                final double originalScaleFactor = chi_2_variable.getScaleFactor();
                chi_2_variable.setScaleFactor(originalScaleFactor * scaleFactor);
            }
        }
    }

    private void writeCompound(int readIndex, int writeIndex, VariableWriter[] variableWriters) throws IOException {
        final CompoundData gridPointData = explorerFile.getGridPointData(readIndex);
        for (VariableWriter writer : variableWriters) {
            writer.write(gridPointData, null, writeIndex);
        }
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
