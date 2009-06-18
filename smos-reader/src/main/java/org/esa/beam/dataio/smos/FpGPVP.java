package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.util.Map;

abstract class FpGPVP implements GridPointValueProvider {

    private final GridPointValueProvider frxProvider;
    private final GridPointValueProvider fryProvider;
    private final GridPointValueProvider frxyProvider;
    private final GridPointValueProvider grxProvider;
    private final GridPointValueProvider gryProvider;
    private final GridPointValueProvider grxyProvider;
    private final GridPointValueProvider btxProvider;
    private final GridPointValueProvider btyProvider;
    private final GridPointValueProvider btxyProvider;
    private final boolean real;

    protected FpGPVP(Product product, Map<String, GridPointValueProvider> valueProviderMap,
                     boolean accuracy, boolean real) {
        this.real = real;
        frxProvider = new ScalingGPVP(product, "Faraday_Rotation_Angle_X", valueProviderMap);
        grxProvider = new ScalingGPVP(product, "Geometric_Rotation_Angle_X", valueProviderMap);

        fryProvider = new ScalingGPVP(product, "Faraday_Rotation_Angle_Y", valueProviderMap);
        gryProvider = new ScalingGPVP(product, "Geometric_Rotation_Angle_Y", valueProviderMap);

        frxyProvider = new ScalingGPVP(product, "Faraday_Rotation_Angle_XY", valueProviderMap);
        grxyProvider = new ScalingGPVP(product, "Geometric_Rotation_Angle_XY", valueProviderMap);

        final String quantity;
        if (accuracy) {
            quantity = "Pixel_Radiometric_Accuracy";
        } else {
            quantity = "BT_Value";
        }

        if (real) {
            btxProvider = valueProviderMap.get(quantity + "_X");
            btyProvider = valueProviderMap.get(quantity + "_Y");
            if (accuracy) {
                btxyProvider = valueProviderMap.get(quantity + "_XY");
            } else {
                btxyProvider = valueProviderMap.get(quantity + "_XY_Real");
            }
        } else {
            btxProvider = null;
            btyProvider = null;
            if (accuracy) {
                btxyProvider = valueProviderMap.get(quantity + "_XY");
            } else {
                btxyProvider = valueProviderMap.get(quantity + "_XY_Imag");
            }
        }
    }

    @Override
    public final Area getRegion() {
        return btxyProvider.getRegion();
    }

    @Override
    public final int getGridPointIndex(int seqnum) {
        return btxyProvider.getGridPointIndex(seqnum);
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

        final double frxy = frxyProvider.getValue(gridPointIndex, noDataValue);
        final double grxy = grxyProvider.getValue(gridPointIndex, noDataValue);

        final double alphaX = Math.toRadians(frx - grx);
        final double alphaY = Math.toRadians(fry - gry);
        final double alphaXY = Math.toRadians(frxy - grxy);
        final double a = (Math.cos(alphaX) + Math.cos(alphaY) + Math.cos(alphaXY)) / 3.0;
        final double b = (Math.sin(alphaX) + Math.sin(alphaY) + Math.sin(alphaXY)) / 3.0;
        final double aa = a * a;
        final double ab = a * b;
        final double bb = b * b;

        final double btx;
        final double bty;
        final double btxy;
        if (real) {
            btx = btxProvider.getValue(gridPointIndex, noDataValue);
            bty = btyProvider.getValue(gridPointIndex, noDataValue);
            btxy = btxyProvider.getValue(gridPointIndex, noDataValue);
        } else {
            btx = 0.0;
            bty = 0.0;
            btxy = btxyProvider.getValue(gridPointIndex, noDataValue);
        }

        return (float) compute(btx, bty, btxy, aa, ab, bb);
    }

    protected abstract double compute(double btx, double bty, double btxy, double aa, double ab, double bb);
}
