package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.util.Map;

abstract class DpGPVP implements GridPointValueProvider {

    private final GridPointValueProvider frxProvider;
    private final GridPointValueProvider fryProvider;
    private final GridPointValueProvider grxProvider;
    private final GridPointValueProvider gryProvider;
    private final GridPointValueProvider btxProvider;
    private final GridPointValueProvider btyProvider;

    protected DpGPVP(Product product, Map<String, GridPointValueProvider> valueProviderMap, boolean accuracy) {
        frxProvider = new ScalingGPVP(product, "Faraday_Rotation_Angle_X", valueProviderMap);
        grxProvider = new ScalingGPVP(product, "Geometric_Rotation_Angle_X", valueProviderMap);

        fryProvider = new ScalingGPVP(product, "Faraday_Rotation_Angle_Y", valueProviderMap);
        gryProvider = new ScalingGPVP(product, "Geometric_Rotation_Angle_Y", valueProviderMap);

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
    public final Area getRegion() {
        return btxProvider.getRegion();
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return btxProvider.getGridPointIndex(seqnum);
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
