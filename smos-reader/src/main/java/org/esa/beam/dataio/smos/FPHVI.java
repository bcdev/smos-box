package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class FPHVI extends FP {

    FPHVI(Product product, Map<String, ValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, false);
    }

    @Override
    protected float computeBT(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return (float) (bb * btx + 2.0 * ab * btxy + aa * bty);
    }

    @Override
    protected float computeRA(double rax, double ray, double raxy, double aa, double ab, double bb) {
        return (float) Math.sqrt(bb * bb * rax * rax + 4.0 * ab * ab * raxy * raxy + aa * aa * ray * ray);
    }
}
