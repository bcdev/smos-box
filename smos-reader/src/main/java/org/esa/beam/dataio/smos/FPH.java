package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class FPH extends FP {

    FPH(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, false);
    }

    @Override
    protected float computeBT(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return (float) (aa * btx - 2.0 * ab * btxy + bb * bty);
    }

    @Override
    protected float computeRA(double rax, double ray, double raxy, double aa, double ab, double bb) {
        return (float) Math.sqrt(aa * aa * rax * rax + 4.0 * ab * ab * raxy * raxy + bb * bb * ray * ray);
    }
}
