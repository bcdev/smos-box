/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.util.Map;

class DPH extends DP {

    DPH(Product product, Map<String, AbstractValueProvider> valueProviderMap, boolean accuracy) {
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
