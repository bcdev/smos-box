package org.esa.beam.smos.ee2netcdf.visat;


import org.esa.beam.framework.gpf.ui.DefaultAppContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetCDFExportDialogTest {

// Test is just for displaying the dialog - do not enable on commits. Buildserver should not open Dialogs - tb 2013-03-27
//    @Test
//    public void testShow() {
//        final DefaultAppContext appContext = new DefaultAppContext("test");
//        final NetCDFExportDialog netCDFExportDialog = new NetCDFExportDialog(appContext, "bla");
//        netCDFExportDialog.show();
//    }

    @Test
    public void testIsSupportedType() {
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_BWLD1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_BWSD1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_BWLF1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_BWSF1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_SCSF1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_SCLF1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_SCSD1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_SCLD1C"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_SMUDP2"));
        assertTrue(NetCDFExportDialog.isSupportedType("MIR_OSUDP2"));

        assertFalse(NetCDFExportDialog.isSupportedType("MPL_XBDOWN"));
        assertFalse(NetCDFExportDialog.isSupportedType("MIR_SMDAP2"));
        assertFalse(NetCDFExportDialog.isSupportedType("MIR_CORN0_"));
        assertFalse(NetCDFExportDialog.isSupportedType("AUX_ECMWF_"));
        assertFalse(NetCDFExportDialog.isSupportedType("MIR_AFWU1A"));
    }
}
