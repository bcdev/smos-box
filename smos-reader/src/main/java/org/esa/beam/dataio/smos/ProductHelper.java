package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.SimpleType;
import com.bc.ceres.binio.Type;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.dataop.maptransf.Datum;
import org.esa.beam.framework.dataop.maptransf.IdentityTransformDescriptor;
import org.esa.beam.framework.dataop.maptransf.MapInfo;
import org.esa.beam.framework.dataop.maptransf.MapProjection;
import org.esa.beam.framework.dataop.maptransf.MapProjectionRegistry;
import org.esa.beam.smos.dgg.SmosDgg;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

class ProductHelper {

    private ProductHelper() {
    }

    static ImageInfo createImageInfo(Band band, BandDescriptor descriptor) {
        final Color[] colors;
        if (descriptor.isCyclic()) {
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

        final double min;
        final double max;
        if (descriptor.hasTypicalMin()) {
            min = descriptor.getTypicalMin();
        } else {
            min = band.getStx().getMin();
        }
        if (descriptor.hasTypicalMax()) {
            max = descriptor.getTypicalMax();
        } else {
            max = band.getStx().getMax();
        }

        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final double sample = min + ((max - min) * i / (colors.length - 1));
            points[i] = new ColorPaletteDef.Point(sample, colors[i]);
        }

        return new ImageInfo(new ColorPaletteDef(points));
    }

    static MapInfo createMapInfo(Dimension dimension) {
        final MapProjection projection = MapProjectionRegistry.getProjection(IdentityTransformDescriptor.NAME);
        final MapInfo mapInfo = new MapInfo(projection, 0.0f, 0.0f, -180.0f, 90.0f,
                                            360.0f / dimension.width,
                                            180.0f / dimension.height,
                                            Datum.WGS_84);
        mapInfo.setSceneWidth(dimension.width);
        mapInfo.setSceneHeight(dimension.height);

        return mapInfo;
    }

    static Dimension getSceneRasterDimension() {
        final MultiLevelImage dggMultiLevelImage = SmosDgg.getInstance().getDggMultiLevelImage();
        final int w = dggMultiLevelImage.getWidth();
        final int h = dggMultiLevelImage.getHeight();

        return new Dimension(w, h);
    }

    static int getDataType(Type memberType) {
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

    static void addMetadata(MetadataElement metadataElement, ExplorerFile explorerFile) throws IOException {
        final Document document;

        try {
            document = new SAXBuilder().build(explorerFile.getHdrFile());
        } catch (JDOMException e) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Invalid document", explorerFile.getHdrFile().getPath()), e);
        }

        final Namespace namespace = document.getRootElement().getNamespace();
        if (namespace == null) {
            throw new IOException(MessageFormat.format(
                    "File ''{0}'': Missing namespace", explorerFile.getHdrFile().getPath()));
        }

        addMetadata(metadataElement, document.getRootElement(), namespace);
    }

    static void addMetadata(MetadataElement metadataElement, Element xmlElement, Namespace namespace) {
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

    static void addFlagsAndMasks(Product product, Band band,
                                 String flagCodingName, Family<FlagDescriptor> flagDescriptors) {
        FlagCoding flagCoding = product.getFlagCodingGroup().get(flagCodingName);
        if (flagCoding == null) {
            flagCoding = new FlagCoding(flagCodingName);
            for (final FlagDescriptor flagDescriptor : flagDescriptors.asList()) {
                flagCoding.addFlag(flagDescriptor.getFlagName(),
                                   flagDescriptor.getMask(),
                                   flagDescriptor.getDescription());
            }
            product.getFlagCodingGroup().add(flagCoding);
        }
        band.setSampleCoding(flagCoding);

        final Random random = new Random(5489); // for creating random colours
        for (final FlagDescriptor flagDescriptor : flagDescriptors.asList()) {
            final String maskName = band.getName() + "_" + flagDescriptor.getFlagName();
            Mask mask = product.getMaskGroup().get(maskName);
            if (mask == null) {
                mask = new Mask(maskName,
                                product.getSceneRasterWidth(),
                                product.getSceneRasterHeight(),
                                new Mask.BandMathType());
                mask.setDescription(flagDescriptor.getDescription());
                Color color = flagDescriptor.getColor();
                if (color == null) {
                    color = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
                }
                mask.setImageColor(color);
                mask.setImageTransparency(flagDescriptor.getTransparency());
                final String imageExpression = band.getName() + "." + flagDescriptor.getFlagName();
                mask.getImageConfig().setValue("expression", imageExpression);
                product.getMaskGroup().add(mask);
            }
        }
    }
}
