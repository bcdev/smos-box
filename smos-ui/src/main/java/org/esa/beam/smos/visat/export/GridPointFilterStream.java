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

import org.esa.beam.dataio.smos.SmosDggFile;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A filter stream that works on a SMOS grid points.
 * <p/>
 * todo - rq/tb/mz consider reworking this interface (rq-20091009)
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 2.0
 */
interface GridPointFilterStream extends GridPointHandler {

    void startFile(SmosDggFile smosfile) throws FileNotFoundException;

    void stopFile(SmosDggFile smosfile) throws IOException;

    void close() throws IOException;
}
