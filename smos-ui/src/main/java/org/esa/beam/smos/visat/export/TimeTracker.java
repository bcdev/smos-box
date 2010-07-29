/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

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
