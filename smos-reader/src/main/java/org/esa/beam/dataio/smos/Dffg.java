package org.esa.beam.dataio.smos;

class Dffg {

    private final double maxLon;
    private final double minLon;
    private final double maxLat;
    private final double minLat;

    private final int latCount;
    private final double deltaLat;
    private final Row[] rows;

    private static class Row {

        private Row(int lonCount, double deltaLon, int cumulatedLonCount) {
            this.lonCount = lonCount;
            this.deltaLon = deltaLon;
            this.cumulatedLonCount = cumulatedLonCount;
        }

        private final int lonCount;
        private final double deltaLon;
        private final int cumulatedLonCount;
    }

    Dffg(double minLat, double maxLat, double minLon, double maxLon, int latCount, double deltaLat) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;

        this.latCount = latCount;
        this.deltaLat = deltaLat;

        rows = new Row[latCount];
    }


}
