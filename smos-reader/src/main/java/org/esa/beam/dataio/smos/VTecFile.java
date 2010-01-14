package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.SequenceData;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.TiePointGrid;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

class VTecFile extends ExplorerFile {

    private final SequenceData mapSequenceData;

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

        final Document document;
        try {
            document = new SAXBuilder().build(hdrFile);
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", hdrFile.getPath()), e);
        }
        final Namespace namespace = document.getRootElement().getNamespace();
        if (namespace == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing namespace", hdrFile.getPath()));
        }
        final Element ionexDescriptor = getElement(document.getRootElement(), "IONEX_Descriptor");
        final Element latitudeVector = getElement(ionexDescriptor, "Latitude_Vector");
        lat1 = Double.valueOf(latitudeVector.getChildText("Latitude_Vector_1st", namespace));
        lat2 = Double.valueOf(latitudeVector.getChildText("Latitude_Vector_2nd", namespace));
        latDelta = Double.valueOf(latitudeVector.getChildText("Latitude_Vector_Increment", namespace));

        final Element longitudeVector = getElement(ionexDescriptor, "Longitude_Vector");
        lon1 = Double.valueOf(longitudeVector.getChildText("Longitude_Vector_1st", namespace));
        lon2 = Double.valueOf(longitudeVector.getChildText("Longitude_Vector_2nd", namespace));
        lonDelta = Double.valueOf(longitudeVector.getChildText("Longitude_Vector_Increment", namespace));

        final int scalingFactorExponent = Integer.valueOf(ionexDescriptor.getChildText("Scale_Factor", namespace));
        scalingFactor = Math.pow(10.0, scalingFactorExponent);

        mapSequenceData = getDataBlock().getSequence("VTEC_Info");
        if (mapSequenceData == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "SMOS File ''{0}'': Missing VTEC information.", dblFile.getPath()));
        }

        rowCount = (int) (Math.round((lat2 - lat1) / latDelta) + 1);
        colCount = (int) (Math.round((lon2 - lon1) / lonDelta) + 1);
    }

    @Override
    protected Area computeEnvelope() throws IOException {
        return new Area(new Rectangle2D.Double(-180.0, -90.0, 360.0, 180.0));
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

        for (int i = 0; i < mapSequenceData.getElementCount(); i++) {
            final CompoundData mapCompoundData = this.mapSequenceData.getCompound(i);
            final SequenceData mapSequenceData = mapCompoundData.getSequence("VTEC_Record");
            final float[] tiePoints = new float[rowCount * colCount];

            for (int j = 0; j < mapSequenceData.getElementCount(); j++) {
                final CompoundData compoundData = mapSequenceData.getCompound(j);
                final SequenceData sequenceData = compoundData.getSequence("VTEC_Data");

                for (int k = 0; k < sequenceData.getElementCount(); k++) {
                    tiePoints[j * colCount + k] = sequenceData.getCompound(k).getShort("VTEC_value");
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

    private Element getElement(Element parent, final String name) throws IOException {
        final Iterator descendants = parent.getDescendants(new Filter() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof Element) {
                    final Element e = (Element) o;
                    if (name.equals(e.getName())) {
                        return true;
                    }
                }

                return false;
            }
        });
        if (descendants.hasNext()) {
            return (Element) descendants.next();
        } else {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing element ''{1}''.", getHdrFile().getPath(), name));
        }
    }

    private String getDescription(CompoundData mapCompoundData) throws IOException {
        final CompoundData epoch = mapCompoundData.getCompound("Epoch_Current_Map");
        final int days = epoch.getInt("Days");
        final int seconds = epoch.getInt("Seconds");
        final int microseconds = epoch.getInt("Microseconds");
        final String dateTime = cfiDateToUtc(days, seconds, microseconds).toString();

        final StringBuilder descriptionBuilder = new StringBuilder("Vertical total electron content (TECU) for epoch ");
        descriptionBuilder.append(dateTime);

        return descriptionBuilder.toString();
    }

    private String getName(CompoundData mapCompoundData) throws IOException {
        final StringBuilder nameBuilder = new StringBuilder(getDataFormat().getName().substring(16, 22));
        final int mapNumber = mapCompoundData.getInt("Map_Number");
        nameBuilder.append("_").append(mapNumber);

        return nameBuilder.toString();
    }
}
