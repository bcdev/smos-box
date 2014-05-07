package org.esa.beam.dataio.smos;

import java.io.IOException;

/**
 * @author Ralf Quast
 */
public interface PointList {

    int getElementCount();

    double getLon(int i) throws IOException;

    double getLat(int i) throws IOException;
}
