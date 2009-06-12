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

import org.esa.beam.dataio.smos.SmosFile;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Exports grid cells to CSV
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 1.0
 */
public class CsvGridExport implements GridPointFilterStream {
    
    private final PrintWriter printWriter;

    public CsvGridExport(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    @Override
    public void startFile(SmosFile smosfile) throws IOException {
        printWriter.println(smosfile.getFile().getName());
//        CompoundType gridPointType = smosfile.getGridPointType();
        // write header
    }
    
    @Override
    public void stopFile(SmosFile smosfile) {
        printWriter.println("----------------------");
    }
    

    @Override
    public void handleGridPoint(CompoundData gridPointData) throws IOException {
        printWriter.println(gridPointData.getMemberCount());
        // TODO Auto-generated method stub
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }
}
