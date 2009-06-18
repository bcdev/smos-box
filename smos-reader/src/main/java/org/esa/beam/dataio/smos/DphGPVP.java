package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class DphGPVP extends DpGPVP {

    protected DphGPVP(Product product, Map<String, GridPointValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy);
    }

    @Override
    protected double compute(double btx, double bty, double aa, double bb) {
        return (aa * btx - bb * bty);
    }
}
