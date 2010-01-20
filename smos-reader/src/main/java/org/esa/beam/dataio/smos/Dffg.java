package org.esa.beam.dataio.smos;

import com.bc.ceres.binio.SequenceData;

class Dffg {

    private final double maxLon;
    private final double minLon;
    private final double maxLat;
    private final double minLat;
    private final double latDelta;

    private final int[] columnCounts;
    private final double[] lonDeltas;
    private final int[] cumulatedColumnCounts;
    private final SequenceData sequenceData;

    Dffg(double minLat, double maxLat, double minLon, double maxLon, double latDelta, SequenceData sequenceData) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;

        this.latDelta = latDelta;
        this.sequenceData = sequenceData;

        final int rowCount = (int) Math.round((maxLat - minLat) / latDelta);
        columnCounts = new int[rowCount];
        lonDeltas = new double[rowCount];
        cumulatedColumnCounts = new int[rowCount];
    }

    int getIndex(double lon, double lat) {
        if (lon < 0.0) {
            lon += 360.0;
        }
        if (lon >= minLon && lon < maxLon && lat > minLat && lat <= maxLat) {
            final int rowIndex = getRowIndex(lat);
            final int columnIndex = getColumnIndex(lon, rowIndex);

            return cumulatedColumnCounts[rowIndex] + columnIndex;
        }

        return -1;
    }

    SequenceData getSequenceData() {
        return sequenceData;
    }

    void setRow(int rowIndex, int columnCount, double deltaLon, int cumulatedColumnCount) {
        columnCounts[rowIndex] = columnCount;
        lonDeltas[rowIndex] = deltaLon;
        cumulatedColumnCounts[rowIndex] = cumulatedColumnCount;
    }

    private int getRowIndex(double lat) {
        return (int) Math.floor((maxLat - lat) / latDelta);
    }

    private int getColumnIndex(double lon, int rowIndex) {
        return (int) Math.floor((lon - minLon) / lonDeltas[rowIndex]);
    }
}
