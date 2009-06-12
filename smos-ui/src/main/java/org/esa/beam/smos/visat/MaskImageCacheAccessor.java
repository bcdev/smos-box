package org.esa.beam.smos.visat;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelSource;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.jai.ImageManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

final class MaskImageCacheAccessor {

    private final WeakReference<Map<?, MultiLevelImage>> maskImageMap;

    public static void remove(ImageManager imageManager, Product product, String expression) {
        try {
            new MaskImageCacheAccessor(imageManager).remove(product, expression);
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    private static void remove(MultiLevelSource multiLevelSource, ImageManager imageManager) {
        try {
            new MaskImageCacheAccessor(imageManager).remove(multiLevelSource);
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    public static void removeAll(ImageManager imageManager, Product product) {
        try {
            new MaskImageCacheAccessor(imageManager).removeAll(product);
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    MaskImageCacheAccessor(ImageManager imageManager) throws NoSuchFieldException, IllegalAccessException {
        final Field maskImageMapField = imageManager.getClass().getDeclaredField("maskImageMap");
        @SuppressWarnings({"unchecked"})
        final Map<?, MultiLevelImage> map = (Map<?, MultiLevelImage>) getValue(imageManager, maskImageMapField);
        maskImageMap = new WeakReference<Map<?, MultiLevelImage>>(map);
    }

    MultiLevelImage get(Product product, String expression) throws NoSuchFieldException, IllegalAccessException {
        for (final Object key : maskImageMap.get().keySet()) {
            if (getProduct(key) == product && getExpression(key).equals(expression)) {
                //noinspection SuspiciousMethodCalls
                return maskImageMap.get().get(key);
            }
        }
        return null;
    }

    void remove(Product product, String expression) {
        final Iterator<?> iterator = maskImageMap.get().keySet().iterator();

        try {
            while (iterator.hasNext()) {
                final Object key = iterator.next();
                if (getProduct(key) == product && getExpression(key).equals(expression)) {
                    iterator.remove();
                    return;
                }
            }
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    private void remove(MultiLevelSource multiLevelSource) {
        try {
            Object obj = getValue(multiLevelSource, "expression");
            if (obj instanceof String) {
                final String expression = (String) obj;
                obj = getValue(multiLevelSource, "product");
                if (obj instanceof Product) {
                    final Product product = (Product) obj;
                    remove(product, expression);
                }
            }
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    void removeAll(Product product) {
        final Iterator<?> iterator = maskImageMap.get().keySet().iterator();

        try {
            while (iterator.hasNext()) {
                final Object key = iterator.next();
                if (getProduct(key) == product) {
                    iterator.remove();
                }
            }
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    int size() {
        return maskImageMap.get().size();
    }

    private static Object getProduct(Object key) throws NoSuchFieldException, IllegalAccessException {
        final Field productField = key.getClass().getDeclaredField("product");
        @SuppressWarnings({"unchecked"})
        final WeakReference<Product> weakReference = (WeakReference<Product>) getValue(key, productField);
        final Field referentField = weakReference.getClass().getSuperclass().getDeclaredField("referent");

        return getValue(weakReference, referentField);
    }

    private static Object getExpression(Object key) throws NoSuchFieldException, IllegalAccessException {
        return getValue(key, "expression");
    }

    private static Object getValue(Object obj, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        final Object fieldValue = field.get(obj);
        field.setAccessible(false);

        return fieldValue;
    }

    private static Object getValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return getValue(obj, obj.getClass().getDeclaredField(fieldName));
    }

}
