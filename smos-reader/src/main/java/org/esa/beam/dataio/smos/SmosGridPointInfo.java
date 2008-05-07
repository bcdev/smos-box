package org.esa.beam.dataio.smos;

import java.util.Arrays;
import java.io.Writer;
import java.io.IOException;
import java.io.PrintWriter;
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
}
