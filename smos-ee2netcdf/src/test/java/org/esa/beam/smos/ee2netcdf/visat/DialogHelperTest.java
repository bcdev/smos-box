package org.esa.beam.smos.ee2netcdf.visat;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class DialogHelperTest {

    private final boolean isGuiAvailable;

    public DialogHelperTest() {
        isGuiAvailable = !GraphicsEnvironment.isHeadless();
    }

    @Test
    public void testIsSupportedType() {
        assertTrue(DialogHelper.isSupportedType("MIR_BWLD1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_BWSD1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_BWLF1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_BWSF1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_SCSF1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_SCLF1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_SCSD1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_SCLD1C"));
        assertTrue(DialogHelper.isSupportedType("MIR_SMUDP2"));
        assertTrue(DialogHelper.isSupportedType("MIR_OSUDP2"));

        assertFalse(DialogHelper.isSupportedType("MPL_XBDOWN"));
        assertFalse(DialogHelper.isSupportedType("MIR_SMDAP2"));
        assertFalse(DialogHelper.isSupportedType("MIR_CORN0_"));
        assertFalse(DialogHelper.isSupportedType("AUX_ECMWF_"));
        assertFalse(DialogHelper.isSupportedType("MIR_AFWU1A"));
    }

    @Test
    public void testCanProductSelectionBeEnabled_noProduct() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        assertNull(appContext.getSelectedProduct());

        assertFalse(DialogHelper.canProductSelectionBeEnabled(appContext));
    }

    @Test
    public void testCanProductSelectionBeEnabled_wrongProductType() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        appContext.setSelectedProduct(new Product("test", "MER_RR__1P", 2, 2));
        assertNotNull(appContext.getSelectedProduct());

        assertFalse(DialogHelper.canProductSelectionBeEnabled(appContext));
    }

    @Test
    public void testCanProductSelectionBeEnabled_validProductType() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        appContext.setSelectedProduct(new Product("test", "MIR_SCLF1C", 2, 2));
        assertNotNull(appContext.getSelectedProduct());

        assertTrue(DialogHelper.canProductSelectionBeEnabled(appContext));
    }

    @Test
    public void testGetSelectedSmosProduct_noProduct() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        assertNull(appContext.getSelectedProduct());

        assertNull(DialogHelper.getSelectedSmosProduct(appContext));
    }

    @Test
    public void testGetSelectedSmosProduct_wrongProductType() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        appContext.setSelectedProduct(new Product("test", "MER_RR__1P", 2, 2));
        assertNotNull(appContext.getSelectedProduct());

        assertNull(DialogHelper.getSelectedSmosProduct(appContext));
    }

    @Test
    public void testGetSelectedSmosProduct_validProductType() {
        assumeTrue(isGuiAvailable);
        final DefaultAppContext appContext = new DefaultAppContext("test");
        final Product expectedProduct = new Product("test", "MIR_OSUDP2", 2, 2);
        appContext.setSelectedProduct(expectedProduct);
        assertNotNull(appContext.getSelectedProduct());

        final Product selectedProduct = DialogHelper.getSelectedSmosProduct(appContext);
        assertEquals(expectedProduct, selectedProduct);
    }
}
