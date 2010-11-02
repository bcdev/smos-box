package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.CompoundData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MultiFilter implements GridPointFilter {

    private final List<GridPointFilter> filterList = new ArrayList<GridPointFilter>();

    void add(GridPointFilter filter) {
        filterList.add(filter);
    }

    @Override
    public boolean accept(int id, CompoundData gridPointData) throws IOException {
        for (final GridPointFilter filter : filterList) {
            if (filter.accept(id, gridPointData)) {
                return true;
            }
        }
        return false;
    }
}
