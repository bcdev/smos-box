package org.esa.beam.smos.ee2netcdf;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.smos.L1cScienceSmosFile;
import org.esa.beam.dataio.smos.SnapshotInfo;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.smos.ee2netcdf.geometry.GeometryFilter;
import org.esa.beam.smos.ee2netcdf.geometry.GeometryFilterFactory;
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

    private int numSnapshotsToExport;
    private int numSnapshotsInInput;
    private HashMap<String, Integer> dimensionMap;
    private ArrayList<Integer> snapshotIdList;
    private L1cScienceSmosFile scienceSmosFile;

    @Override
    public void initialize(Product product, ExportParameter exportParameter) throws IOException {
        super.initialize(product, exportParameter);

        scienceSmosFile = (L1cScienceSmosFile) explorerFile;
        final SnapshotInfo snapshotInfo = scienceSmosFile.getSnapshotInfo();
        numSnapshotsToExport = snapshotInfo.getSnapshotIds().size();
        numSnapshotsInInput = numSnapshotsToExport;

        snapshotIdList = new ArrayList<>();

        applyScalingFromHeaderFile(product);
    }

    private void applyScalingFromHeaderFile(Product product) {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement variableHeader = metadataRoot.getElement("Variable_Header");
        if (variableHeader == null) {
            return;
        }

        final MetadataElement specificProductHeader = variableHeader.getElement("Specific_Product_Header");
        if (specificProductHeader == null) {
            return;
        }

        final MetadataAttribute radiometricAccuracyAttribute = specificProductHeader.getAttribute("Radiometric_Accuracy_Scale");
        if (radiometricAccuracyAttribute != null) {
            final double scaleFactor = radiometricAccuracyAttribute.getData().getElemDouble();
            if (scaleFactor != 1.0) {
                double originalScaleFactor;
                final VariableDescriptor radiometricAccuracyVariable = variableDescriptors.get("Radiometric_Accuracy");
                if (radiometricAccuracyVariable != null) {
                    originalScaleFactor = radiometricAccuracyVariable.getScaleFactor();
                    radiometricAccuracyVariable.setScaleFactor(originalScaleFactor * scaleFactor);
                }

                final VariableDescriptor accuracyOfPixelVariable = variableDescriptors.get("Radiometric_Accuracy_of_Pixel");
                if (accuracyOfPixelVariable != null) {
                    originalScaleFactor = accuracyOfPixelVariable.getScaleFactor();
                    accuracyOfPixelVariable.setScaleFactor(originalScaleFactor * scaleFactor);
                }
            }
        }

        final MetadataAttribute pixelFootprintAttribute = specificProductHeader.getAttribute("Pixel_Footprint_Scale");
        if (pixelFootprintAttribute != null) {
            final double scaleFactor = pixelFootprintAttribute.getData().getElemDouble();
            if (scaleFactor != 1.0) {
                final VariableDescriptor footprintAxis1Variable = variableDescriptors.get("Footprint_Axis1");
                if (footprintAxis1Variable != null) {
                    final double originalScaleFactor = footprintAxis1Variable.getScaleFactor();
                    footprintAxis1Variable.setScaleFactor(originalScaleFactor * scaleFactor);
                }

                final VariableDescriptor footprintAxis2Variable = variableDescriptors.get("Footprint_Axis2");
                if (footprintAxis2Variable != null) {
                    final double originalScaleFactor = footprintAxis2Variable.getScaleFactor();
                    footprintAxis2Variable.setScaleFactor(originalScaleFactor * scaleFactor);
                }
            }
        }
    }

    @Override
    public void prepareGeographicSubset(NFileWriteable nFileWriteable, ExportParameter exportParameter) throws IOException {
        if (exportParameter.getRegion() != null) {
            final GeometryFilter geometryFilter = GeometryFilterFactory.create(exportParameter.getRegion());
            gpIndexList = new ArrayList<>(gridPointCount);
            for (int i = 0; i < gridPointCount; i++) {
                final CompoundData gridPointData = explorerFile.getGridPointData(i);
                if (geometryFilter.accept(gridPointData)) {
                    gpIndexList.add(i);

                    final SequenceData btDataList = scienceSmosFile.getBtDataList(i);
                    final int elementCount = btDataList.getElementCount();
                    for (int j = 0; j < elementCount; j++) {
                        final CompoundData btData = btDataList.getCompound(j);
                        final int snapshotId = btData.getInt("Snapshot_ID_of_Pixel");
                        if (!snapshotIdList.contains(snapshotId)) {
                            snapshotIdList.add(snapshotId);
                        }
                    }
                }
            }

            gridPointCount = gpIndexList.size();
            numSnapshotsToExport = snapshotIdList.size();
        }
    }

    @Override
    public void addDimensions(NFileWriteable nFileWriteable) throws IOException {
        nFileWriteable.addDimension("n_grid_points", gridPointCount);
        nFileWriteable.addDimension("n_bt_data", 300);
        nFileWriteable.addDimension("n_radiometric_accuracy", 2);
        nFileWriteable.addDimension("n_snapshots", numSnapshotsToExport);

        dimensionMap = new HashMap<>();
        dimensionMap.put("n_grid_points", gridPointCount);
        dimensionMap.put("n_bt_data", 300);
        dimensionMap.put("n_radiometric_accuracy", 2);
        dimensionMap.put("n_snapshots", numSnapshotsToExport);
    }

    @Override
    public void writeData(NFileWriteable nFileWriteable) throws IOException {
        final L1cScienceSmosFile l1cScienceSmosFile = (L1cScienceSmosFile) explorerFile;

        writeGridPointVariables(nFileWriteable);
        writeSnapshotVariables(nFileWriteable, l1cScienceSmosFile);
    }

    private void writeSnapshotVariables(NFileWriteable nFileWriteable, L1cScienceSmosFile l1cScienceSmosFile) throws IOException {
        final VariableWriter[] snapshotVariableWriters = createVariableWriters(nFileWriteable, false);
        if (snapshotIdList.isEmpty()) {
            writeAllSnapshots(l1cScienceSmosFile, snapshotVariableWriters);
        } else {
            writeSnapshotSubset(l1cScienceSmosFile, snapshotVariableWriters);
        }
    }

    private void writeAllSnapshots(L1cScienceSmosFile l1cScienceSmosFile, VariableWriter[] snapshotVariableWriters) throws IOException {
        for (int i = 0; i < numSnapshotsInInput; i++) {
            final CompoundData snapshotData = l1cScienceSmosFile.getSnapshotData(i);
            final SequenceData radiometricAccuracy = snapshotData.getSequence("Radiometric_Accuracy");

            for (VariableWriter writer : snapshotVariableWriters) {
                writer.write(snapshotData, radiometricAccuracy, i);
            }
        }

        for (VariableWriter writer : snapshotVariableWriters) {
            writer.close();
        }
    }

    private void writeSnapshotSubset(L1cScienceSmosFile l1cScienceSmosFile, VariableWriter[] snapshotVariableWriters) throws IOException {
        int writeIndex = 0;
        for (int i = 0; i < numSnapshotsInInput; i++) {
            final CompoundData snapshotData = l1cScienceSmosFile.getSnapshotData(i);

            final int snapshotId = snapshotData.getInt("Snapshot_ID");
            if (!snapshotIdList.contains(snapshotId)) {
                continue;
            }

            final SequenceData radiometricAccuracy = snapshotData.getSequence("Radiometric_Accuracy");
            for (VariableWriter writer : snapshotVariableWriters) {
                writer.write(snapshotData, radiometricAccuracy, writeIndex);
            }

            ++writeIndex;
        }

        for (VariableWriter writer : snapshotVariableWriters) {
            writer.close();
        }
    }

    private void writeGridPointVariables(NFileWriteable nFileWriteable) throws IOException {
        final VariableWriter[] gridPointVariableWriters = createVariableWriters(nFileWriteable, true);

        if (gpIndexList == null) {
            for (int i = 0; i < gridPointCount; i++) {
                writeGridPointData(i, i, gridPointVariableWriters);
            }
        } else {
            int writeIndex = 0;
            for (int index : gpIndexList) {
                writeGridPointData(index, writeIndex, gridPointVariableWriters);
                ++writeIndex;
            }
        }

        for (VariableWriter writer : gridPointVariableWriters) {
            writer.close();
        }
    }

    private void writeGridPointData(int readIndex, int writeIndex, VariableWriter[] gridPointVariableWriters) throws IOException {
        final CompoundData gridPointData = scienceSmosFile.getGridPointData(readIndex);
        final SequenceData btDataList = scienceSmosFile.getBtDataList(readIndex);

        for (VariableWriter writer : gridPointVariableWriters) {
            writer.write(gridPointData, btDataList, writeIndex);
        }
    }

    private VariableWriter[] createVariableWriters(NFileWriteable nFileWriteable, boolean gridPointData) {
        final Set<String> variableNameKeys = variableDescriptors.keySet();

        final ArrayList<Object> variableWriterList = new ArrayList<>(variableNameKeys.size());
        for (final String ncVariableName : variableNameKeys) {
            final NVariable nVariable = nFileWriteable.findVariable(ncVariableName);
            final VariableDescriptor variableDescriptor = variableDescriptors.get(ncVariableName);
            if (gridPointData != variableDescriptor.isGridPointData()) {
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
