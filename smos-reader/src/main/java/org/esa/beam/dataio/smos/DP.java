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

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.Map;

abstract class DP extends AbstractValueProvider {

    private final AbstractValueProvider frxProvider;
    private final AbstractValueProvider grxProvider;
    private final AbstractValueProvider btxProvider;
    private final AbstractValueProvider btyProvider;

    private final boolean accuracy;

    protected DP(Product product, Map<String, AbstractValueProvider> valueProviderMap, boolean accuracy) {
        this.accuracy = accuracy;

        frxProvider = getValueProvider(product.getBand("Faraday_Rotation_Angle_X"), valueProviderMap);
        grxProvider = getValueProvider(product.getBand("Geometric_Rotation_Angle_X"), valueProviderMap);

        final String quantity;
        if (accuracy) {
            quantity = "Pixel_Radiometric_Accuracy";
        } else {
            quantity = "BT_Value";
        }

        btxProvider = getValueProvider(product.getBand(quantity + "_X"), valueProviderMap);
        btyProvider = getValueProvider(product.getBand(quantity + "_Y"), valueProviderMap);
    }

    private static AbstractValueProvider getValueProvider(Band band, Map<String, AbstractValueProvider> map) {
        if (band.isScalingApplied()) {
            return new Scaler(map.get(band.getName()), band);
        }
        return map.get(band.getName());
    }

    @Override
    public final Area getArea() {
        return frxProvider.getArea();
    }

    @Override
    public final float getValue(int seqnum, float noDataValue) {
        final float value = super.getValue(seqnum, noDataValue);
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return noDataValue;
        }
        return value;
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return frxProvider.getGridPointIndex(seqnum);
    }

    @Override
    protected final byte getByte(int gridPointIndex) {
        return 0;
    }

    @Override
    protected final short getShort(int gridPointIndex) {
        return 0;
    }

    @Override
    protected final int getInt(int gridPointIndex) {
        return 0;
    }

    @Override
    public final float getFloat(int gridPointIndex) throws IOException {
        final float frx = frxProvider.getFloat(gridPointIndex);
        final float grx = grxProvider.getFloat(gridPointIndex);

        final double alpha = Math.toRadians(frx - grx);
        final double a = Math.cos(alpha);
        final double b = Math.sin(alpha);
        final double aa = a * a;
        final double bb = b * b;

        final double btx = btxProvider.getFloat(gridPointIndex);
        final double bty = btyProvider.getFloat(gridPointIndex);

        final float result;
        if (accuracy) {
            result = (float) (computeRA(btx, bty, aa, bb) / Math.abs(aa * aa - bb * bb));
        } else {
            result = (float) (computeBT(btx, bty, aa, bb) / (aa * aa - bb * bb));
        }
        return result;
    }

    protected abstract float computeBT(double btx, double bty, double aa, double bb);

    protected abstract float computeRA(double rax, double ray, double aa, double bb);
}
