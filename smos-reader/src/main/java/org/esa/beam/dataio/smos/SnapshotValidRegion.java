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
 * Valid region for SMOS snapshot images.
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 */
class SnapshotValidRegion implements ValidDataRegion {

    private final Rectangle validRectangle;

    public SnapshotValidRegion(Rectangle rectangle) {
        this.validRectangle = rectangle;
    }
    
    @Override
    public boolean rectangleContainsData(Rectangle rectangle) {
        return validRectangle.intersects(rectangle);
    }
    
    @Override
    public boolean lineContainsData(int y) {
        return validRectangle.y < y && (validRectangle.y + validRectangle.height) > y;
    }

    @Override
    public boolean pointContainsData(int x, int y) {
        return validRectangle.contains(x, y);
    }
}
