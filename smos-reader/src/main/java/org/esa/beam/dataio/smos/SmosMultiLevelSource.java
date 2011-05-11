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

import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelModel;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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
        super(createMultiLevelModel(rasterDataNode, valueProvider));

        this.valueProvider = valueProvider;
        this.rasterDataNode = rasterDataNode;
    }

    private static MultiLevelModel createMultiLevelModel(RasterDataNode rasterDataNode, ValueProvider valueProvider) {
        final MultiLevelModel model = SmosDgg.getInstance().getMultiLevelImage().getModel();
        final AffineTransform imageToModelTransform = new AffineTransform(model.getImageToModelTransform(0));
        final AffineTransform modelToImageTransform = new AffineTransform(model.getModelToImageTransform(0));

        final Area area = new Area(valueProvider.getArea());
        area.transform(modelToImageTransform);
        final Rectangle bounds = area.getBounds();
        imageToModelTransform.translate(bounds.x, bounds.y);

        final int width = rasterDataNode.getSceneRasterWidth();
        final int height = rasterDataNode.getSceneRasterHeight();

        return new DefaultMultiLevelModel(model.getLevelCount(), imageToModelTransform, width, height);
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
