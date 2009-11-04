package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class FPV extends FP {

    FPV(Product product, Map<String, ValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, true);
    }

    @Override
    protected float computeBT(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return (float) ((aa + bb) * btxy);
    }

    @Override
    protected float computeRA(double rax, double ray, double raxy, double aa, double ab, double bb) {
        return (float) ((aa + bb) * raxy);
    }
}
