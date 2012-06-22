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

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.dataio.smos.util.DateTimeUtils;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.TiePointGrid;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

class VTecFile extends ExplorerFile {

    private static final String TAG_IONEX_DESCRIPTOR = "IONEX_Descriptor";
    private static final String TAG_LATITUDE_VECTOR = "Latitude_Vector";
    private static final String TAG_LATITUDE_VECTOR_1ST = "Latitude_Vector_1st";
    private static final String TAG_LATITUDE_VECTOR_2ND = "Latitude_Vector_2nd";
    private static final String TAG_LATITUDE_VECTOR_INCREMENT = "Latitude_Vector_Increment";
    private static final String TAG_LONGITUDE_VECTOR = "Longitude_Vector";
    private static final String TAG_LONGITUDE_VECTOR_1ST = "Longitude_Vector_1st";
    private static final String TAG_LONGITUDE_VECTOR_2ND = "Longitude_Vector_2nd";
    private static final String TAG_LONGITUDE_VECTOR_INCREMENT = "Longitude_Vector_Increment";
    private static final String TAG_SCALING_FACTOR_EXPONENT = "Scale_Factor";

    private static final String DAYS_NAME = "Days";
    private static final String EPOCH_CURRENT_MAP_NAME = "Epoch_Current_Map";
    private static final String MAP_NUMBER_NAME = "Map_Number";
    private static final String MICROSECONDS_NAME = "Microseconds";
    private static final String SECONDS_NAME = "Seconds";

    private static final String VTEC_INFO_NAME = "VTEC_Info";
    private static final String VTEC_RECORD_NAME = "VTEC_Record";
    private static final String VTEC_DATA_NAME = "VTEC_Data";
    private static final String VTEC_VALUE_NAME = "VTEC_value";

    private final SequenceData mapData;

    private final double lat1;
    private final double lat2;
    private final double latDelta;
    private final double lon1;
    private final double lon2;
    private final double lonDelta;
    private double scalingFactor;

    private final int rowCount;
    private final int colCount;

    VTecFile(File hdrFile, File dblFile, DataFormat dataFormat) throws IOException {
        super(hdrFile, dblFile, dataFormat);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element ionexDescriptor = getElement(document.getRootElement(), TAG_IONEX_DESCRIPTOR);
        final Element latitudeVector = getElement(ionexDescriptor, TAG_LATITUDE_VECTOR);
        lat1 = Double.valueOf(latitudeVector.getChildText(TAG_LATITUDE_VECTOR_1ST, namespace));
        lat2 = Double.valueOf(latitudeVector.getChildText(TAG_LATITUDE_VECTOR_2ND, namespace));
        latDelta = Double.valueOf(latitudeVector.getChildText(TAG_LATITUDE_VECTOR_INCREMENT, namespace));

        final Element longitudeVector = getElement(ionexDescriptor, TAG_LONGITUDE_VECTOR);
        lon1 = Double.valueOf(longitudeVector.getChildText(TAG_LONGITUDE_VECTOR_1ST, namespace));
        lon2 = Double.valueOf(longitudeVector.getChildText(TAG_LONGITUDE_VECTOR_2ND, namespace));
        lonDelta = Double.valueOf(longitudeVector.getChildText(TAG_LONGITUDE_VECTOR_INCREMENT, namespace));

        final int scalingFactorExponent = Integer.valueOf(ionexDescriptor.getChildText(TAG_SCALING_FACTOR_EXPONENT,
                                                                                       namespace));
        scalingFactor = Math.pow(10.0, scalingFactorExponent);

        mapData = getDataBlock().getSequence(VTEC_INFO_NAME);
        if (mapData == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing VTEC info.", dblFile.getPath()));
        }

        rowCount = (int) (Math.round((lat2 - lat1) / latDelta) + 1);
        colCount = (int) (Math.round((lon2 - lon1) / lonDelta) + 1);
    }

    @Override
    protected Area getArea() {
        return new Area(new Rectangle2D.Double(lon1, lat2, lon2 - lon1, lat1 - lat2));
    }

    @Override
    protected Product createProduct() throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(getHdrFile());
        final String productType = getDataFormat().getName().substring(12, 22);
        final Dimension dimension = ProductHelper.getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(getDblFile());
        product.setPreferredTileSize(512, 512);
        ProductHelper.addMetadata(product.getMetadataRoot(), this);
        product.setGeoCoding(ProductHelper.createGeoCoding(dimension));

        for (int i = 0; i < mapData.getElementCount(); i++) {
            final CompoundData mapCompoundData = mapData.getCompound(i);
            final SequenceData mapSequenceData = mapCompoundData.getSequence(VTEC_RECORD_NAME);
            final float[] tiePoints = new float[rowCount * colCount];

            for (int j = 0; j < mapSequenceData.getElementCount(); j++) {
                final CompoundData compoundData = mapSequenceData.getCompound(j);
                final SequenceData sequenceData = compoundData.getSequence(VTEC_DATA_NAME);

                for (int k = 0; k < sequenceData.getElementCount(); k++) {
                    tiePoints[j * colCount + k] = sequenceData.getCompound(k).getShort(VTEC_VALUE_NAME);
                }
            }

            final String name = getName(mapCompoundData);
            final String description = getDescription(mapCompoundData);
            addTiePointGrid(product, name, description, tiePoints);
        }

        return product;
    }

    private void addTiePointGrid(Product product, String name, String description, float[] tiePoints) {
        final int w = product.getSceneRasterWidth();
        final int h = product.getSceneRasterHeight();

        final double maxLat = 90.0;
        final double maxLon = 180.0;
        final double latRange = 180.0;
        final double lonRange = 360.0;

        final double scaleX = w / lonRange;
        final double scaleY = h / latRange;
        final float samplingX = (float) Math.abs(lonDelta * scaleX);
        final float samplingY = (float) Math.abs(latDelta * scaleY);
        final float offsetX = (float) ((maxLon + lon1) * scaleX);
        final float offsetY = (float) ((maxLat - lat1) * scaleY);

        final TiePointGrid tiePointGrid = new TiePointGrid(name, colCount, rowCount, offsetX, offsetY, samplingX,
                                                           samplingY, tiePoints);
        tiePointGrid.setScalingFactor(scalingFactor);
        tiePointGrid.setDescription(description);
        tiePointGrid.setUnit("TECU");

        product.addTiePointGrid(tiePointGrid);
    }

    private String getDescription(CompoundData mapCompoundData) throws IOException {
        final CompoundData epoch = mapCompoundData.getCompound(EPOCH_CURRENT_MAP_NAME);
        final int days = epoch.getInt(DAYS_NAME);
        final int seconds = epoch.getInt(SECONDS_NAME);
        final int microseconds = epoch.getInt(MICROSECONDS_NAME);
        final String dateTime = DateTimeUtils.cfiDateToUtc(days, seconds, microseconds).toString();

        final StringBuilder descriptionBuilder = new StringBuilder("Vertical total electron content (TECU) for epoch ");
        descriptionBuilder.append(dateTime);

        return descriptionBuilder.toString();
    }

    private String getName(CompoundData mapCompoundData) throws IOException {
        final StringBuilder nameBuilder = new StringBuilder(getDataFormat().getName().substring(16, 22));
        final int mapNumber = mapCompoundData.getInt(MAP_NUMBER_NAME);
        nameBuilder.append("_").append(mapNumber);

        return nameBuilder.toString();
    }
}
