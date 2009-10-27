package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.smos.dgg.SmosDgg;
import org.esa.beam.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.awt.Dimension;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.binio.DataFormat;

abstract class SmosProductFactory {

    public final Product createProduct(File hdrFile, SmosFile smosFile) throws IOException {
        final String productName = FileUtils.getFilenameWithoutExtension(hdrFile);
        final String productType = smosFile.getFormat().getName().substring(12, 22);
        final Dimension dimension = getSceneRasterDimension();
        final Product product = new Product(productName, productType, dimension.width, dimension.height);

        product.setFileLocation(smosFile.getFile());
        product.setPreferredTileSize(512, 512);
        addMetadata(product, hdrFile);
        setGeoCoding(product);
        addBands(product, smosFile);
        setQuicklookBandName(product);
        
        return product;
    }

    protected Dimension getSceneRasterDimension() {
        final MultiLevelImage dggMultiLevelImage = SmosDgg.getInstance().getDggMultiLevelImage();
        final int w = dggMultiLevelImage.getWidth();
        final int h = dggMultiLevelImage.getHeight();

        return new Dimension(w, h);
    }

    protected abstract void setGeoCoding(Product product);

    protected abstract void addBands(Product product, SmosFile smosFile);

    protected void setQuicklookBandName(Product product) {
    }

    private void addMetadata(Product product, File hdrFile) throws IOException {
        addMetadata(product.getMetadataRoot(), hdrFile);
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
