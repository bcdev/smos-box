package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.HashMap;

class FphGPVP extends FpGPVP {

    public FphGPVP(Product product, HashMap<String, GridPointValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, true);
    }

    @Override
    protected double compute(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return aa * btx - 2.0 * ab * btxy + bb * bty;
    }

}
