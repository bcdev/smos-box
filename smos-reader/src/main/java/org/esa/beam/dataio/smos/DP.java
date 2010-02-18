package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.Map;

abstract class DP extends SmosValueProvider {

    private final SmosValueProvider frxProvider;
    private final SmosValueProvider fryProvider;
    private final SmosValueProvider grxProvider;
    private final SmosValueProvider gryProvider;
    private final SmosValueProvider btxProvider;
    private final SmosValueProvider btyProvider;

    private final boolean accuracy;

    protected DP(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy) {
        this.accuracy = accuracy;

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

        btxProvider = getValueProvider(product.getBand(quantity + "_X"), valueProviderMap);
        btyProvider = getValueProvider(product.getBand(quantity + "_Y"), valueProviderMap);
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
    public final float getFloat(int gridPointIndex) throws IOException {
        final float frx = frxProvider.getFloat(gridPointIndex);
        final float grx = grxProvider.getFloat(gridPointIndex);
        final float fry = fryProvider.getFloat(gridPointIndex);
        final float gry = gryProvider.getFloat(gridPointIndex);

        final double alphaX = Math.toRadians(frx - grx);
        final double alphaY = Math.toRadians(fry - gry);
        final double a = (Math.cos(alphaX) + Math.cos(alphaY)) / 2.0;
        final double b = (Math.sin(alphaX) + Math.sin(alphaY)) / 2.0;
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
