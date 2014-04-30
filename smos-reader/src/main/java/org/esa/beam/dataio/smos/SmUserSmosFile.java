package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataFormat;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.io.IOException;

/**
 * @author Ralf Quast
 */
class SmUserSmosFile extends SmosFile {

    private final double chi2Scale;

    SmUserSmosFile(File hdrFile, File dblFile, DataFormat format) throws IOException {
        super(hdrFile, dblFile, format);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        chi2Scale = Double.valueOf(specificProductHeader.getChildText("Chi_2_Scale", namespace));
    }

    @Override
    protected void setScaling(Band band, BandDescriptor descriptor) {
        final String memberName = descriptor.getMemberName();
        if ("Chi_2".equals(memberName)) {
            band.setScalingFactor(descriptor.getScalingFactor() * chi2Scale);
        } else {
            super.setScaling(band, descriptor);
        }
    }
}
