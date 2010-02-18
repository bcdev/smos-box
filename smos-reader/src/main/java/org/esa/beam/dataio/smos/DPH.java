package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class DPH extends DP {

    DPH(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy);
    }

    @Override
    protected float computeBT(double btx, double bty, double aa, double bb) {
        return (float) (aa * btx - bb * bty);
    }

    @Override
    protected float computeRA(double rax, double ray, double aa, double bb) {
        return (float) Math.sqrt(aa * aa * rax * rax + bb * bb * ray * ray);
    }
}
