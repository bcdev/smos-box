package org.esa.beam.dataio.smos;

import java.util.Arrays;

class GridPointInfo {

    final int minSeqnum;
    final int maxSeqnum;
    final int[] indexes;

    GridPointInfo(int minSeqnum, int maxSeqnum) {
        this.minSeqnum = minSeqnum;
        this.maxSeqnum = maxSeqnum;
        indexes = new int[maxSeqnum - minSeqnum + 1];
        Arrays.fill(indexes, -1);
    }

    void setSequenceNumbers(int[] sequenceNumbers) {
        for (int i = 0; i < sequenceNumbers.length; i++) {
            indexes[sequenceNumbers[i] - minSeqnum] = i;
        }
    }

    int getGridPointIndex(int seqnum) {
        if (seqnum < minSeqnum || seqnum > maxSeqnum) {
            return -1;
        }

        return indexes[seqnum - minSeqnum];
    }
}
