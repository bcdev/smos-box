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

import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.Shape;
import java.awt.image.RenderedImage;


/**
 * Represents a SMOS DGG multi level source.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class SmosMultiLevelSource extends AbstractMultiLevelSource {

    private final RasterDataNode rasterDataNode;
    private final ValueProvider valueProvider;

    public SmosMultiLevelSource(RasterDataNode rasterDataNode, ValueProvider valueProvider) {
        super(SmosDgg.getInstance().getMultiLevelImage().getModel());

        this.valueProvider = valueProvider;
        this.rasterDataNode = rasterDataNode;
    }

    public ValueProvider getValueProvider() {
        return valueProvider;
    }

    @Override
    public Shape getImageShape(int level) {
        return valueProvider.getArea().createTransformedArea(getModel().getModelToImageTransform(level));
    }

    @Override
    protected RenderedImage createImage(int level) {
        return new SmosOpImage(valueProvider, rasterDataNode, getModel(), ResolutionLevel.create(getModel(), level));
    }
}
