package org.esa.beam.dataio.smos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BandDescriptors {

    private final List<BandDescriptor> descriptorList;
    private final Map<String, BandDescriptor> descriptorMap;

    BandDescriptors(List<String[]> recordList) {
        descriptorList = new ArrayList<BandDescriptor>(recordList.size());
        descriptorMap = new HashMap<String, BandDescriptor>(recordList.size());

        for (String[] tokens : recordList) {
            final BandDescriptor descriptor = new BandDescriptor(tokens);
            descriptorList.add(descriptor);
            descriptorMap.put(descriptor.getBandName(), descriptor);
        }
    }

    public final List<BandDescriptor> asList() {
        return Collections.unmodifiableList(descriptorList);
    }

    public final BandDescriptor getDescriptor(String bandName) {
        return descriptorMap.get(bandName);
    }
}
