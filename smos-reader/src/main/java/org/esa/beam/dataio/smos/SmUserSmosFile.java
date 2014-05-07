package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.DataContext;
import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.util.StringUtils;
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

    SmUserSmosFile(File hdrFile, File dblFile, DataContext dataContext) throws IOException {
        super(hdrFile, dblFile, dataContext);

        final Document document = getDocument();
        final Namespace namespace = document.getRootElement().getNamespace();
        final Element specificProductHeader = getElement(document.getRootElement(), TAG_SPECIFIC_PRODUCT_HEADER);

        final String chi_2_scale = specificProductHeader.getChildText("Chi_2_Scale", namespace);
        if (StringUtils.isNotNullAndNotEmpty(chi_2_scale)) {
            chi2Scale = Double.valueOf(chi_2_scale);
        } else {
            chi2Scale = 1.0;
        }
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
