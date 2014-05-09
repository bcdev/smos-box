/*
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

package org.esa.beam.dataio.smos;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.ma2.StructureMembers;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Sequence;
import ucar.nc2.Variable;
import ucar.nc2.iosp.bufr.BufrIosp;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for trying out reading SMOS BUFR formatted files obtained from 'ideas-nas.eo.esa.int'.
 *
 * @author Ralf Quast
 */
@Ignore
public class BufrTest {

    @Before
    public void registerBufrIosp() throws Exception {
        NetcdfFile.registerIOProvider(BufrIosp.class);
    }

    @Test
    public void testBufrIosp() throws Exception {
        assertTrue(NetcdfFile.iospRegistered(BufrIosp.class));
    }

    @Test
    public void testCanReadBufrFiles() throws Exception {
        NetcdfFile dataset;
        /*
        dataset = NetcdfFile.open(
                "/Users/ralf/Desktop/ideas-nas.eo.esa.int/miras_20131028_002942_20131028_003302_smos_20947_t_20131028_033058_l1c.bufr");

        assertNotNull(dataset);

        performAssertions(dataset);
        */
        dataset = NetcdfFile.open(
                "/Users/ralf/Desktop/ideas-nas.eo.esa.int/miras_20131028_003256_20131028_020943_smos_20947_o_20131028_031005_l1c.bufr");

        assertNotNull(dataset);

        performAssertions(dataset);
    }

    @Test
    public void testCanReadBufrLightFiles() throws Exception {
        NetcdfFile dataset;


        dataset = NetcdfFile.open(
                "/Users/ralf/Desktop/ideas-nas.eo.esa.int/W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028030552_20131028003256_20131028020943_bufr_v505.bin");

        assertNotNull(dataset);

        performAssertions(dataset);


        dataset = NetcdfFile.open(
                "/Users/ralf/Desktop/ideas-nas.eo.esa.int/W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028033037_20131028002942_20131028003302_bufr_v505.bin");

        assertNotNull(dataset);

        performAssertions(dataset);

        /*
        dataset = NetcdfFile.open(
                "/Users/ralf/Desktop/ideas-nas.eo.esa.int/W_ES-ESA-ESAC,SMOS,N256_C_LEMM_20131028044206_20131028020942_20131028034943_bufr_v505.bin");

        assertNotNull(dataset);

        performAssertions(dataset);
        */
    }

    private static void performAssertions(NetcdfFile dataset) throws IOException, InvalidRangeException {
        //dataset.writeCDL(System.out, false);

        final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
        for (Attribute globalAttribute : globalAttributes) {
            assertNotNull(globalAttribute.getFullName());
        }

        final List<Variable> variables = dataset.getVariables();
        assertEquals(1, variables.size());

        final Variable variable = variables.get(0);
        assertEquals("obs", variable.getFullName());
        assertTrue(variable.isVariableLength());
        assertEquals(DataType.SEQUENCE, variable.getDataType());

        final Sequence sequence = (Sequence) variable;
        final StructureDataIterator structureIterator = sequence.getStructureIterator();
        assertNotNull(structureIterator);

        final List<Variable> variableList = sequence.getVariables();
        final Variable listVariable = variableList.get(3);
        final Array array = listVariable.read();
        assertNotNull(array);

        while (structureIterator.hasNext()) {
            final StructureData structureData = structureIterator.next();
            assertNotNull(structureData);

            final List<StructureMembers.Member> members = structureData.getMembers();
            assertEquals(33, members.size());

            final Array gridPointCountData = structureData.getArray("Number_of_grid_points");
            assertNotNull(gridPointCountData);

            final Array gridPointIdentifierData = structureData.getArray("Grid_point_identifier");
            assertNotNull(gridPointIdentifierData);
            assertEquals(1, gridPointIdentifierData.getSize());

            final Array snapshotIdentifierData = structureData.getArray("Snapshot_identifier");
            assertNotNull(snapshotIdentifierData);
            assertEquals(1, snapshotIdentifierData.getSize());

            final Array brightnessTemperatureRealPartData = structureData.getArray("Brightness_temperature_real_part");
            assertNotNull(brightnessTemperatureRealPartData);
            assertEquals(1, brightnessTemperatureRealPartData.getSize());
        }
    }
}
