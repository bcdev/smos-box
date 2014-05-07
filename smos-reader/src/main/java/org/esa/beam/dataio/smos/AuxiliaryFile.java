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
package org.esa.beam.dataio.smos;


import com.bc.ceres.binio.DataContext;
import org.esa.beam.smos.EEFilePair;

import java.io.IOException;

/**
 * Represents a zoned DGG product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
class AuxiliaryFile extends DggFile {

    AuxiliaryFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext, true);
    }
}
