package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cBrowseSmosFile;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.dddb.Family;
import org.esa.beam.dataio.smos.dddb.MemberDescriptor;
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
    private Family<MemberDescriptor> memberDescriptors;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

        memberDescriptors = Dddb.getInstance().getMemberDescriptors(explorerFile.getHeaderFile());
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

        final List<MemberDescriptor> memberDescriptorList = memberDescriptors.asList();
        for (final MemberDescriptor memberDescriptor : memberDescriptorList) {
            final String memberDescriptorName = memberDescriptor.getName();
            if (mustExport(memberDescriptorName, outputBandNames)) {
                final String dimensionNames = memberDescriptor.getDimensionNames();
                final int numDimensions = getNumDimensions(dimensionNames);
                final VariableDescriptor variableDescriptor = new VariableDescriptor(memberDescriptorName,
                        memberDescriptor.isGridPointData(),
                        DataType.OBJECT,
                        dimensionNames,
                        numDimensions == 2,
                        memberDescriptor.getMemberIndex());

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
                // @todo 1 tb/tb add scaling offset

                final short[] flagMasks = memberDescriptor.getFlagMasks();
                if (flagMasks != null) {
                    variableDescriptor.setFlagMasks(memberDescriptor.getFlagMasks());
                    variableDescriptor.setFlagValues(memberDescriptor.getFlagValues());
                    variableDescriptor.setFlagMeanings(memberDescriptor.getFlagMeanings());
                }

                variableDescriptors.put(memberDescriptorName, variableDescriptor);
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

            variableWriters[index] = VariableWriterFactory.create(nVariable, variableDescriptor, gridPointCount, nBtData);
            index++;
        }
        return variableWriters;
    }
}
