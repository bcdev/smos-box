package org.esa.beam.dataio.smos;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.io.PrintStream;


class SmosGridPointInfo {
    private final int gridPointIdMin;
    private final int gridPointIdMax;
    private final int[] gridPointOffsets;


    SmosGridPointInfo(int gridPointIdMin, int gridPointIdMax) {
        this.gridPointIdMin = gridPointIdMin;
        this.gridPointIdMax = gridPointIdMax;
        gridPointOffsets = new int[1 + gridPointIdMax - gridPointIdMin];
        Arrays.fill(gridPointOffsets, -1);
    }

    public final int getOffset(int gridPointId) {
        if (gridPointId < gridPointIdMin || gridPointId > gridPointIdMax) {
            return -1;
        }
        return gridPointOffsets[gridPointId - gridPointIdMin];
    }

    public final void setOffset(int gridPointId, int offset) {
        if (gridPointId < gridPointIdMin || gridPointId > gridPointIdMax) {
            throw new IllegalArgumentException("gridPointId");
        }
        gridPointOffsets[gridPointId - gridPointIdMin] = offset;
    }

    public void dump(PrintStream stream) {
        for (int i = 0; i < gridPointOffsets.length; i++) {
            int gridPointOffset = gridPointOffsets[i];
            stream.print(i);
            stream.print(',');
            stream.print(gridPointOffset);
            stream.print('\n');
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" +
                "[gridPointIdMin="+gridPointIdMin+
                ",gridPointIdMax="+gridPointIdMax+
                ",gridPointOffsets.length="+gridPointOffsets.length+
                 "]";
    }

//    private ArrayList<Range> ranges = new ArrayList<Range>(128);
//
//    public int findRangeIndex(int gridPointId) {
//        final int n = ranges.size();
//        final int i00 = 0;
//        final int i01 = n / 2;
//        final int i10 = i01 + 1;
//        final int i11 = n - 1;
//    }
//
//    public int findRangeIndex(int gridPointId, int i0, int i1) {
//        final int n = i1 - i0 + 1;
//        final Range range = ranges.get(i0);
//        if (range.contains(gridPointId)) {
//            return
//        }
//    }
//
//    private static class Range {
//        private final int min;
//        private final int max;
//        private int[] offsets;
//
//        private Range(int min, int max) {
//            this.min = min;
//            this.max = max;
//            this.offsets = new int[8];
//        }
//
//        public boolean intersects(Range other) {
//            return other.max >= min && other.min <= max;
//        }
//
//        public boolean contains(int v) {
//            return v >= min && v <= max;
//        }
//    }
}
