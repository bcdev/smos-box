package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.smos.dgg.SmosDgg;

import java.awt.image.RenderedImage;


/**
 * Represents a SMOS DGG multi level source.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class SmosMultiLevelSource extends AbstractMultiLevelSource {

    private final RasterDataNode rasterDataNode;
    private final FieldValueProvider valueProvider;

    public SmosMultiLevelSource(RasterDataNode rasterDataNode, FieldValueProvider valueProvider) {
        super(SmosDgg.getInstance().getDggMultiLevelImage().getModel());

        this.valueProvider = valueProvider;
        this.rasterDataNode = rasterDataNode;
    }

    public FieldValueProvider getValueProvider() {
        return valueProvider;
    }

    @Override
    public RenderedImage createImage(int level) {
        return new SmosOpImage(valueProvider, rasterDataNode, getModel(), ResolutionLevel.create(getModel(), level));
    }
}
