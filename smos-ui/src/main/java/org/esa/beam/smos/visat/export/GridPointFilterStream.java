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

import org.esa.beam.dataio.smos.SmosFile;

import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * A filter stream that works on a SMOS grid points.
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 2.0
 */
interface GridPointFilterStream extends GridPointHandler {

    void startFile(SmosFile smosfile) throws FileNotFoundException;

    void stopFile(SmosFile smosfile) throws IOException;

    void close() throws IOException;
}
