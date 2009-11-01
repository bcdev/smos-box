package org.esa.beam.dataio.smos;

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

    protected DP(Product product, Map<String, ValueProvider> valueProviderMap, boolean accuracy) {
        frxProvider = new ValueScaler(valueProviderMap.get("Faraday_Rotation_Angle_X"),
                                           product.getBand("Faraday_Rotation_Angle_X"));
        grxProvider = new ValueScaler(valueProviderMap.get("Geometric_Rotation_Angle_X"),
                                           product.getBand("Geometric_Rotation_Angle_X"));

        fryProvider = new ValueScaler(valueProviderMap.get("Faraday_Rotation_Angle_Y"),
                                           product.getBand("Faraday_Rotation_Angle_Y"));
        gryProvider = new ValueScaler(valueProviderMap.get("Geometric_Rotation_Angle_Y"),
                                           product.getBand("Geometric_Rotation_Angle_Y"));

        final String quantity;
        if (accuracy) {
            quantity = "Pixel_Radiometric_Accuracy";
        } else {
            quantity = "BT_Value";
        }

        btxProvider = valueProviderMap.get(quantity + "_X");
        btyProvider = valueProviderMap.get(quantity + "_Y");
    }

    @Override
    public final Area getDomain() {
        return frxProvider.getDomain();
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
        final double frx = frxProvider.getValue(gridPointIndex, noDataValue);
        final double grx = grxProvider.getValue(gridPointIndex, noDataValue);

        final double fry = fryProvider.getValue(gridPointIndex, noDataValue);
        final double gry = gryProvider.getValue(gridPointIndex, noDataValue);

        final double alphaX = Math.toRadians(frx - grx);
        final double alphaY = Math.toRadians(fry - gry);
        final double a = (Math.cos(alphaX) + Math.cos(alphaY)) / 2.0;
        final double b = (Math.sin(alphaX) + Math.sin(alphaY)) / 2.0;
        final double aa = a * a;
        final double bb = b * b;

        final double btx = btxProvider.getValue(gridPointIndex, noDataValue);
        final double bty = btyProvider.getValue(gridPointIndex, noDataValue);

        return (float) (compute(btx, bty, aa, bb) / (aa * aa - bb * bb));
    }

    protected abstract double compute(double btx, double bty, double aa, double bb);
}
