package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.HashMap;

class FPH extends FP {

    FPH(Product product, HashMap<String, FieldValueProvider> valueProviderMap, boolean accuracy) {
        super(product, valueProviderMap, accuracy, false);
    }

    @Override
    protected double compute(double btx, double bty, double btxy, double aa, double ab, double bb) {
        return aa * btx - 2.0 * ab * btxy + bb * bty;
    }

}
