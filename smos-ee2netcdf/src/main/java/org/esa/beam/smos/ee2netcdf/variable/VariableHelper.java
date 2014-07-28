package org.esa.beam.smos.ee2netcdf.variable;

import java.util.Arrays;

class VariableHelper {

    static float[][] getFloatArray(int width, int height, float fillValue) {
        final float[][] floatArray = new float[width][height];
        if (fillValue != 0.f) {
            for (int i = 0; i < width; i++) {
                final float[] vector = floatArray[i];
                Arrays.fill(vector, fillValue);
            }
        }

        return floatArray;
    }

    static float[] getFloatVector(int length, float fillValue) {
        final float[] vector = new float[length];
        if (fillValue != 0.f) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }

    static int[][] getIntArray(int width, int height, int fillValue) {
        final int[][] intArray = new int[width][height];
        if (fillValue != 0) {
            for (int i = 0; i < width; i++) {
                Arrays.fill(intArray[i], fillValue);
            }
        }
        return intArray;
    }

    static short[][] getShortArray(int width, int height, short fillValue) {
        final short[][] shortArray = new short[width][height];

        if (fillValue != 0) {
            for (int i = 0; i < width; i++) {
                Arrays.fill(shortArray[i], fillValue);
            }
        }

        return shortArray;
    }

    static int[] getIntVector(int length, int fillValue) {
        final int[] vector = new int[length];
        if (fillValue != 0) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }

    static short[] getShortVector(int length, short fillValue) {
        final short[] vector = new short[length];
        if (fillValue != (short) 0) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }

    static byte[] getByteVector(int length, byte fillValue) {
        final byte[] vector = new byte[length];
        if (fillValue != (byte) 0) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }

    static long[] getLongVector(int length, long fillValue) {
        final long[] vector = new long[length];
        if (fillValue != 0L) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }

    static double[] getDoubleVector(int length, double fillValue) {
        final double[] vector = new double[length];
        if (fillValue != 0.0) {
            Arrays.fill(vector, fillValue);

        }
        return vector;
    }
}
