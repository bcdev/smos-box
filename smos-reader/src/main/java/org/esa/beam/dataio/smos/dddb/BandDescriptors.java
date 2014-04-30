package org.esa.beam.dataio.smos.dddb;


import java.util.*;

class BandDescriptors implements Family<BandDescriptor> {
    private final List<BandDescriptor> descriptorList;
    private final Map<String, BandDescriptor> descriptorMap;

    BandDescriptors(List<String[]> recordList, Dddb dddb) {
        descriptorList = new ArrayList<>(recordList.size());
        descriptorMap = new HashMap<>(recordList.size());

        for (final String[] tokens : recordList) {
            final BandDescriptorImpl bandDescriptor = new BandDescriptorImpl(tokens, dddb);
            descriptorList.add(bandDescriptor);
            descriptorMap.put(bandDescriptor.getBandName(), bandDescriptor);
        }
    }

    @Override
    public final List<BandDescriptor> asList() {
        return Collections.unmodifiableList(descriptorList);
    }

    @Override
    public final BandDescriptor getMember(String bandName) {
        return descriptorMap.get(bandName);
    }
}
