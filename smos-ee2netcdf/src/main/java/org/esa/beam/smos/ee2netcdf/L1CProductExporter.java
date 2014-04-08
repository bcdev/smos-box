package org.esa.beam.smos.ee2netcdf;

import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.framework.datamodel.Product;

import java.io.IOException;

class L1CProductExporter extends AbstractFormatExporter {

    private int numSnapshots;

    @Override
    public void initialize(Product product) {
        super.initialize(product);

        final L1cScienceSmosFile scienceSmosFile = (L1cScienceSmosFile) explorerFile;
        final SnapshotInfo snapshotInfo = scienceSmosFile.getSnapshotInfo();
        numSnapshots = snapshotInfo.getSnapshotIds().size();
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("grid_point_count", gridPointCount);
        nFileWriteable.addDimension("bt_data_count", 300);
        nFileWriteable.addDimension("radiometric_accuracy_count", 2);
        nFileWriteable.addDimension("snapshot_count", numSnapshots);
    }

    @Override
    public void addVariables(NFileWriteable nFileWriteable) throws IOException {

    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {

    }
}
