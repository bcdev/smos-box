/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.util.NumberUtils;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.dataio.smos.dddb.Dddb;
import org.esa.beam.dataio.smos.L1cSmosFile;

import java.io.IOException;


class GridPointBtDataset {

    final int gridPointIndex;
    final CompoundType btDataType;
    final String[] columnNames;
    final Class[] columnClasses;
    final Number[][] data;

    static GridPointBtDataset read(L1cSmosFile smosFile, int gridPointIndex) throws IOException {
        SequenceData btDataList = smosFile.getBtDataList(gridPointIndex);

        CompoundType type = (CompoundType) btDataList.getType().getElementType();
        int memberCount = type.getMemberCount();

        int btDataListCount = btDataList.getElementCount();

        final String[] columnNames = new String[memberCount];
        final Class[] columnClasses = new Class[memberCount];
        final BandDescriptor[] descriptors = new BandDescriptor[memberCount];

        for (int j = 0; j < memberCount; j++) {
            final String memberName = type.getMemberName(j);
            columnNames[j] = memberName;
            final BandDescriptor descriptor =
                    Dddb.getInstance().findBandDescriptorForMember(smosFile.getDataFormat().getName(), memberName);
            if (descriptor == null || descriptor.getScalingFactor() == 1.0 && descriptor.getScalingOffset() == 0.0) {
                columnClasses[j] = NumberUtils.getNumericMemberType(type, j);
            } else {
                columnClasses[j] = Double.class;
            }
            descriptors[j] = descriptor;
        }

        final Number[][] tableData = new Number[btDataListCount][memberCount];
        for (int i = 0; i < btDataListCount; i++) {
            CompoundData btData = btDataList.getCompound(i);
            for (int j = 0; j < memberCount; j++) {
                final Number member = NumberUtils.getNumericMember(btData, j);
                final BandDescriptor descriptor = descriptors[j];
                if (descriptor == null || descriptor.getScalingFactor() == 1.0 && descriptor.getScalingOffset() == 0.0) {
                    tableData[i][j] = member;
                } else {
                    tableData[i][j] = member.doubleValue() * descriptor.getScalingFactor() + descriptor.getScalingOffset();
                }
            }
        }

        return new GridPointBtDataset(gridPointIndex, smosFile.getBtDataType(), columnNames, columnClasses, tableData);
    }

    GridPointBtDataset(int gridPointIndex, CompoundType btDataType, String[] columnNames, Class[] columnClasses,
                       Number[][] data) {
        this.gridPointIndex = gridPointIndex;
        this.btDataType = btDataType;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        this.data = data;
    }

    int getColumnIndex(String name) {
        return btDataType.getMemberIndex(name);
    }
}
