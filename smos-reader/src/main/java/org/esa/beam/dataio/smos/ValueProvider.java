/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
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
package org.esa.beam.dataio.smos;

import java.awt.geom.Area;

/**
 * Interface providing access to the grid point data values.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public interface ValueProvider {

    Area getEnvelope();

    int getGridPointIndex(int seqnum);

    byte getValue(int gridPointIndex, byte noDataValue);

    short getValue(int gridPointIndex, short noDataValue);

    int getValue(int gridPointIndex, int noDataValue);

    float getValue(int gridPointIndex, float noDataValue);
}
