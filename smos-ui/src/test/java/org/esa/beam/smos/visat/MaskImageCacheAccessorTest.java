package org.esa.beam.smos.visat;

import junit.framework.TestCase;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.jai.ImageManager;

import java.awt.Color;

public class MaskImageCacheAccessorTest extends TestCase {

    public void testRemoveCachedMaskImage() throws NoSuchFieldException, IllegalAccessException {
        final String testExpression = "true";
        final Product testProduct = new Product("test", "test", 1, 1);

        final MaskImageCacheAccessor cache = new MaskImageCacheAccessor(ImageManager.getInstance());
        assertEquals(0, cache.size());

        ImageManager.getInstance().createColoredMaskImage(testExpression, testProduct, Color.ORANGE, true, 0);
        assertEquals(1, cache.size());
        assertNotNull(cache.get(testExpression, testProduct));

        MaskImageCacheAccessor.remove(testExpression, testProduct, ImageManager.getInstance());
        assertEquals(0, cache.size());
        assertNull(cache.get(testExpression, testProduct));
    }

}
