package org.esa.beam.dataio.smos.dddb;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class FlagDescriptorsTest {

    private FlagDescriptors flagDescriptors;
    private ArrayList<String[]> recordList;

    @Before
    public void setUp() {
        final String[] tokens_1 = new String[]{"true", "flagName_1", "1000", "2000", "0.78", "description_1"};
        final String[] tokens_2 = new String[]{"false", "flagName_2", "3000", "4000", "0.88", "description_2"};
        recordList = new ArrayList<>();
        recordList.add(tokens_1);
        recordList.add(tokens_2);

        flagDescriptors = new FlagDescriptors(recordList);
    }

    @Test
    public void testAsList() {
        final List<FlagDescriptor> descriptorList = flagDescriptors.asList();
        assertNotNull(descriptorList);
        assertThat(descriptorList, is(instanceOf(Collections.unmodifiableList(recordList).getClass())));
        assertEquals(2, descriptorList.size());
        assertEquals("description_2", descriptorList.get(1).getDescription());
    }

    @Test
    public void testGetMember() {
        final FlagDescriptor member = flagDescriptors.getMember("flagName_1");
        assertNotNull(member);
        assertEquals(4096, member.getMask());
    }

    @Test
    public void testInterfaceImplemented() {
         assertThat(flagDescriptors, is(instanceOf(Family.class)));
    }
}
