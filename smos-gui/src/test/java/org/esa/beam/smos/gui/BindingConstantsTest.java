package org.esa.beam.smos.gui;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BindingConstantsTest {

    @Test
    public void testBindingConstants() {
       assertEquals("useSelectedProduct", BindingConstants.SELECTED_PRODUCT);
       assertEquals("sourceDirectory", BindingConstants.SOURCE_DIRECTORY);
    }
}
