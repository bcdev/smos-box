package org.esa.beam.dataio.smos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BandDescriptors {

    private final List<BandDescriptor> recordList;
    private final Map<String, BandDescriptor> recordMap;

    BandDescriptors(List<String[]> stringRecordList) {
        recordList = new ArrayList<BandDescriptor>(stringRecordList.size());
        recordMap = new HashMap<String, BandDescriptor>(stringRecordList.size());

        for (String[] strings : stringRecordList) {
            final BandDescriptor record = new BandDescriptor(strings);
            recordList.add(record);
            recordMap.put(record.getBandName(), record);
        }
    }

    public final List<BandDescriptor> asList() {
        return Collections.unmodifiableList(recordList);
    }

    public final BandDescriptor getDescriptor(String bandName) {
        return recordMap.get(bandName);
    }
}
