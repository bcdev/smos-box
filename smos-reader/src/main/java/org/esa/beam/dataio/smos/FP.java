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

abstract class FP extends SmosValueProvider {

    private final SmosValueProvider frxProvider;
    private final SmosValueProvider fryProvider;
    private final SmosValueProvider grxProvider;
    private final SmosValueProvider gryProvider;
    private final SmosValueProvider btxProvider;
    private final SmosValueProvider btyProvider;
    private final SmosValueProvider btxyProvider;

    private final boolean accuracy;
    private final boolean imaginary;

    protected FP(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy,
                 boolean imaginary) {
        this.accuracy = accuracy;
        this.imaginary = imaginary;

        frxProvider = getValueProvider(product.getBand("Faraday_Rotation_Angle_X"), valueProviderMap);
        fryProvider = getValueProvider(product.getBand("Faraday_Rotation_Angle_Y"), valueProviderMap);
        grxProvider = getValueProvider(product.getBand("Geometric_Rotation_Angle_X"), valueProviderMap);
        gryProvider = getValueProvider(product.getBand("Geometric_Rotation_Angle_Y"), valueProviderMap);

        final String quantity;
        if (accuracy) {
            quantity = "Pixel_Radiometric_Accuracy";
        } else {
            quantity = "BT_Value";
        }
        if (imaginary) {
            btxProvider = null;
            btyProvider = null;
            if (accuracy) {
                btxyProvider = getValueProvider(product.getBand(quantity + "_XY"), valueProviderMap);
            } else {
                btxyProvider = getValueProvider(product.getBand(quantity + "_XY_Imag"), valueProviderMap);
            }
        } else {
            btxProvider = getValueProvider(product.getBand(quantity + "_X"), valueProviderMap);
            btyProvider = getValueProvider(product.getBand(quantity + "_Y"), valueProviderMap);
            if (accuracy) {
                btxyProvider = getValueProvider(product.getBand(quantity + "_XY"), valueProviderMap);
            } else {
                btxyProvider = getValueProvider(product.getBand(quantity + "_XY_Real"), valueProviderMap);
            }
        }
    }

    private static SmosValueProvider getValueProvider(Band band, Map<String, SmosValueProvider> map) {
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
    protected final float getFloat(int gridPointIndex) throws IOException {
        final float frx = frxProvider.getFloat(gridPointIndex);
        final float grx = grxProvider.getFloat(gridPointIndex);
        final float fry = fryProvider.getFloat(gridPointIndex);
        final float gry = gryProvider.getFloat(gridPointIndex);

        final double alphaX = Math.toRadians(frx - grx);
        final double alphaY = Math.toRadians(fry - gry);
        final double a = (Math.cos(alphaX) + Math.cos(alphaY)) / 2.0;
        final double b = (Math.sin(alphaX) + Math.sin(alphaY)) / 2.0;
        final double aa = a * a;
        final double ab = a * b;
        final double bb = b * b;

        final double btx;
        final double bty;
        final double btxy;
        if (imaginary) {
            btx = 0.0;
            bty = 0.0;
            btxy = btxyProvider.getFloat(gridPointIndex);
        } else {
            btx = btxProvider.getFloat(gridPointIndex);
            bty = btyProvider.getFloat(gridPointIndex);
            btxy = btxyProvider.getFloat(gridPointIndex);
        }

        final float result;
        if (accuracy) {
            result = computeRA(btx, bty, btxy, aa, ab, bb);
        } else {
            result = computeBT(btx, bty, btxy, aa, ab, bb);
        }
        return result;
    }

    protected abstract float computeBT(double btx, double bty, double btxy, double aa, double ab, double bb);

    protected abstract float computeRA(double rax, double ray, double raxy, double aa, double ab, double bb);
}
