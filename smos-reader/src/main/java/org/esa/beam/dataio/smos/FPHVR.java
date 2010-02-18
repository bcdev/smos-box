package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class FPHVR extends FP {

    FPHVR(Product product, Map<String, SmosValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, false);
    }

    @Override
    protected float computeBT(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return (float) (ab * (btx - bty) + (aa - bb) * btxy);
    }

    @Override
    protected float computeRA(double rax, double ray, double raxy, double aa, double ab, double bb) {
        return (float) Math.sqrt(ab * ab * (rax * rax + ray * ray)  + (aa - bb) * (aa - bb) * raxy * raxy);
    }
}
