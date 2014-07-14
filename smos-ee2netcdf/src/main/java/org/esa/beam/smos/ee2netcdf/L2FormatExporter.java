package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.MemberDescriptor;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class L2FormatExporter extends AbstractFormatExporter {

    private Family<MemberDescriptor> memberDescriptors;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

        memberDescriptors = Dddb.getInstance().getMemberDescriptors(explorerFile.getHeaderFile());
        createVariableDescriptors(exportParameter);
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
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

        final List<String> outputBandNames = exportParameter.getOutputBandNames();

        final List<MemberDescriptor> memberDescriptorList = memberDescriptors.asList();
        for (final MemberDescriptor memberDescriptor : memberDescriptorList) {
            final String memberDescriptorName = memberDescriptor.getName();
            if (mustExport(memberDescriptorName, outputBandNames)) {
                final String dimensionNames = memberDescriptor.getDimensionNames();
                final int numDimensions = getNumDimensions(dimensionNames);
                final String variableName = ensureNetCDFName(memberDescriptorName);
                final VariableDescriptor variableDescriptor = new VariableDescriptor(variableName,
                        memberDescriptor.isGridPointData(),
                        DataType.OBJECT,
                        dimensionNames,
                        numDimensions == 2,
                        memberDescriptor.getMemberIndex(),
                        memberDescriptor.getCompoundIndex());

                setDataType(variableDescriptor, memberDescriptor.getDataTypeName());

                variableDescriptor.setBinXName(memberDescriptor.getBinXName());

                variableDescriptor.setUnit(memberDescriptor.getUnit());
                variableDescriptor.setFillValue(memberDescriptor.getFillValue());
                // @todo 2 tb/tb valid min
                // @todo 2 tb/tb valid max

                final float scalingFactor = memberDescriptor.getScalingFactor();
                if (scalingFactor != 1.0) {
                    variableDescriptor.setScaleFactor(scalingFactor);
                }

                final float scalingOffset = memberDescriptor.getScalingOffset();
                if (scalingOffset != 0.0) {
                    variableDescriptor.setScaleOffset(memberDescriptor.getScalingOffset());
                }

                final short[] flagMasks = memberDescriptor.getFlagMasks();
                if (flagMasks != null) {
                    variableDescriptor.setFlagMasks(memberDescriptor.getFlagMasks());
                    variableDescriptor.setFlagValues(memberDescriptor.getFlagValues());
                    variableDescriptor.setFlagMeanings(memberDescriptor.getFlagMeanings());
                }

                variableDescriptors.put(variableName, variableDescriptor);
            }
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
