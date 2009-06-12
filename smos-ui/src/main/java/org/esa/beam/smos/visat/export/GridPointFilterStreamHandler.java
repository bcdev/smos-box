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
import com.bc.ceres.binio.CompoundType;

import org.esa.beam.dataio.smos.SmosFile;
import org.esa.beam.dataio.smos.SmosFormats;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;

/**
 * todo - add API doc
 *
 * @author Marco Zuehlke
 * @version $Revision$ $Date$
 * @since SMOS 1.0
 */
public class GridPointFilterStreamHandler {
    
    private final GridPointFilterStream filterStream;
    
    public GridPointFilterStreamHandler(GridPointFilterStream filterStream) {
        this.filterStream = filterStream;
    }
    
    public void processProductList(Product[] products, Area area) throws IOException {
        for (Product product : products) {
            ProductReader productReader = product.getProductReader();
            if (productReader instanceof SmosProductReader) {
                SmosProductReader smosProductReader = (SmosProductReader) productReader;
                SmosFile smosFile = smosProductReader.getSmosFile();
                handleSmosFile(smosFile, area);
            }
        }
    }
    

    public void processDirectory(File dir, boolean recursive, Area area) throws IOException {
        
    }
    
    private void handleSmosFile(SmosFile smosFile, Area area) throws IOException {
        CompoundType gridPointType = smosFile.getGridPointType();
        final int latIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LATITUDE_NAME);
        final int lonIndex = gridPointType.getMemberIndex(SmosFormats.GRID_POINT_LONGITUDE_NAME);
        
        filterStream.startFile(smosFile);
        final int gridPointCount = smosFile.getGridPointCount();
        for (int i = 0; i < gridPointCount; i++) {
            CompoundData gridPointData = smosFile.getGridPointData(i);
            double lat = gridPointData.getDouble(latIndex);
            double lon = gridPointData.getDouble(lonIndex);
            if (area.contains(lon, lat)) {
                filterStream.handleGridPoint(gridPointData);
            }
        }
        filterStream.stopFile(smosFile);
    }
}
