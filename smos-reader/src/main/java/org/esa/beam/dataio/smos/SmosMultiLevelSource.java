package org.esa.beam.dataio.smos;

import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.jai.ResolutionLevel;

import java.awt.geom.Area;
import java.awt.image.RenderedImage;


/**
 * Represents a SMOS DGG multi level source.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 * @since SMOS-Box 1.0
 */
public class SmosMultiLevelSource extends AbstractMultiLevelSource {

    private final FieldValueProvider valueProvider;
    private final MultiLevelSource dggMultiLevelSource;
    private final RasterDataNode rasterDataNode;

    public SmosMultiLevelSource(FieldValueProvider valueProvider, MultiLevelSource dggMultiLevelSource,
                                RasterDataNode rasterDataNode) {
        super(dggMultiLevelSource.getModel());

        this.valueProvider = valueProvider;
        this.dggMultiLevelSource = dggMultiLevelSource;
        this.rasterDataNode = rasterDataNode;
    }

    public FieldValueProvider getValueProvider() {
        return valueProvider;
    }

    @Override
    public RenderedImage createImage(int level) {
        final Area modelRegion = valueProvider.getRegion();
        final Area levelRegion = modelRegion.createTransformedArea(getModel().getModelToImageTransform(level));
        final ResolutionLevel resolutionLevel = ResolutionLevel.create(getModel(), level);
        final RenderedImage seqnumImage = dggMultiLevelSource.getImage(level);

        return new SmosOpImage(valueProvider, rasterDataNode, seqnumImage, resolutionLevel, levelRegion);
    }
}
