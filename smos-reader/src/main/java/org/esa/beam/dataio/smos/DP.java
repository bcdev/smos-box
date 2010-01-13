package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.util.Map;

abstract class DP implements ValueProvider {

    private final ValueProvider frxProvider;
    private final ValueProvider fryProvider;
    private final ValueProvider grxProvider;
    private final ValueProvider gryProvider;
    private final ValueProvider btxProvider;
    private final ValueProvider btyProvider;

    private final boolean accuracy;

    protected DP(Product product, Map<String, ValueProvider> valueProviderMap, boolean accuracy) {
        this.accuracy = accuracy;

        frxProvider = getValueProvider(product, "Faraday_Rotation_Angle_X", valueProviderMap);
        grxProvider = getValueProvider(product, "Geometric_Rotation_Angle_X", valueProviderMap);

        fryProvider = getValueProvider(product, "Faraday_Rotation_Angle_Y", valueProviderMap);
        gryProvider = getValueProvider(product, "Geometric_Rotation_Angle_Y", valueProviderMap);

        final String quantity;
        if (accuracy) {
            quantity = "Pixel_Radiometric_Accuracy";
        } else {
            quantity = "BT_Value";
        }

        btxProvider = getValueProvider(product, quantity + "_X", valueProviderMap);
        btyProvider = getValueProvider(product, quantity + "_Y", valueProviderMap);
    }

    private static ValueProvider getValueProvider(Product product, String bandName, Map<String, ValueProvider> map) {
        final Band band = product.getBand(bandName);
        if (band.isScalingApplied()) {
            return new Scaler(map.get(bandName), band);
        }
        return map.get(bandName);
    }

    @Override
    public final Area getEnvelope() {
        return frxProvider.getEnvelope();
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return frxProvider.getGridPointIndex(seqnum);
    }

    @Override
    public final byte getValue(int gridPointIndex, byte noDataValue) {
        return 0;
    }

    @Override
    public final short getValue(int gridPointIndex, short noDataValue) {
        return 0;
    }

    @Override
    public final int getValue(int gridPointIndex, int noDataValue) {
        return 0;
    }

    @Override
    public final float getValue(int gridPointIndex, float noDataValue) {
        final float frx = frxProvider.getValue(gridPointIndex, Float.NaN);
        if (Float.isNaN(frx)) {
            return noDataValue;
        }
        final float grx = grxProvider.getValue(gridPointIndex, Float.NaN);
        if (Float.isNaN(grx)) {
            return noDataValue;
        }
        final float fry = fryProvider.getValue(gridPointIndex, Float.NaN);
        if (Float.isNaN(fry)) {
            return noDataValue;
        }
        final float gry = gryProvider.getValue(gridPointIndex, Float.NaN);
        if (Float.isNaN(gry)) {
            return noDataValue;
        }

        final double alphaX = Math.toRadians(frx - grx);
        final double alphaY = Math.toRadians(fry - gry);
        final double a = (Math.cos(alphaX) + Math.cos(alphaY)) / 2.0;
        final double b = (Math.sin(alphaX) + Math.sin(alphaY)) / 2.0;
        final double aa = a * a;
        final double bb = b * b;

        final double btx = btxProvider.getValue(gridPointIndex, noDataValue);
        final double bty = btyProvider.getValue(gridPointIndex, noDataValue);

        if (accuracy) {
            return (float) (computeRA(btx, bty, aa, bb) / Math.abs(aa * aa - bb * bb));
        } else {
            return (float) (computeBT(btx, bty, aa, bb) / (aa * aa - bb * bb));
        }
    }

    protected abstract float computeBT(double btx, double bty, double aa, double bb);

    protected abstract float computeRA(double rax, double ray, double aa, double bb);
}
