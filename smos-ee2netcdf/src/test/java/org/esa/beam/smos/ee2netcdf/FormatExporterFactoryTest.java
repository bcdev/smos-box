package org.esa.beam.smos.ee2netcdf;


import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FormatExporterFactoryTest {

    @Test
    public void testCreateExporterForBrowseProduct() throws IOException {
        final FormatExporter exporter = FormatExporterFactory.create("SM_OPER_MIR_BWLF1C_20111026T143206_20111026T152520_503_001_1.zip");
        assertThat(exporter, is(instanceOf(BrowseFormatExporter.class)));
    }

    @Test
    public void testCreateExporterForL1CProduct() throws IOException {
        final FormatExporter exporter = FormatExporterFactory.create("SM_REPB_MIR_SCLF1C_20110201T151254_20110201T151308_505_152_1.hdr");
        assertThat(exporter, is(instanceOf(L1CFormatExporter.class)));
    }

    @Test
    public void testCreateExporterForL2Product() throws IOException {
        FormatExporter exporter = FormatExporterFactory.create("SM_OPER_MIR_OSUDP2_20091204T001853_20091204T011255_310_001_1.dbl");
        assertThat(exporter, is(instanceOf(L2FormatExporter.class)));

        exporter = FormatExporterFactory.create("SM_OPER_MIR_SMUDP2_20101019T050111_20101019T053129_309_001_1.zip");
        assertThat(exporter, is(instanceOf(L2FormatExporter.class)));
    }

    @Test
    public void testCreateExporterForUnsupportedFormat() {
        try {
            FormatExporterFactory.create("really_weired_format.dbl");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}
