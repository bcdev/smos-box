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

import com.bc.ceres.binio.*;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.smos.EEFilePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Abstract representation of a SMOS L1c product file.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class L1cSmosFile extends SmosFile {

    private final int btDataListIndex;
    private final CompoundType btDataType;
    private final double radiometricAccuracyScale;
    private final double pixelFootprintScale;

    protected L1cSmosFile(EEFilePair eeFilePair, DataContext dataContext) throws IOException {
        super(eeFilePair, dataContext);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        radiometricAccuracyScale = Double.valueOf(
                specificProductHeader.getChildText("Radiometric_Accuracy_Scale", namespace));
        pixelFootprintScale = Double.valueOf(specificProductHeader.getChildText("Pixel_Footprint_Scale", namespace));

        btDataListIndex = getGridPointType().getMemberIndex(SmosConstants.BT_DATA_LIST_NAME);
        if (btDataListIndex == -1) {
            throw new IOException("Grid point type does not include BT data list.");
        }

        final Type memberType = getGridPointType().getMemberType(btDataListIndex);
        if (!memberType.isSequenceType()) {
            throw new IOException(MessageFormat.format(
                    "Data type ''{0}'' is not of appropriate type", memberType.getName()));
        }

        final Type elementType = ((SequenceType) memberType).getElementType();
        if (!elementType.isCompoundType()) {
            throw new IOException(MessageFormat.format(
                    "Data type ''{0}'' is not a compound type", elementType.getName()));
        }

        btDataType = (CompoundType) elementType;
    }

    @Override
    protected void setScaling(Band band, BandDescriptor descriptor) {
        final String memberName = descriptor.getMemberName();
        if (memberName.startsWith("Footprint_Axis")) {
            band.setScalingFactor(descriptor.getScalingFactor() * pixelFootprintScale);
        } else if (memberName.startsWith("Pixel_Radiometric_Accuracy")) {
            band.setScalingFactor(descriptor.getScalingFactor() * radiometricAccuracyScale);
        } else if (memberName.startsWith("Radiometric_Accuracy_of_Pixel")) { // does not occur, is mapped to Pixel_Radiometric_Accuracy
            band.setScalingFactor(descriptor.getScalingFactor() * radiometricAccuracyScale);
        } else {
            super.setScaling(band, descriptor);
        }
    }

    public final CompoundType getBtDataType() {
        return btDataType;
    }

    public final SequenceData getBtDataList(int gridPointIndex) throws IOException {
        return getGridPointData(gridPointIndex).getSequence(btDataListIndex);
    }
}
