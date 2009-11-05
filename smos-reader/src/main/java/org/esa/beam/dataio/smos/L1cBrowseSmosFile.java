/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import org.esa.beam.framework.datamodel.Product;

import java.io.File;
import java.io.IOException;

/**
 * Represents a SMOS L1c Browse product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class L1cBrowseSmosFile extends L1cSmosFile {

    L1cBrowseSmosFile(File hdrFile, File dblFile, DataFormat format) throws IOException {
        super(hdrFile, dblFile, format);
    }

    @Override
    protected final void addBand(Product product, BandDescriptor descriptor) {
        if (descriptor.getPolarization() < 0) {
            super.addBand(product, descriptor);
        } else {
            addBand(product, descriptor, getBtDataType());
        }
    }

    @Override
    protected final ValueProvider createValueProvider(BandDescriptor descriptor) {
        final int polarization = descriptor.getPolarization();
        if (polarization < 0) {
            return super.createValueProvider(descriptor);
        }
        return new L1cBrowseValueProvider(this, getBtDataType().getMemberIndex(descriptor.getMemberName()),
                                          polarization);
    }
}
