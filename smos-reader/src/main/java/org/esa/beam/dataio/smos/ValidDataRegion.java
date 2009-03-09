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
package org.esa.beam.dataio.smos;

import java.awt.Rectangle;

/**
 * Defines the area of an SMOS product that may contain data.
 * This is area could only be a rough assumption. The check only
 * guarantees, that pixel outside of this region do not contains data. 
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 */
interface ValidDataRegion {
    
    /**
     * Checks weather for the specified rectangular area data maybe available.
     * 
     * @param rectangle
     * @return true, if the rectangle may contain data  
     */
    boolean rectangleContainsData(Rectangle rectangle);
    
    /**
     * Checks weather for the given line data maybe available.
     * 
     * @param y
     * @return true, if the line may contain data
     */
    boolean lineContainsData(int y);
    
    /**
     * Checks weather for the given point data maybe available.
     * 
     * @param x
     * @param y
     * @return true, if the point may contain data
     */
    boolean pointContainsData(int x, int y);

}
