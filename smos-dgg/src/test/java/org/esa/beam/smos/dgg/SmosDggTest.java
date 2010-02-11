/* 
 * Copyright (C) 2002-2008 by Brockmann Consult
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.smos.dgg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Antonio Gutierrez (antonio.gutierrez@deimos.com.pt) wrote:
 * <p/>
 * The numbering in the SMOS DGG follows a combination of the first and second counters in the
 * "isea4h9_cell_in_column" file.
 * The second counter is the one which contemplates the 10 zones (each zone being two adjacent
 * triangles of the icosahedron). Each zone is composed by 262144 points, and can be identified
 * by the first 2 digits of the counter (i.e. 01000000000 -> zone 1, 10333333333 -> zone 10).
 * <p/>
 * The first and last point in the file do not belong to any of these 10 zones, but we incorporated
 * them into the first and last zones respectively, so they have one extra point in the end.
 * <p/>
 * The remaining digits of the second counter should indicate the number within the zone, but
 * the encoding used is based on quads, so we reduced it to a sequential numbering. In short,
 * the method we used is the following:
 * <p/>
 * <pre>
 * 1, 00000000000 -> 1
 * 2, 01000000000 -> 2
 * 3, 01000000001 -> 3
 * ...
 * 262144, 01333333332 -> 262144
 * 262145, 01333333333 -> 262145
 * 262146, 02000000000 -> 1000001
 * 262147, 02000000001 -> 1000002
 * ...
 * 524288, 02333333332 -> 1262143
 * 524289, 02333333333 -> 1262144
 * 524290, 03000000000 -> 2000001
 * 524291, 03000000001 -> 2000002
 * ...
 * 2359296, 09333333332 -> 8262143
 * 2359297, 09333333333 -> 8262144
 * 2359298, 10000000000 -> 9000001
 * 2359299, 10000000001 -> 9000002
 * ...
 * 2621441, 10333333333 ->9262144
 * 2621442, 11000000000 ->9262145
 * </pre>
 */
public class SmosDggTest {

    @Test
    public void gridPointIdToSeqnum() {
        assertEquals(1, SmosDgg.gridPointIdToSeqnum(1));
        assertEquals(2, SmosDgg.gridPointIdToSeqnum(2));
        assertEquals(3, SmosDgg.gridPointIdToSeqnum(3));
        assertEquals(262144, SmosDgg.gridPointIdToSeqnum(262144));
        assertEquals(262145, SmosDgg.gridPointIdToSeqnum(262145));

        assertEquals(262146, SmosDgg.gridPointIdToSeqnum(1000001));
        assertEquals(262147, SmosDgg.gridPointIdToSeqnum(1000002));
        assertEquals(524288, SmosDgg.gridPointIdToSeqnum(1262143));
        assertEquals(524289, SmosDgg.gridPointIdToSeqnum(1262144));

        assertEquals(524290, SmosDgg.gridPointIdToSeqnum(2000001));
        assertEquals(524291, SmosDgg.gridPointIdToSeqnum(2000002));

        assertEquals(2359296, SmosDgg.gridPointIdToSeqnum(8262143));
        assertEquals(2359297, SmosDgg.gridPointIdToSeqnum(8262144));

        assertEquals(2359298, SmosDgg.gridPointIdToSeqnum(9000001));
        assertEquals(2359299, SmosDgg.gridPointIdToSeqnum(9000002));
        assertEquals(2621441, SmosDgg.gridPointIdToSeqnum(9262144));
        assertEquals(2621442, SmosDgg.gridPointIdToSeqnum(9262145));
    }

    @Test
    public void gridPointIdToSeqnumInZone() {
        assertEquals(1, SmosDgg.gridPointIdToSeqnumInZone(1));
        assertEquals(2, SmosDgg.gridPointIdToSeqnumInZone(2));
        assertEquals(3, SmosDgg.gridPointIdToSeqnumInZone(3));

        assertEquals(262144, SmosDgg.gridPointIdToSeqnumInZone(262144));
        assertEquals(262145, SmosDgg.gridPointIdToSeqnumInZone(262145));

        assertEquals(1, SmosDgg.gridPointIdToSeqnumInZone(1000001));
        assertEquals(2, SmosDgg.gridPointIdToSeqnumInZone(1000002));
        assertEquals(262143, SmosDgg.gridPointIdToSeqnumInZone(1262143));
        assertEquals(262144, SmosDgg.gridPointIdToSeqnumInZone(1262144));

        assertEquals(1, SmosDgg.gridPointIdToSeqnumInZone(2000001));
        assertEquals(2, SmosDgg.gridPointIdToSeqnumInZone(2000002));

        assertEquals(262143, SmosDgg.gridPointIdToSeqnumInZone(8262143));
        assertEquals(262144, SmosDgg.gridPointIdToSeqnumInZone(8262144));

        assertEquals(1, SmosDgg.gridPointIdToSeqnumInZone(9000001));
        assertEquals(2, SmosDgg.gridPointIdToSeqnumInZone(9000002));
        assertEquals(262144, SmosDgg.gridPointIdToSeqnumInZone(9262144));
        assertEquals(262145, SmosDgg.gridPointIdToSeqnumInZone(9262145));
    }

