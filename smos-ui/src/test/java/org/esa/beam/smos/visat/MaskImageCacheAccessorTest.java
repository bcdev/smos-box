package org.esa.beam.smos.visat;

import junit.framework.TestCase;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.jai.ImageManager;

import java.awt.Color;

public class MaskImageCacheAccessorTest extends TestCase {

    public void testRemoveAllMaskImagesForProduct() throws NoSuchFieldException, IllegalAccessException {
        final Product product1 = new Product("1", "test", 1, 1);
        final Product product2 = new Product("2", "test", 1, 1);

        final MaskImageCacheAccessor cache = new MaskImageCacheAccessor(ImageManager.getInstance());
        assertEquals(0, cache.size());

        ImageManager.getInstance().createColoredMaskImage("true", product1, Color.ORANGE, true, 0);
        ImageManager.getInstance().createColoredMaskImage("true", product2, Color.ORANGE, true, 0);
        assertEquals(2, cache.size());
        assertNotNull(cache.get(product1, "true"));
        assertNotNull(cache.get(product2, "true"));

        MaskImageCacheAccessor.removeAll(ImageManager.getInstance(), product1);
        assertEquals(1, cache.size());
        assertNull(cache.get(product1, "true"));
        assertNotNull(cache.get(product2, "true"));

        MaskImageCacheAccessor.removeAll(ImageManager.getInstance(), product2);
        assertEquals(0, cache.size());
        assertNull(cache.get(product2, "true"));
    }

    public void testRemoveMaskImage() throws NoSuchFieldException, IllegalAccessException {
        final Product product = new Product("1", "test", 1, 1);

        final MaskImageCacheAccessor cache = new MaskImageCacheAccessor(ImageManager.getInstance());
        assertEquals(0, cache.size());

        ImageManager.getInstance().createColoredMaskImage("true", product, Color.ORANGE, true, 0);
        assertEquals(1, cache.size());
        assertNotNull(cache.get(product, "true"));

        MaskImageCacheAccessor.remove(ImageManager.getInstance(), product, "true");
        assertEquals(0, cache.size());
        assertNull(cache.get(product, "true"));
    }

}
