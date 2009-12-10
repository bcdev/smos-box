package org.esa.beam.smos.visat.export;

import java.util.Date;

class TimeTracker {
    private long intervalStart;
    private long intervalStop;

    TimeTracker() {
        intervalStart = Long.MAX_VALUE;
        intervalStop = Long.MIN_VALUE;
    }

    Date getIntervalStart() {
        return new Date(intervalStart);
    }

    Date getIntervalStop() {
        return new Date(intervalStop);
    }

    void track(Date date) {
        final long currentMillis = date.getTime();

        if (intervalStart > currentMillis) {
            intervalStart = currentMillis;
        }
        if (intervalStop < currentMillis) {
            intervalStop = currentMillis;
        }
    }

    boolean hasValidPeriod() {
        return intervalStop >= intervalStart;
    }
}
