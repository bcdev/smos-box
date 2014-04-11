package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.framework.datamodel.Product;

import java.io.IOException;
import java.util.HashMap;

class L1CProductExporter extends AbstractFormatExporter {

    private int numSnapshots;

    L1CProductExporter() {
        createVariableDescriptors();
    }

    @Override
    public void initialize(Product product) {
        super.initialize(product);

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

    }

    void createVariableDescriptors() {
        variableDescriptors = new HashMap<>();
    }
}
