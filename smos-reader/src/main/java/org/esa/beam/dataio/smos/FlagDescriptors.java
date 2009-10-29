package org.esa.beam.dataio.smos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlagDescriptors {

    private final List<FlagDescriptor> descriptorList;
    private final Map<String, FlagDescriptor> descriptorMap;

    FlagDescriptors(List<String[]> stringRecordList) {
        descriptorList = new ArrayList<FlagDescriptor>(stringRecordList.size());
        descriptorMap = new HashMap<String, FlagDescriptor>(stringRecordList.size());

        for (String[] strings : stringRecordList) {
            final FlagDescriptor record = new FlagDescriptor(strings);
            descriptorList.add(record);
            descriptorMap.put(record.getFlagName(), record);
        }
    }

    public final List<FlagDescriptor> asList() {
        return Collections.unmodifiableList(descriptorList);
    }

    public final FlagDescriptor getDescriptor(String flagName) {
        return descriptorMap.get(flagName);
    }
}
