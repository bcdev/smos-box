package org.esa.beam.dataio.smos;/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.ma2.StructureMembers;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for trying out reading SMOS BUFR formatted files obtained from 'ideas-nas.eo.esa.int'.
 * <p/>
 * The test has a dependency to the Unidata BUFR module.
 * <p/>
 * <pre>
 *      <dependency>
 *          <groupId>edu.ucar</groupId>
 *          <artifactId>bufr</artifactId>
 *          <version>4.3.19</version>
 *      </dependency>
 * </pre>
 *
 * @author Ralf Quast
 */
@Ignore
public class BufrTest {

    @Before
    public void registerBufrIosp() throws Exception {
        NetcdfFile.registerIOProvider("ucar.nc2.iosp.bufr.BufrIosp");
    }

    @Ignore
    @Test
    public void testBufrIosp() throws Exception {
        assertTrue(NetcdfFile.iospRegistered(null)); // replace 'null' with 'BufrIosp.class'
    }

    @Ignore
    @Test
    public void testCanReadBufrFiles() throws Exception {
        NetcdfFile dataset;

        dataset = NetcdfFile.open(
                "ideas-nas.eo.esa.int/miras_20131028_002942_20131028_003302_smos_20947_t_20131028_033058_l1c.bufr");

        assertNotNull(dataset);

        performAssertions(dataset);

        dataset = NetcdfFile.open(
                "ideas-nas.eo.esa.int/miras_20131028_003256_20131028_020943_smos_20947_o_20131028_031005_l1c.bufr");

        assertNotNull(dataset);

        performAssertions(dataset);
    }

    private static void performAssertions(NetcdfFile dataset) throws IOException {
        final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
        for (Attribute globalAttribute : globalAttributes) {
            assertNotNull(globalAttribute.getFullName());
        }

        final List<Variable> variables = dataset.getVariables();
        assertEquals(1, variables.size());

        for (Variable variable : variables) {
            assertEquals("obs", variable.getFullName());
            assertTrue(variable.isVariableLength());
            assertEquals(DataType.SEQUENCE, variable.getDataType());

            final Sequence sequence = (Sequence) variable;
            final StructureDataIterator structureIterator = sequence.getStructureIterator();
            assertNotNull(structureIterator);

            while (structureIterator.hasNext()) {
                final StructureData structureData = structureIterator.next();
                assertNotNull(structureData);

                final StructureMembers structureMembers = structureData.getStructureMembers();
                assertEquals(80, structureMembers.getStructureSize());

                final List<StructureMembers.Member> members = structureMembers.getMembers();
                assertEquals(33, members.size());

                for (StructureMembers.Member member : members) {
                    assertNotNull(member.getFullName());
                    final DataType dataType = member.getDataType();
                    assertTrue(dataType.isNumeric() || dataType.isString() || dataType.isEnum());
                }
            }
        }
    }
}
