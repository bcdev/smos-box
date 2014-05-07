package org.esa.beam.dataio.smos;

import org.esa.beam.framework.datamodel.Product;

import java.awt.geom.Area;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author Ralf Quast
 */
public interface ProductFile extends Closeable {

    @Override
    void close() throws IOException;

    Product createProduct() throws IOException;

    Area getArea();

    File getDataFile();
}
