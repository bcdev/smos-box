package org.esa.beam.dataio.smos.dddb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemberDescriptors implements Family<MemberDescriptor> {

    private final List<MemberDescriptor> descriptorList;

    public MemberDescriptors() {
        descriptorList = new ArrayList<>();
    }

     void add(MemberDescriptor memberDescriptor) {
         descriptorList.add(memberDescriptor);
     }

    void remove(String memberName) {
        MemberDescriptor toRemove = null;
        for(MemberDescriptor descriptor : descriptorList) {
            if (memberName.equalsIgnoreCase(descriptor.getName())) {
                toRemove = descriptor;
                break;
            }
        }

        if (toRemove != null) {
            descriptorList.remove(toRemove);
        }
    }

    @Override
    public List<MemberDescriptor> asList() {
        return Collections.unmodifiableList(descriptorList);
    }

    @Override
    public MemberDescriptor getMember(String name) {
        return null;
    }
}
