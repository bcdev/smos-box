package org.esa.beam.dataio.smos.dddb;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class BandDescriptorsTest {

    private BandDescriptors bandDescriptors;
    private ArrayList<String[]> recordList;

    @Before
    public void setIUp(){
        final String[] tokens_1 = new String[] {"false", "theBand_1", "theMember_1", "18", "19", "20.0", "21.1", "22.2", "23.3", "true", "24.4", "pixelExpression_1", "unit_1", "description_1", "codingName_1", "flagDescriptors_1"};
        final String[] tokens_2 = new String[] {"false", "theBand_2", "theMember_2", "18", "19", "20.0", "21.1", "22.2", "23.3", "true", "24.4", "pixelExpression_2", "unit_2", "description_2", "codingName_2", "flagDescriptors_2"};
        recordList = new ArrayList<>();
        recordList.add(tokens_1);
        recordList.add(tokens_2);
        final Dddb dddb = mock(Dddb.class);

        bandDescriptors = new BandDescriptors(recordList, dddb);
    }
    @Test
    public void testAsList() {
        final List<BandDescriptor> descriptorList = bandDescriptors.asList();
        assertNotNull(descriptorList);
        assertEquals(2, descriptorList.size());
        assertThat(descriptorList, is(instanceOf(Collections.unmodifiableList(recordList).getClass())));

        assertEquals("theBand_1", descriptorList.get(0).getBandName());
    }

    @Test
    public void testGetMember() {
        final BandDescriptor member = bandDescriptors.getMember("theBand_2");
        assertNotNull(member);
        assertEquals("theMember_2", member.getMemberName());
    }

    @Test
    public void testInterfaceImplemented() {
        assertThat(bandDescriptors, is(instanceOf(Family.class)));
    }
}
