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
import java.awt.geom.Area;

/**
 * Valid region for SMOS swath images.
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 */
public class SwathValidRegion implements ValidDataRegion {
    private final Area rectifiedRegion;

    public SwathValidRegion(Area region, SmosOpImage opImage) {
        rectifiedRegion = new Area();
        for (int x = 0; x < opImage.getNumXTiles(); ++x) {
            for (int y = 0; y < opImage.getNumYTiles(); ++y) {
                final Rectangle tileRect = opImage.getTileRect(x, y);
                if (region.intersects(tileRect)) {
                    rectifiedRegion.add(new Area(tileRect));
                }
            }
        }
    }
    
    @Override
    public boolean rectangleContainsData(Rectangle rectangle) {
        return rectifiedRegion.intersects(rectangle);
    }
    
    @Override
    public boolean lineContainsData(int y) {
        return true;
    }

    @Override
    public boolean pointContainsData(int x, int y) {
        return true;//rectifiedRegion.contains(x, y);
    }

}
