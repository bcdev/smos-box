package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.util.Map;

abstract class FP implements FieldValueProvider {

    private final FieldValueProvider frxProvider;
    private final FieldValueProvider fryProvider;
    private final FieldValueProvider grxProvider;
    private final FieldValueProvider gryProvider;
    private final FieldValueProvider btxProvider;
    private final FieldValueProvider btyProvider;
    private final FieldValueProvider btxyProvider;
    private final boolean imaginary;

    protected FP(Product product, Map<String, FieldValueProvider> valueProviderMap,
                 boolean accuracy, boolean imaginary) {
        this.imaginary = imaginary;
        frxProvider = new ScalingDecorator(valueProviderMap.get("Faraday_Rotation_Angle_X"),
                                           product.getBand("Faraday_Rotation_Angle_X"));
        grxProvider = new ScalingDecorator(valueProviderMap.get("Geometric_Rotation_Angle_X"),
                                           product.getBand("Geometric_Rotation_Angle_X"));

        fryProvider = new ScalingDecorator(valueProviderMap.get("Faraday_Rotation_Angle_Y"),
                                           product.getBand("Faraday_Rotation_Angle_Y"));
        gryProvider = new ScalingDecorator(valueProviderMap.get("Geometric_Rotation_Angle_Y"),
                                           product.getBand("Geometric_Rotation_Angle_Y"));

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
                btxyProvider = valueProviderMap.get(quantity + "_XY");
            } else {
                btxyProvider = valueProviderMap.get(quantity + "_XY_Imag");
            }
        } else {
            btxProvider = valueProviderMap.get(quantity + "_X");
            btyProvider = valueProviderMap.get(quantity + "_Y");
            if (accuracy) {
                btxyProvider = valueProviderMap.get(quantity + "_XY");
            } else {
                btxyProvider = valueProviderMap.get(quantity + "_XY_Real");
            }
        }
    }

    @Override
    public final Area getRegion() {
        return frxProvider.getRegion();
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
        final double ab = a * b;
        final double bb = b * b;

        final double btx;
        final double bty;
        final double btxy;
        if (imaginary) {
            btx = 0.0;
            bty = 0.0;
            btxy = btxyProvider.getValue(gridPointIndex, noDataValue);
        } else {
            btx = btxProvider.getValue(gridPointIndex, noDataValue);
            bty = btyProvider.getValue(gridPointIndex, noDataValue);
            btxy = btxyProvider.getValue(gridPointIndex, noDataValue);
        }

        return (float) compute(btx, bty, btxy, aa, ab, bb);
    }

    protected abstract double compute(double btx, double bty, double btxy, double aa, double ab, double bb);
}
