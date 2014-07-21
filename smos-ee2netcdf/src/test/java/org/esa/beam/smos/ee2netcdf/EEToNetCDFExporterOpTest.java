package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.Coordinate;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EEToNetCDFExporterOpTest {

    @Test
    public void testOperatorAnnotations() {
        final Annotation[] declaredAnnotations = EEToNetCDFExporterOp.class.getDeclaredAnnotations();

        assertEquals(1, declaredAnnotations.length);
        final OperatorMetadata operatorMetadata = (OperatorMetadata) declaredAnnotations[0];
        assertEquals("SmosEE2NetCDF", operatorMetadata.alias());
        assertEquals("1.0", operatorMetadata.version());
        assertEquals("Tom Block", operatorMetadata.authors());
        assertEquals("(c) 2013, 2014 by Brockmann Consult", operatorMetadata.copyright());
        assertEquals("Converts SMOS EE Products to NetCDF format.", operatorMetadata.description());
    }

    @Test
    public void testGetOutputFile() {
        final File input = new File("bla/bla/change_my_name.zip");
        final File targetDir = new File("/target/di/rectory");

        final File outputFile = EEToNetCDFExporterOp.getOutputFile(input, targetDir);
        assertEquals("change_my_name.nc", outputFile.getName());
        assertEquals(targetDir.getAbsolutePath(), outputFile.getParentFile().getAbsolutePath());
    }

    @Test
    public void testCreateSubsetDef() {
        final Rectangle rectangle = new Rectangle(0, 1, 3, 4);

        final ProductSubsetDef subsetDef = EEToNetCDFExporterOp.createSubsetDef(rectangle);
        assertNotNull(subsetDef);

        final Rectangle subsetDefRegion = subsetDef.getRegion();
        assertEquals(rectangle.toString(), subsetDefRegion.toString());

        assertNull(subsetDef.getNodeNames());
        assertEquals(1, subsetDef.getSubSamplingX());
        assertEquals(1, subsetDef.getSubSamplingY());
    }

    @Test
    public void testConvert() {
        final ArrayList<double[]> rawCoords = new ArrayList<>();
        rawCoords.add(new double[]{1.0, 2.0});
        rawCoords.add(new double[]{3.0, 4.0});
        rawCoords.add(new double[]{5.0, 6.0});

        final Coordinate[] coordinates = EEToNetCDFExporterOp.convert(rawCoords);
        assertNotNull(coordinates);
        assertEquals(3, coordinates.length);
        assertEquals(1.0, coordinates[0].x, 1e-8);
        assertEquals(2.0, coordinates[0].y, 1e-8);
        assertEquals(3.0, coordinates[1].x, 1e-8);
        assertEquals(4.0, coordinates[1].y, 1e-8);
        assertEquals(5.0, coordinates[2].x, 1e-8);
        assertEquals(6.0, coordinates[2].y, 1e-8);
    }

    @Test
    public void testAlias() {
        assertEquals("SmosEE2NetCDF", EEToNetCDFExporterOp.ALIAS);
    }
}
