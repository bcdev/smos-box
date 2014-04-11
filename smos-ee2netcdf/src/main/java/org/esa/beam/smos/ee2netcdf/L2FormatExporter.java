package org.esa.beam.smos.ee2netcdf;


import com.bc.ceres.binio.CompoundData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.smos.ee2netcdf.variable.VariableDescriptor;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriter;
import org.esa.beam.smos.ee2netcdf.variable.VariableWriterFactory;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

class L2FormatExporter extends AbstractFormatExporter {

    L2FormatExporter() {
        createVariableDescriptors();
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

    void createVariableDescriptors() {
        variableDescriptors = new HashMap<>();

        final VariableDescriptor gpIdDescriptor = new VariableDescriptor("Grid_Point_ID", true, DataType.INT, "n_grid_points", false, -1);
        gpIdDescriptor.setUnsigned(true);
        variableDescriptors.put("grid_point_id", gpIdDescriptor);

        final VariableDescriptor latDescriptor = new VariableDescriptor("Latitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        latDescriptor.setUnit("degrees_north");
        latDescriptor.setFillValue(-999.f);
        latDescriptor.setValidMin(-90.f);
        latDescriptor.setValidMax(90.f);
        latDescriptor.setOriginalName("Latitude");
        latDescriptor.setStandardName("latitude");
        variableDescriptors.put("lat", latDescriptor);

        final VariableDescriptor lonDescriptor = new VariableDescriptor("Longitude", true, DataType.FLOAT, "n_grid_points", false, -1);
        lonDescriptor.setUnit("degrees_east");
        lonDescriptor.setFillValue(-999.f);
        lonDescriptor.setValidMin(-180.f);
        lonDescriptor.setValidMax(180.f);
        lonDescriptor.setOriginalName("Longitude");
        lonDescriptor.setStandardName("longitude");
        variableDescriptors.put("lon", lonDescriptor);

        final VariableDescriptor ftprtDiamDescriptor = new VariableDescriptor("Equiv_ftprt_diam", true, DataType.FLOAT, "n_grid_points", false, -1);
        ftprtDiamDescriptor.setUnit("m");
        ftprtDiamDescriptor.setFillValue(-999.f);
        variableDescriptors.put("equiv_ftprt_diam", ftprtDiamDescriptor);

        // @todo 3 tb/** mismatch between StructMember name and name in schema file - check and resolve tb 2014-04-11
        final VariableDescriptor acqTimeDescriptor = new VariableDescriptor("Mean_Acq_Time", true, DataType.FLOAT, "n_grid_points", false, -1);
        acqTimeDescriptor.setUnit("dd");
        acqTimeDescriptor.setFillValue(-999.f);
        variableDescriptors.put("mean_acq_time", acqTimeDescriptor);

        final VariableDescriptor sss1Descriptor = new VariableDescriptor("SSS1", true, DataType.FLOAT, "n_grid_points", false, -1);
        sss1Descriptor.setUnit("psu");
        sss1Descriptor.setFillValue(-999.f);
        variableDescriptors.put("sss1", sss1Descriptor);
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
