package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.binio.Type;
import com.bc.ceres.binio.SimpleType;
import org.esa.beam.framework.datamodel.MapGeoCoding;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.framework.dataop.maptransf.IdentityTransformDescriptor;
import org.esa.beam.framework.dataop.maptransf.MapInfo;
import org.esa.beam.framework.dataop.maptransf.MapProjection;
import org.esa.beam.framework.dataop.maptransf.MapProjectionRegistry;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.awt.Dimension;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

abstract class SmosProductFactory {

    protected SmosProductFactory() {
    }

    public final Product createProduct(File hdrFile, SmosFile smosFile) throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(hdrFile);
        final String productType = smosFile.getFormat().getName().substring(12, 22);
        final Dimension dimension = getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(smosFile.getFile());
        product.setPreferredTileSize(512, 512);
        addMetadata(product, hdrFile);

        final MapInfo mapInfo = createMapInfo();
        product.setGeoCoding(new MapGeoCoding(mapInfo));

        addBands(product, smosFile);
        setQuicklookBandName(product);

        return product;
    }

    protected abstract void addBands(Product product, SmosFile smosFile);

    private void addMetadata(Product product, File hdrFile) throws IOException {
        addMetadata(product.getMetadataRoot(), hdrFile);
    }

    protected final ImageInfo createImageInfo(BandInfo bandInfo) {
        final Color[] colors;
        if (bandInfo.isCircular()) {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0),
                    new Color(255, 140, 0),
                    new Color(255, 255, 0),
                    new Color(0, 255, 0),
                    new Color(0, 255, 255),
                    new Color(0, 0, 255),
                    new Color(85, 0, 136),
                    new Color(0, 0, 0)
            };
        } else {
            colors = new Color[]{
                    new Color(0, 0, 0),
                    new Color(85, 0, 136),
                    new Color(0, 0, 255),
                    new Color(0, 255, 255),
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 140, 0),
                    new Color(255, 0, 0)
            };
        }
        double min = bandInfo.getMin();
        double max = bandInfo.getMax();

        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final double sample = min + ((max - min) * i / (colors.length - 1));
            points[i] = new ColorPaletteDef.Point(sample, colors[i]);
        }

        return new ImageInfo(new ColorPaletteDef(points));
    }

    private MapInfo createMapInfo() {
        final Dimension dimension = getSceneRasterDimension();
        final MapProjection projection = MapProjectionRegistry.getProjection(IdentityTransformDescriptor.NAME);
        final MapInfo mapInfo = new MapInfo(projection, 0.0f, 0.0f, -180.0f, 90.0f,
                                            360.0f / dimension.width,
                                            180.0f / dimension.height,
                                            Datum.WGS_84);
        mapInfo.setSceneWidth(dimension.width);
        mapInfo.setSceneHeight(dimension.height);

        return mapInfo;
    }

    protected abstract MultiLevelSource createMultiLevelSource(Band band, FieldValueProvider valueProvider);

    protected final MultiLevelImage createSourceImage(Band band, FieldValueProvider valueProvider) {
        return new DefaultMultiLevelImage(createMultiLevelSource(band, valueProvider));
    }

    private Dimension getSceneRasterDimension() {
        final MultiLevelImage dggMultiLevelImage = SmosDgg.getInstance().getDggMultiLevelImage();
        final int w = dggMultiLevelImage.getWidth();
        final int h = dggMultiLevelImage.getHeight();

        return new Dimension(w, h);
    }

    protected void setQuicklookBandName(Product product) {
    }

    protected final int getDataType(Type memberType) {
        if (memberType.equals(SimpleType.BYTE)) {
            return ProductData.TYPE_INT8;
        }
        if (memberType.equals(SimpleType.UBYTE)) {
            return ProductData.TYPE_UINT8;
        }
        if (memberType.equals(SimpleType.SHORT)) {
            return ProductData.TYPE_INT16;
        }
        if (memberType.equals(SimpleType.USHORT)) {
            return ProductData.TYPE_UINT16;
        }
        if (memberType.equals(SimpleType.INT)) {
            return ProductData.TYPE_INT32;
        }
        if (memberType.equals(SimpleType.UINT)) {
            return ProductData.TYPE_UINT32;
        }
        if (memberType.equals(SimpleType.FLOAT)) {
            return ProductData.TYPE_FLOAT32;
        }
        if (memberType.equals(SimpleType.DOUBLE)) {
            return ProductData.TYPE_FLOAT64;
        }
        if (memberType.equals(SimpleType.ULONG)) {
            return ProductData.TYPE_UINT32;
        }

        throw new IllegalArgumentException("Illegal member type:" + memberType.getName());
    }

    private static void addMetadata(MetadataElement metadataElement, File hdrFile) throws IOException {
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

        addMetadata(metadataElement, document.getRootElement(), namespace);
    }

    private static void addMetadata(MetadataElement metadataElement, Element xmlElement, Namespace namespace) {
        for (final Object o : xmlElement.getChildren()) {
            final Element xmlChild = (Element) o;

            if (xmlChild.getChildren(null, namespace).size() == 0) {
                final String s = xmlChild.getTextNormalize();
                if (s != null && !s.isEmpty()) {
                    metadataElement.addAttribute(
                            new MetadataAttribute(xmlChild.getName(), ProductData.createInstance(s), true));
                }
            } else {
                final MetadataElement metadataChild = new MetadataElement(xmlChild.getName());
                metadataElement.addElement(metadataChild);
                addMetadata(metadataChild, xmlChild, namespace);
            }
        }
    }
}
