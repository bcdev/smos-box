/*
 * $Id: $
 *
 * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.CompoundMember;
import com.bc.ceres.binio.CompoundType;
import com.bc.ceres.binio.SequenceData;
import com.bc.ceres.binio.SequenceType;
import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;
import org.esa.beam.dataio.smos.BandInfo;
import org.esa.beam.dataio.smos.BandInfoRegistry;
import org.esa.beam.dataio.smos.SmosDggFile;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Exports grid cells to CSV
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS-Box 2.0
 */
class CsvExportStream implements GridPointFilterStream {

    private final PrintWriter printWriter;
    private final String separator;

    public CsvExportStream(PrintWriter printWriter, String separator) {
        this.printWriter = printWriter;
        this.separator = separator;
    }

    @Override
    public void startFile(SmosDggFile smosfile) {
        printWriter.println(smosfile.getFile().getName());

        printTypeHeader(smosfile.getGridPointType());
        printWriter.println();
    }

    private void printTypeHeader(Type type) {
        if (type.isCompoundType()) {
            CompoundType compoundType = (CompoundType) type;
            int memberCount = compoundType.getMemberCount();
            for (int i = 0; i < memberCount; i++) {
                CompoundMember member = compoundType.getMember(i);
                if (member.getType().isSimpleType()) {
                    printWriter.print(member.getName());
                } else {
                    printTypeHeader(member.getType());
                }
                if (i < memberCount - 1) {
                    printWriter.print(separator);
                }
            }
        } else if (type.isSequenceType()) {
            SequenceType sequenceType = (SequenceType) type;
            Type elementType = sequenceType.getElementType();
            printTypeHeader(elementType);
        }
    }

    @Override
    public void stopFile(SmosDggFile smosfile) {
        printWriter.println("-----------------------------------------------");
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        int btDataIndex = gridPointData.getMemberIndex("BT_Data_List");
        if (btDataIndex != -1) {
            SequenceData sequence = gridPointData.getSequence(btDataIndex);
            for (int i = 0; i < sequence.getElementCount(); i++) {
                writeCompound(gridPointData);
                writeCompound(sequence.getCompound(i));
                printWriter.println();
            }
        } else {
            writeCompound(gridPointData);
            printWriter.println();
        }
    }

    private void writeCompound(CompoundData compoundData) throws IOException {
        int memberCount = compoundData.getMemberCount();
        CompoundType gridPointType = compoundData.getType();
        for (int i = 0; i < memberCount; i++) {
            CompoundMember member = gridPointType.getMember(i);
            if (member.getType().isSimpleType()) {
                String memberName = member.getName();
                if (member.getType() == SimpleType.DOUBLE || member.getType() == SimpleType.FLOAT) {
                    printWriter.print(compoundData.getDouble(i));
                } else {
                    BandInfo bandInfo = BandInfoRegistry.getInstance().getBandInfo(memberName);
                    long longValue = compoundData.getLong(i);
                    if (bandInfo != null) {
                        double doubleValue = bandInfo.getScaleFactor() * longValue + bandInfo.getScaleOffset();
                        printWriter.print(doubleValue);
                    } else {
                        printWriter.print(longValue);
                    }
                }
            } else if (member.getType().isCompoundType()) {
                writeCompound(compoundData.getCompound(i));
            }
            if (i < memberCount - 1) {
                printWriter.print(separator);
            }
        }
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }
}
