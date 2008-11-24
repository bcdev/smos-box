package org.esa.beam.dataio.smos;

import java.nio.ByteOrder;


public class ByteArrayDecoder {
    private final ByteOrder byteOrder;

    public ByteArrayDecoder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public byte toByte(byte[] b, int boff) {
        return b[boff];
    }

    public void toBytes(byte[] b, int boff, byte[] s, int off, int len) {
        for (int j = 0; j < len; j++) {
            s[off + j] = b[boff];
            boff++;
        }
    }

    public short toShort(byte[] b, int boff) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return sb(b, boff);
        } else {
            return sl(b, boff);
        }
    }

    public void toShorts(byte[] b, int boff, short[] s, int off, int len) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                s[off + j] = sb(b, boff);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; j++) {
                s[off + j] = sl(b, boff);
                boff += 2;
            }
        }
    }

    private short sl(byte[] b, int boff) {
        int b0 = b[boff + 1];
        int b1 = b[boff] & 0xff;
        return (short) ((b0 << 8) | b1);
    }

    private short sb(byte[] b, int boff) {
        int b0 = b[boff];
        int b1 = b[boff + 1] & 0xff;
        return (short) ((b0 << 8) | b1);
    }

    public int toInt(byte[] b, int boff) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return ib(b, boff);
        } else {
            return il(b, boff);
        }
    }

    public void toInts(byte[] b, int boff, int[] i, int off, int len) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                i[off + j] = ib(b, boff);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; j++) {
                i[off + j] = il(b, boff);
                boff += 4;
            }
        }
    }

    private int il(byte[] b, int boff) {
        int b0 = b[boff + 3];
        int b1 = b[boff + 2] & 0xff;
        int b2 = b[boff + 1] & 0xff;
        int b3 = b[boff] & 0xff;
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    private int ib(byte[] b, int boff) {
        int b0 = b[boff];
        int b1 = b[boff + 1] & 0xff;
        int b2 = b[boff + 2] & 0xff;
        int b3 = b[boff + 3] & 0xff;
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public long toLong(byte[] b, int boff) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return lb(b, boff);
        } else {
            return ll(b, boff);
        }
    }

    public void toLongs(byte[] b, int boff, long[] l, int off, int len) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                l[off + j] = lb(b, boff);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; j++) {
                l[off + j] = ll(b, boff);
                boff += 8;
            }
        }
    }

    private long ll(byte[] b, int boff) {
        int b0 = b[boff + 7];
        int b1 = b[boff + 6] & 0xff;
        int b2 = b[boff + 5] & 0xff;
        int b3 = b[boff + 4] & 0xff;
        int b4 = b[boff + 3];
        int b5 = b[boff + 2] & 0xff;
        int b6 = b[boff + 1] & 0xff;
        int b7 = b[boff] & 0xff;

        int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;

        return ((long) i0 << 32) | (i1 & 0xffffffffL);
    }

    private long lb(byte[] b, int boff) {
        int b0 = b[boff];
        int b1 = b[boff + 1] & 0xff;
        int b2 = b[boff + 2] & 0xff;
        int b3 = b[boff + 3] & 0xff;
        int b4 = b[boff + 4];
        int b5 = b[boff + 5] & 0xff;
        int b6 = b[boff + 6] & 0xff;
        int b7 = b[boff + 7] & 0xff;

        int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;

        return ((long) i0 << 32) | (i1 & 0xffffffffL);
    }

    public float toFloat(byte[] b, int boff) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return fb(b, boff);
        } else {
            return fl(b, boff);
        }
    }

    public void toFloats(byte[] b, int boff, float[] f, int off, int len) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                f[off + j] = fb(b, boff);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; j++) {
                f[off + j] = fl(b, boff);
                boff += 4;
            }
        }
    }

    private float fl(byte[] b, int boff) {
        int b0 = b[boff + 3];
        int b1 = b[boff + 2] & 0xff;
        int b2 = b[boff + 1] & 0xff;
        int b3 = b[boff] & 0xff;
        int i = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        return Float.intBitsToFloat(i);
    }

    private float fb(byte[] b, int boff) {
        int b0 = b[boff];
        int b1 = b[boff + 1] & 0xff;
        int b2 = b[boff + 2] & 0xff;
        int b3 = b[boff + 3] & 0xff;
        int i = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        return Float.intBitsToFloat(i);
    }

    public double toDoubles(byte[] b, int boff) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return db(b, boff);
        } else {
            return dl(b, boff);
        }
    }

    public void toDoubles(byte[] b, int boff, double[] d, int off, int len) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; j++) {
                d[off + j] = db(b, boff);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; j++) {
                d[off + j] = dl(b, boff);
                boff += 8;
            }
        }
    }

    private double dl(byte[] b, int boff) {
        int b0 = b[boff + 7];
        int b1 = b[boff + 6] & 0xff;
        int b2 = b[boff + 5] & 0xff;
        int b3 = b[boff + 4] & 0xff;
        int b4 = b[boff + 3];
        int b5 = b[boff + 2] & 0xff;
        int b6 = b[boff + 1] & 0xff;
        int b7 = b[boff] & 0xff;

        int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
        long l = ((long) i0 << 32) | (i1 & 0xffffffffL);

        return Double.longBitsToDouble(l);
    }

    private double db(byte[] b, int boff) {
        int b0 = b[boff];
        int b1 = b[boff + 1] & 0xff;
        int b2 = b[boff + 2] & 0xff;
        int b3 = b[boff + 3] & 0xff;
        int b4 = b[boff + 4];
        int b5 = b[boff + 5] & 0xff;
        int b6 = b[boff + 6] & 0xff;
        int b7 = b[boff + 7] & 0xff;

        int i0 = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        int i1 = (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;
        long l = ((long) i0 << 32) | (i1 & 0xffffffffL);

        return Double.longBitsToDouble(l);
    }
}