    @Test
    public void gridPointIdToZoneId() {
        assertEquals(1, SmosDgg.gridPointIdToZoneId(1));
        assertEquals(1, SmosDgg.gridPointIdToZoneId(2));
        assertEquals(1, SmosDgg.gridPointIdToZoneId(3));

        assertEquals(1, SmosDgg.gridPointIdToZoneId(262144));
        assertEquals(1, SmosDgg.gridPointIdToZoneId(262145));

        assertEquals(2, SmosDgg.gridPointIdToZoneId(1000001));
        assertEquals(2, SmosDgg.gridPointIdToZoneId(1000002));
        assertEquals(2, SmosDgg.gridPointIdToZoneId(1262143));
        assertEquals(2, SmosDgg.gridPointIdToZoneId(1262144));

        assertEquals(3, SmosDgg.gridPointIdToZoneId(2000001));
        assertEquals(3, SmosDgg.gridPointIdToZoneId(2000002));

        assertEquals(9, SmosDgg.gridPointIdToZoneId(8262143));
        assertEquals(9, SmosDgg.gridPointIdToZoneId(8262144));

        assertEquals(10, SmosDgg.gridPointIdToZoneId(9000001));
        assertEquals(10, SmosDgg.gridPointIdToZoneId(9000002));
        assertEquals(10, SmosDgg.gridPointIdToZoneId(9262144));
        assertEquals(10, SmosDgg.gridPointIdToZoneId(9262145));
    }

    @Test
    public void seqnumToGridPointId() {
        assertEquals(1, SmosDgg.seqnumToGridPointId(1));
        assertEquals(2, SmosDgg.seqnumToGridPointId(2));
        assertEquals(3, SmosDgg.seqnumToGridPointId(3));

        assertEquals(262144, SmosDgg.seqnumToGridPointId(262144));
        assertEquals(262145, SmosDgg.seqnumToGridPointId(262145));

        assertEquals(1000001, SmosDgg.seqnumToGridPointId(262146));
        assertEquals(1000002, SmosDgg.seqnumToGridPointId(262147));
        assertEquals(1262143, SmosDgg.seqnumToGridPointId(524288));
        assertEquals(1262144, SmosDgg.seqnumToGridPointId(524289));

        assertEquals(2000001, SmosDgg.seqnumToGridPointId(524290));
        assertEquals(2000002, SmosDgg.seqnumToGridPointId(524291));

        assertEquals(8262143, SmosDgg.seqnumToGridPointId(2359296));
        assertEquals(8262144, SmosDgg.seqnumToGridPointId(2359297));

        assertEquals(9000001, SmosDgg.seqnumToGridPointId(2359298));
        assertEquals(9000002, SmosDgg.seqnumToGridPointId(2359299));
        assertEquals(9262144, SmosDgg.seqnumToGridPointId(2621441));
        assertEquals(9262145, SmosDgg.seqnumToGridPointId(2621442));
    }

    @Test
    public void seqnumToSeqnumInZone() {
        assertEquals(1, SmosDgg.seqnumToSeqnumInZone(1));
        assertEquals(2, SmosDgg.seqnumToSeqnumInZone(2));
        assertEquals(3, SmosDgg.seqnumToSeqnumInZone(3));

        assertEquals(262144, SmosDgg.seqnumToSeqnumInZone(262144));
        assertEquals(262145, SmosDgg.seqnumToSeqnumInZone(262145));

        assertEquals(1, SmosDgg.seqnumToSeqnumInZone(262146));
        assertEquals(2, SmosDgg.seqnumToSeqnumInZone(262147));
        assertEquals(262143, SmosDgg.seqnumToSeqnumInZone(524288));
        assertEquals(262144, SmosDgg.seqnumToSeqnumInZone(524289));

        assertEquals(1, SmosDgg.seqnumToSeqnumInZone(524290));
        assertEquals(2, SmosDgg.seqnumToSeqnumInZone(524291));

        assertEquals(262143, SmosDgg.seqnumToSeqnumInZone(2359296));
        assertEquals(262144, SmosDgg.seqnumToSeqnumInZone(2359297));

        assertEquals(1, SmosDgg.seqnumToSeqnumInZone(2359298));
        assertEquals(2, SmosDgg.seqnumToSeqnumInZone(2359299));
        assertEquals(262144, SmosDgg.seqnumToSeqnumInZone(2621441));
        assertEquals(262145, SmosDgg.seqnumToSeqnumInZone(2621442));
    }

    @Test
    public void seqnumToZoneId() {
        assertEquals(1, SmosDgg.seqnumToZoneId(1));
        assertEquals(1, SmosDgg.seqnumToZoneId(2));
        assertEquals(1, SmosDgg.seqnumToZoneId(3));

        assertEquals(1, SmosDgg.seqnumToZoneId(262144));
        assertEquals(1, SmosDgg.seqnumToZoneId(262145));

        assertEquals(2, SmosDgg.seqnumToZoneId(262146));
        assertEquals(2, SmosDgg.seqnumToZoneId(262147));
        assertEquals(2, SmosDgg.seqnumToZoneId(524288));
        assertEquals(2, SmosDgg.seqnumToZoneId(524289));

        assertEquals(3, SmosDgg.seqnumToZoneId(524290));
        assertEquals(3, SmosDgg.seqnumToZoneId(524291));

        assertEquals(9, SmosDgg.seqnumToZoneId(2359296));
        assertEquals(9, SmosDgg.seqnumToZoneId(2359297));

        assertEquals(10, SmosDgg.seqnumToZoneId(2359298));
        assertEquals(10, SmosDgg.seqnumToZoneId(2359299));
        assertEquals(10, SmosDgg.seqnumToZoneId(2621441));
        assertEquals(10, SmosDgg.seqnumToZoneId(2621442));
    }
}
