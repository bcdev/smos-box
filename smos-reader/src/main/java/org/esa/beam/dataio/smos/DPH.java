package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class DPH extends DP {

    DPH(Product product, Map<String, ValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy);
    }

    @Override
    protected double compute(double btx, double bty, double aa, double bb) {
        return (aa * btx - bb * bty);
    }
}
