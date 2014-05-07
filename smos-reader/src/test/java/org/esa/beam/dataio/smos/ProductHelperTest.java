package org.esa.beam.dataio.smos;

import org.esa.beam.dataio.smos.dddb.BandDescriptor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductHelperTest {

    public static final String VIRTUAL_BAND_NAME = "the_virtual_band";
    private Product product;
    private BandDescriptor bandDescriptor;

    @Before
    public void setUp() {
        product = new Product("bla", "test-Type", 2, 3);

        bandDescriptor = mock(BandDescriptor.class);
        when(bandDescriptor.getBandName()).thenReturn(VIRTUAL_BAND_NAME);
        when(bandDescriptor.hasTypicalMin()).thenReturn(true);
        when(bandDescriptor.getTypicalMin()).thenReturn(0.2);
        when(bandDescriptor.hasTypicalMax()).thenReturn(true);
        when(bandDescriptor.getTypicalMax()).thenReturn(1.2);
    }

    @Test
    public void testAddVirtualBand_bandDescriptionIsAdded() {
        when(bandDescriptor.getDescription()).thenReturn("the virtual description");

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals("the virtual description", band.getDescription());
    }

    @Test
    public void testAddVirtualBand_unitIsAdded() {
        when(bandDescriptor.getUnit()).thenReturn("it's virtual");

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals("it's virtual", band.getUnit());
    }

    @Test
    public void testAddVirtualBand_fillValueIsAdded() {
        when(bandDescriptor.getFillValue()).thenReturn(123.78);

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertEquals(123.78, band.getGeophysicalNoDataValue(), 1e-5);
    }

    @Test
    public void testAddVirtualBand_hasFillValueIsAdded() {
        when(bandDescriptor.hasFillValue()).thenReturn(true);

        ProductHelper.addVirtualBand(product, bandDescriptor, "2 * source_band");

        final Band band = product.getBand(VIRTUAL_BAND_NAME);
        assertNotNull(band);
        assertTrue(band.isNoDataValueUsed());
    }
}
