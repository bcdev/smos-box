package org.esa.beam.dataio.smos;

import java.awt.geom.Area;

interface LaiValueProvider {

    Area getArea();

    long getCellIndex(double lon, double lat);

    byte getValue(long cellIndex, byte noDataValue);

    short getValue(long cellIndex, short noDataValue);

    int getValue(long cellIndex, int noDataValue);

    float getValue(long cellIndex, float noDataValue);
}
