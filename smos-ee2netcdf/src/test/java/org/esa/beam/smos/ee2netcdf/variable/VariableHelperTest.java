package org.esa.beam.smos.ee2netcdf.variable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableHelperTest {

    @Test
    public void testGetFilledArray_floats_filled() {
        final int width = 3;
        final int height = 4;

        final float[][] array = VariableHelper.getFloatArray(width, height, -776.2f);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals(-776.2f, array[i][j], 1e-8);
            }
        }
    }

    @Test
    public void testGetFilledArray_floats_unfilled() {
        final int width = 5;
        final int height = 2;

        final float[][] array = VariableHelper.getFloatArray(width, height, 0.f);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals(0.f, array[i][j], 1e-8);
            }
        }
    }

    @Test
    public void testGetFilledVector_floats_filled() {
        final float[] floatVector = VariableHelper.getFloatVector(45, 13.7f);

        assertEquals(45, floatVector.length);
        for (final float value : floatVector) {
            assertEquals(13.7f, value, 1e-8);
        }
    }

    @Test
    public void testGetFilledVector_floats_unfilled() {
        final float[] floatVector = VariableHelper.getFloatVector(26, 0.f);

        assertEquals(26, floatVector.length);
        for (final float value : floatVector) {
            assertEquals(0.f, value, 1e-8);
        }
    }

    @Test
    public void testGetFilledArray_ints_filled() {
        final int width = 5;
        final int height = 3;

        final int[][] array = VariableHelper.getIntArray(width, height, 11);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals(11, array[i][j]);
            }
        }
    }

    @Test
    public void testGetFilledArray_ints_unfilled() {
        final int width = 6;
        final int height = 2;

        final int[][] array = VariableHelper.getIntArray(width, height, 0);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals(0, array[i][j]);
            }
        }
    }

    @Test
    public void testGetFilledVector_ints_filled() {
        final int[] intVector = VariableHelper.getIntVector(15, -18);

        assertEquals(15, intVector.length);
        for (final int value : intVector) {
            assertEquals(-18, value);
        }
    }

    @Test
    public void testGetFilledVector_ints_unfilled() {
        final int[] intVector = VariableHelper.getIntVector(14, 0);

        assertEquals(14, intVector.length);
        for (final int value : intVector) {
            assertEquals(0, value);
        }
    }

    @Test
    public void testGetFilledArray_shorts_filled() {
        final int width = 4;
        final int height = 6;

        final short[][] array = VariableHelper.getShortArray(width, height, (short) 14);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals((short)14, array[i][j]);
            }
        }
    }

    @Test
    public void testGetFilledArray_shorts_unfilled() {
        final int width = 2;
        final int height = 7;

        final short[][] array = VariableHelper.getShortArray(width, height, (short)0);
        assertEquals(width, array.length);
        assertEquals(height, array[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals((short)0, array[i][j]);
            }
        }
    }

    @Test
    public void testGetFilledVector_shorts_filled() {
        final short[] intVector = VariableHelper.getShortVector(17, (short) -567);

        assertEquals(17, intVector.length);
        for (final short value : intVector) {
            assertEquals((short)-567, value);
        }
    }

    @Test
    public void testGetFilledVector_shorts_unfilled() {
        final short[] shortVector = VariableHelper.getShortVector(18, (short)0);

        assertEquals(18, shortVector.length);
        for (final short value : shortVector) {
            assertEquals((short)0, value);
        }
    }

    @Test
    public void testGetFilledVector_bytes_filled() {
        final byte[] byteVector = VariableHelper.getByteVector(16, (byte)107);

        assertEquals(16, byteVector.length);
        for (final byte value : byteVector) {
            assertEquals((byte)107, value);
        }
    }

    @Test
    public void testGetFilledVector_bytes_unfilled() {
        final byte[] byteVector = VariableHelper.getByteVector(15, (byte)0);

        assertEquals(15, byteVector.length);
        for (final byte value : byteVector) {
            assertEquals((byte)0, value);
        }
    }

    @Test
    public void testGetFilledVector_longs_filled() {
        final long[] longVector = VariableHelper.getLongVector(19, -118837676435L);

        assertEquals(19, longVector.length);
        for (final long value : longVector) {
            assertEquals(-118837676435L, value);
        }
    }

    @Test
    public void testGetFilledVector_longs_unfilled() {
        final long[] longVector = VariableHelper.getLongVector(20, 0L);

        assertEquals(20, longVector.length);
        for (final long value : longVector) {
            assertEquals(0L, value);
        }
    }

    @Test
    public void testGetFilledVector_doubles_filled() {
        final double[] doubleVector = VariableHelper.getDoubleVector(18, 0.9986);

        assertEquals(18, doubleVector.length);
        for (final double value : doubleVector) {
            assertEquals(0.9986, value, 1e-8);
        }
    }

    @Test
    public void testGetFilledVector_doubles_unfilled() {
        final double[] doubleVector = VariableHelper.getDoubleVector(17, 0.0);

        assertEquals(17, doubleVector.length);
        for (final double value : doubleVector) {
            assertEquals(0.0, value, 1e-8);
        }
    }
}
