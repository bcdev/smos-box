package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.HashMap;

class FpvGPVP extends FpGPVP {

    public FpvGPVP(Product product, HashMap<String, GridPointValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, true);
    }

    @Override
    protected double compute(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return bb * btx + 2.0 * ab * btxy + aa * bty;
    }
}
