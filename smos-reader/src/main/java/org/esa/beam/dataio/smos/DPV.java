package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class DPV extends DP {

    DPV(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy);
    }

    @Override
    protected float computeBT(double btx, double bty, double aa, double bb) {
        return (float) (aa * bty - bb * btx);
    }

    @Override
    protected float computeRA(double rax, double ray, double aa, double bb) {
        return (float) Math.sqrt(aa * aa * ray * ray + bb * bb * rax * rax);
    }
}
